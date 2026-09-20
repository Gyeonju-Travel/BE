package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;
import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.domain.schedule.repository.ScheduleRepository;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleService;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;
import com.example.gyeonjutravel.global.tourapi.TourApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:tourism-workflow;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false")
@ActiveProfiles("test")
@Transactional
class TourismWorkflowIntegrationTest {
    @Autowired MemberRepository members;
    @Autowired PlaceRepository places;
    @Autowired ScheduleRepository schedules;
    @Autowired PlaceService placeService;
    @Autowired ScheduleService scheduleService;
    @Autowired EntityManager entityManager;
    @Autowired ObjectMapper mapper;
    @Autowired com.example.gyeonjutravel.domain.stamp.service.StampService stampService;
    @Autowired com.example.gyeonjutravel.domain.stamp.repository.PlaceVisitRepository visits;
    @MockitoBean TourApiClient client;

    @Test
    void awardsStampUsingLiveCoordinatesAndReadsAwardWithoutTourApi() throws Exception {
        Member member = members.save(Member.builder().email("stamp-tour@example.com")
                .password("encoded").name("스탬프 여행자").phoneNumber("010-2222-3333").build());
        Place identity = Place.tourReference("TOUR_API:456");
        identity.linkTourContent("456");
        identity.assignStampType(StampType.CHEOMSEONGDAE);
        identity = places.save(identity);
        Schedule schedule = new Schedule(member, LocalDate.now(), DepartureArea.HWANGRIDAN_GIL);
        schedule.addItem(identity, 1, 300L, 400L);
        schedule.start(java.time.LocalDateTime.now());
        schedules.saveAndFlush(schedule);
        Long memberId = member.getId();
        Long placeId = identity.getId();
        Long scheduleId = schedule.getId();
        entityManager.clear();
        when(client.common("456")).thenReturn(mapper.readTree("""
                {"contentid":"456","contenttypeid":"12","title":"첨성대 최신 이름",
                 "mapx":"129.2186","mapy":"35.8344"}
                """));
        var request = new com.example.gyeonjutravel.domain.stamp.dto.request.PlaceVisitCreateRequest(
                scheduleId, 129.2186, 35.8344);
        var award = stampService.visitPlace(memberId, placeId, request);
        assertThat(award.stampName()).isEqualTo("경주 첨성대");
        assertThat(stampService.visitPlace(memberId, placeId, request).visitId()).isEqualTo(award.visitId());
        entityManager.flush();
        entityManager.clear();
        var storedVisit = visits.findById(award.visitId()).orElseThrow();
        assertThat(storedVisit.getStampType()).isEqualTo(StampType.CHEOMSEONGDAE);
        assertThat(storedVisit.getPlace().getLatitude()).isZero();
        assertThat(storedVisit.getPlace().getName()).isEqualTo("관광지 정보 조회 필요");
        clearInvocations(client);
        var stamps = stampService.getMyPageStamps(memberId);
        assertThat(stamps.stamps()).extracting("stampName").contains("경주 첨성대");
        verifyNoInteractions(client);
    }

    @Test
    void bookmarksAndScheduleUseLiveInformationWithoutChangingTheirForeignKeys() throws Exception {
        Member member = members.save(Member.builder().email("tour-workflow@example.com")
                .password("encoded").name("여행자").phoneNumber("010-1111-2222").build());
        Place identity = Place.tourReference("TOUR_API:123");
        identity.linkTourContent("123");
        identity.assignStampType(StampType.CHEOMSEONGDAE);
        identity = places.save(identity);
        member.addBookmark(identity);
        Schedule schedule = new Schedule(member, LocalDate.now().plusDays(1), DepartureArea.HWANGRIDAN_GIL);
        schedule.addItem(identity, 1, 300L, 400L);
        schedules.saveAndFlush(schedule);
        Long placeId = identity.getId();
        Long memberId = member.getId();
        Long scheduleId = schedule.getId();
        entityManager.clear();

        when(client.common("123")).thenReturn(mapper.readTree("""
                {"contentid":"123","contenttypeid":"12","title":"관광공사에서 변경된 첨성대 이름",
                 "addr1":"최신 주소","mapx":"129.2186","mapy":"35.8344"}
                """));
        when(client.pet("123")).thenReturn(mapper.readTree("{\"acmpyNeedMtr\":\"목줄 필수\"}"));
        var bookmarks = placeService.getBookmarks(members.findById(memberId).orElseThrow(), null);
        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.getFirst().id()).isEqualTo(placeId);
        assertThat(bookmarks.getFirst().name()).isEqualTo("관광공사에서 변경된 첨성대 이름");
        assertThat(bookmarks.getFirst().petRequirements()).isEqualTo("목줄 필수");

        var date = scheduleService.getByDate(memberId, schedule.getTravelDate());
        assertThat(date.schedules().getFirst().lastPlaceName()).isEqualTo("관광공사에서 변경된 첨성대 이름");
        entityManager.flush();
        entityManager.clear();
        Place stored = places.findById(placeId).orElseThrow();
        assertThat(stored.getName()).isEqualTo("관광지 정보 조회 필요");
        assertThat(stored.getPetRequirements()).isNull();
        assertThat(schedules.findById(scheduleId).orElseThrow().getItems().getFirst().getPlace().getId()).isEqualTo(placeId);
        assertThat(StampType.fromPlace(stored)).contains(StampType.CHEOMSEONGDAE);
        verify(client, times(2)).common("123");
        verify(client, never()).attractions();
    }
}
