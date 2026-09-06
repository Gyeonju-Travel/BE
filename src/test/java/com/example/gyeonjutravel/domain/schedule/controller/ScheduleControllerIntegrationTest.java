package com.example.gyeonjutravel.domain.schedule.controller;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;
import com.example.gyeonjutravel.domain.schedule.repository.ScheduleRepository;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache;
import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;
import com.example.gyeonjutravel.domain.stamp.repository.PlaceVisitRepository;
import com.example.gyeonjutravel.global.tmap.MatrixNode;
import com.example.gyeonjutravel.global.tmap.WalkingMatrix;
import com.example.gyeonjutravel.global.tmap.WalkingMatrixClient;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import com.example.gyeonjutravel.global.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ScheduleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private PlaceVisitRepository placeVisitRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private WalkingMatrixClient walkingMatrixClient;

    @Test
    void memberCanPreviewReorderAndSaveWalkingSchedule() throws Exception {
        Member member = saveMember();
        Place cafe = savePlace("SCHEDULE:CAFE", "반려견 카페", 129.2100, 35.8370);
        Place garden = savePlace("SCHEDULE:GARDEN", "정원", 129.2200, 35.8330);
        Place village = savePlace("SCHEDULE:VILLAGE", "한옥마을", 129.2150, 35.8290);
        member.addBookmark(cafe);
        member.addBookmark(garden);
        member.addBookmark(village);
        memberRepository.flush();

        when(walkingMatrixClient.calculate(anyList()))
                .thenAnswer(invocation -> matrixFor(
                        invocation.getArgument(0),
                        cafe.getId(),
                        garden.getId(),
                        village.getId()
                ));

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String previewRequest = """
                {
                  "departureArea": "HWANGRIDAN_GIL",
                  "date": "%s",
                  "placeIds": [%d, %d, %d]
                }
                """.formatted(
                LocalDate.now().plusDays(1),
                cafe.getId(),
                garden.getId(),
                village.getId()
        );

        MvcResult previewResult = mockMvc.perform(post("/api/schedules/preview")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(previewRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.departure.code").value("HWANGRIDAN_GIL"))
                .andExpect(jsonPath("$.result.recommendedPlaces[0].placeId").value(garden.getId()))
                .andExpect(jsonPath("$.result.recommendedPlaces[0].category").value("CAFE"))
                .andExpect(jsonPath("$.result.recommendedPlaces[0].categoryLabel").value("카페"))
                .andExpect(jsonPath("$.result.recommendedPlaces[0].walkingDurationSeconds").value(300))
                .andExpect(jsonPath("$.result.recommendedPlaces[0].walkingDistanceMeters").value(350))
                .andExpect(jsonPath("$.result.recommendedPlaces[1].placeId").value(cafe.getId()))
                .andExpect(jsonPath("$.result.recommendedPlaces[1].walkingDurationSeconds").value(200))
                .andExpect(jsonPath("$.result.recommendedPlaces[1].walkingDistanceMeters").value(250))
                .andExpect(jsonPath("$.result.recommendedPlaces[2].placeId").value(village.getId()))
                .andExpect(jsonPath("$.result.recommendedPlaces[2].walkingDurationSeconds").value(100))
                .andExpect(jsonPath("$.result.recommendedPlaces[2].walkingDistanceMeters").value(150))
                .andExpect(jsonPath("$.result.walkingTimeMatrix.length()").value(9))
                .andExpect(jsonPath("$.result.routes").doesNotExist())
                .andReturn();
        String matrixToken = objectMapper.readTree(previewResult.getResponse().getContentAsString())
                .path("result")
                .path("matrixToken")
                .asText();

        mockMvc.perform(post("/api/schedules/preview")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(previewRequest))
                .andExpect(status().isOk());
        verify(walkingMatrixClient, times(1)).calculate(anyList());

        MvcResult createResult = mockMvc.perform(post("/api/schedules")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matrixToken": "%s",
                                  "orderedPlaceIds": [%d, %d, %d]
                                }
                                """.formatted(
                                matrixToken,
                                village.getId(),
                                garden.getId(),
                                cafe.getId()
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.places[0].placeId").value(village.getId()))
                .andExpect(jsonPath("$.result.places[0].category").value("CAFE"))
                .andExpect(jsonPath("$.result.places[0].categoryLabel").value("카페"))
                .andExpect(jsonPath("$.result.places[0].walkingDurationSeconds").value(900))
                .andExpect(jsonPath("$.result.places[1].placeId").value(garden.getId()))
                .andExpect(jsonPath("$.result.places[1].walkingDurationSeconds").value(400))
                .andExpect(jsonPath("$.result.places[2].placeId").value(cafe.getId()))
                .andExpect(jsonPath("$.result.places[2].walkingDurationSeconds").value(200))
                .andReturn();

        long scheduleId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("result")
                .path("scheduleId")
                .asLong();
        Schedule savedSchedule = scheduleRepository.findById(scheduleId).orElseThrow();
        assertThat(savedSchedule.getItems())
                .extracting(item -> item.getPlace().getId())
                .containsExactly(village.getId(), garden.getId(), cafe.getId());
        assertThat(savedSchedule.getItems())
                .extracting(item -> item.getWalkingDurationSeconds())
                .containsExactly(900L, 400L, 200L);
    }

    @Test
    void memberCanPreviewAllChangesAndReplaceRoute() throws Exception {
        Member member = saveMember();
        Place oldPlace = savePlace("SCHEDULE:UPDATE:OLD", "기존 장소", 129.2100, 35.8370);
        Place newPlace = savePlace("SCHEDULE:UPDATE:NEW", "새 장소", 129.2200, 35.8330);
        member.addBookmark(oldPlace);
        member.addBookmark(newPlace);
        memberRepository.flush();

        LocalDate oldDate = LocalDate.now().plusDays(1);
        Schedule schedule = new Schedule(member, oldDate, DepartureArea.HWANGRIDAN_GIL);
        schedule.addItem(oldPlace, 1, 100, 120);
        schedule = scheduleRepository.saveAndFlush(schedule);
        String accessToken = jwtTokenProvider.createAccessToken(member);

        LocalDate changedDate = oldDate.plusDays(1);
        MvcResult datePreviewResult = mockMvc.perform(post(
                            "/api/schedules/{scheduleId}/preview", schedule.getId()
                        )
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "departureArea": "HWANGRIDAN_GIL",
                                  "date": "%s",
                                  "placeIds": [%d]
                                }
                                """.formatted(changedDate, oldPlace.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.recommendedPlaces[0].placeId").value(oldPlace.getId()))
                .andExpect(jsonPath("$.result.recommendedPlaces[0].walkingDurationSeconds").value(100))
                .andReturn();
        String dateMatrixToken = objectMapper.readTree(datePreviewResult.getResponse().getContentAsString())
                .path("result")
                .path("matrixToken")
                .asText();
        verify(walkingMatrixClient, times(0)).calculate(anyList());

        mockMvc.perform(put("/api/schedules/{scheduleId}", schedule.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matrixToken": "%s",
                                  "departureArea": "HWANGRIDAN_GIL",
                                  "orderedPlaceIds": [%d]
                                }
                                """.formatted(dateMatrixToken, oldPlace.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.date").value(changedDate.toString()));
        verify(walkingMatrixClient, times(0)).calculate(anyList());

        when(walkingMatrixClient.calculate(anyList()))
                .thenAnswer(invocation -> matrixFor(
                        invocation.getArgument(0),
                        oldPlace.getId(),
                        newPlace.getId(),
                        -1L
                ));
        MvcResult previewResult = mockMvc.perform(post("/api/schedules/{scheduleId}/preview", schedule.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "departureArea": "CHEOMSEONGDAE",
                                  "date": "%s",
                                  "placeIds": [%d]
                                }
                                """.formatted(changedDate, newPlace.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.date").value(changedDate.toString()))
                .andExpect(jsonPath("$.result.recommendedPlaces[0].placeId").value(newPlace.getId()))
                .andReturn();
        String matrixToken = objectMapper.readTree(previewResult.getResponse().getContentAsString())
                .path("result")
                .path("matrixToken")
                .asText();

        mockMvc.perform(put("/api/schedules/{scheduleId}", schedule.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matrixToken": "%s",
                                  "departureArea": "CHEOMSEONGDAE",
                                  "orderedPlaceIds": [%d]
                                }
                                """.formatted(matrixToken, newPlace.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.date").value(changedDate.toString()))
                .andExpect(jsonPath("$.result.departure.code").value("CHEOMSEONGDAE"))
                .andExpect(jsonPath("$.result.places[0].placeId").value(newPlace.getId()));
        verify(walkingMatrixClient, times(1)).calculate(anyList());
    }

    @Test
    void memberCanGetSchedulesByDateAndDeleteSelectedSchedules() throws Exception {
        Member member = saveMember();
        Member anotherMember = memberRepository.save(Member.builder()
                .email("another-schedule@example.com")
                .password("encoded-password")
                .name("다른 사용자")
                .phoneNumber("010-1111-1111")
                .build());
        Place cafe = savePlace("SCHEDULE:DATE:CAFE", "날짜 카페", 129.2111, 35.8311);
        Place park = savePlace("SCHEDULE:DATE:PARK", "날짜 공원", 129.2222, 35.8422);
        LocalDate targetDate = LocalDate.now().plusDays(2);

        Schedule first = new Schedule(member, targetDate, DepartureArea.HWANGRIDAN_GIL);
        first.addItem(cafe, 1, 100, 120);
        first.addItem(park, 2, 200, 250);
        first = scheduleRepository.save(first);

        Schedule second = new Schedule(member, targetDate, DepartureArea.GEUMRIDAN_GIL);
        second.addItem(park, 1, 400, 500);
        second = scheduleRepository.save(second);
        second.start(LocalDate.now().atTime(10, 0));
        placeVisitRepository.save(new PlaceVisit(member, second, park, LocalDateTime.now()));

        Schedule anotherDate = new Schedule(member, targetDate.plusDays(1), DepartureArea.CHEOMSEONGDAE);
        anotherDate.addItem(cafe, 1, 300, 350);
        scheduleRepository.save(anotherDate);

        Schedule anotherMembersSchedule = new Schedule(
                anotherMember,
                targetDate,
                DepartureArea.GYOCHON_VILLAGE
        );
        anotherMembersSchedule.addItem(cafe, 1, 500, 600);
        anotherMembersSchedule = scheduleRepository.save(anotherMembersSchedule);
        scheduleRepository.flush();

        String accessToken = jwtTokenProvider.createAccessToken(member);
        mockMvc.perform(get("/api/schedules")
                        .param("date", targetDate.toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.date").value(targetDate.toString()))
                .andExpect(jsonPath("$.result.totalScheduleCount").value(2))
                .andExpect(jsonPath("$.result.schedules[0].scheduleId").value(first.getId()))
                .andExpect(jsonPath("$.result.schedules[0].started").value(false))
                .andExpect(jsonPath("$.result.schedules[0].startedAt").isEmpty())
                .andExpect(jsonPath("$.result.schedules[0].ended").value(false))
                .andExpect(jsonPath("$.result.schedules[0].endedAt").isEmpty())
                .andExpect(jsonPath("$.result.schedules[0].totalPlaceCount").value(2))
                .andExpect(jsonPath("$.result.schedules[0].totalWalkingDurationSeconds").value(300))
                .andExpect(jsonPath("$.result.schedules[0].departure.code").value("HWANGRIDAN_GIL"))
                .andExpect(jsonPath("$.result.schedules[0].lastPlaceName").value("날짜 공원"))
                .andExpect(jsonPath("$.result.schedules[0].places[0].placeId").value(cafe.getId()))
                .andExpect(jsonPath("$.result.schedules[0].places[0].category").value("CAFE"))
                .andExpect(jsonPath("$.result.schedules[0].places[0].categoryLabel").value("카페"))
                .andExpect(jsonPath("$.result.schedules[0].places[0].longitude").value(129.2111))
                .andExpect(jsonPath("$.result.schedules[0].places[0].latitude").value(35.8311))
                .andExpect(jsonPath("$.result.schedules[0].places[1].placeId").value(park.getId()))
                .andExpect(jsonPath("$.result.schedules[1].scheduleId").value(second.getId()))
                .andExpect(jsonPath("$.result.schedules[1].started").value(true))
                .andExpect(jsonPath("$.result.schedules[1].startedAt").isNotEmpty())
                .andExpect(jsonPath("$.result.schedules[1].ended").value(false))
                .andExpect(jsonPath("$.result.schedules[1].endedAt").isEmpty())
                .andExpect(jsonPath("$.result.schedules[1].lastPlaceName").value("날짜 공원"))
                .andExpect(jsonPath("$.result.schedules[1].totalPlaceCount").value(1))
                .andExpect(jsonPath("$.result.schedules[1].totalWalkingDurationSeconds").value(400));

        mockMvc.perform(delete("/api/schedules")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"scheduleIds": [%d, %d]}
                                """.formatted(first.getId(), second.getId())))
                .andExpect(status().isOk());
        scheduleRepository.flush();

        assertThat(scheduleRepository.existsById(first.getId())).isFalse();
        assertThat(scheduleRepository.existsById(second.getId())).isFalse();
        assertThat(scheduleRepository.existsById(anotherDate.getId())).isTrue();
        assertThat(scheduleRepository.existsById(anotherMembersSchedule.getId())).isTrue();

        mockMvc.perform(delete("/api/schedules")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"scheduleIds": [%d]}
                                """.formatted(anotherMembersSchedule.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_404_1"));
    }

    @Test
    void dateSchedulesIncludeEndedStatusAfterOfficialEndTime() throws Exception {
        Member member = saveMember();
        Place cafe = savePlace("SCHEDULE:ENDED:CAFE", "종료 카페", 129.2111, 35.8311);
        LocalDate endedDate = LocalDate.now().minusDays(1);
        Schedule schedule = new Schedule(member, endedDate, DepartureArea.HWANGRIDAN_GIL);
        schedule.addItem(cafe, 1, 100, 120);
        schedule.start(endedDate.atTime(10, 0));
        schedule = scheduleRepository.saveAndFlush(schedule);

        String accessToken = jwtTokenProvider.createAccessToken(member);
        mockMvc.perform(get("/api/schedules")
                        .param("date", endedDate.toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.schedules[0].scheduleId").value(schedule.getId()))
                .andExpect(jsonPath("$.result.schedules[0].started").value(true))
                .andExpect(jsonPath("$.result.schedules[0].startedAt").value(endedDate + "T10:00:00"))
                .andExpect(jsonPath("$.result.schedules[0].ended").value(true))
                .andExpect(jsonPath("$.result.schedules[0].endedAt").value(endedDate + "T21:00:00"));
    }

    @Test
    void memberCanStartAndCancelStartedSchedule() throws Exception {
        Member member = saveMember();
        Place cafe = savePlace("SCHEDULE:START:CAFE", "시작 카페", 129.2111, 35.8311);
        Schedule schedule = new Schedule(member, LocalDate.now(), DepartureArea.HWANGRIDAN_GIL);
        schedule.addItem(cafe, 1, 100, 120);
        schedule = scheduleRepository.saveAndFlush(schedule);

        String accessToken = jwtTokenProvider.createAccessToken(member);
        mockMvc.perform(post("/api/schedules/{scheduleId}/start", schedule.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.scheduleId").value(schedule.getId()))
                .andExpect(jsonPath("$.result.started").value(true))
                .andExpect(jsonPath("$.result.startedAt").isNotEmpty());

        mockMvc.perform(delete("/api/schedules/{scheduleId}/start", schedule.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.scheduleId").value(schedule.getId()))
                .andExpect(jsonPath("$.result.started").value(false))
                .andExpect(jsonPath("$.result.startedAt").isEmpty());
        scheduleRepository.flush();

        Schedule canceledSchedule = scheduleRepository.findById(schedule.getId()).orElseThrow();
        assertThat(canceledSchedule.isStarted()).isFalse();
        assertThat(canceledSchedule.getStartedAt()).isNull();
    }

    private WalkingMatrix matrixFor(
            List<MatrixNode> nodes,
            Long cafeId,
            Long gardenId,
            Long villageId
    ) {
        String cafe = ScheduleMatrixCache.placeNodeKey(cafeId);
        String garden = ScheduleMatrixCache.placeNodeKey(gardenId);
        String village = ScheduleMatrixCache.placeNodeKey(villageId);
        List<WalkingRoute> routes = new ArrayList<>();
        for (MatrixNode origin : nodes) {
            for (MatrixNode destination : nodes) {
                if (origin.key().equals(destination.key())) {
                    continue;
                }
                long duration = duration(origin.key(), destination.key(), cafe, garden, village);
                routes.add(new WalkingRoute(
                        origin.key(),
                        destination.key(),
                        duration,
                        duration + 50
                ));
            }
        }
        return new WalkingMatrix(nodes.stream().map(MatrixNode::key).toList(), routes);
    }

    private long duration(String from, String to, String cafe, String garden, String village) {
        if (ScheduleMatrixCache.START_NODE_KEY.equals(from) && cafe.equals(to)) {
            return 600;
        }
        if (ScheduleMatrixCache.START_NODE_KEY.equals(from) && garden.equals(to)) {
            return 300;
        }
        if (ScheduleMatrixCache.START_NODE_KEY.equals(from) && village.equals(to)) {
            return 900;
        }
        if (garden.equals(from) && cafe.equals(to)) {
            return 200;
        }
        if (cafe.equals(from) && village.equals(to)) {
            return 100;
        }
        if (village.equals(from) && garden.equals(to)) {
            return 400;
        }
        return 800;
    }

    private Member saveMember() {
        return memberRepository.save(Member.builder()
                .email("schedule@example.com")
                .password("encoded-password")
                .name("일정 사용자")
                .phoneNumber("010-0000-0000")
                .build());
    }

    private Place savePlace(String sourceKey, String name, double longitude, double latitude) {
        return placeRepository.save(Place.builder()
                .sourceKey(sourceKey)
                .category(PlaceCategory.CAFE)
                .originalCategory("카페")
                .name(name)
                .detailCategory("반려동물 동반 카페")
                .roadAddress("경상북도 경주시")
                .longitude(longitude)
                .latitude(latitude)
                .petAccessType("실내외")
                .petRequirements("리드줄")
                .build());
    }
}
