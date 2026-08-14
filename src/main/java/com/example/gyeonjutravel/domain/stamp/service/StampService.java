package com.example.gyeonjutravel.domain.stamp.service;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.repository.PetRepository;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.domain.schedule.repository.ScheduleRepository;
import com.example.gyeonjutravel.domain.stamp.dto.request.FootprintAddRequest;
import com.example.gyeonjutravel.domain.stamp.dto.request.PlaceVisitCreateRequest;
import com.example.gyeonjutravel.domain.stamp.dto.response.MyPageStampItemResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.MyPageStampsResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.PetFootprintResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.PlaceVisitResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.ScheduleFootprintResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.StampAlbumResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.TravelRecordItemResponse;
import com.example.gyeonjutravel.domain.stamp.dto.response.TravelRecordsResponse;
import com.example.gyeonjutravel.domain.stamp.entity.PlaceVisit;
import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;
import com.example.gyeonjutravel.domain.stamp.exception.StampErrorCode;
import com.example.gyeonjutravel.domain.stamp.repository.PlaceVisitRepository;
import com.example.gyeonjutravel.domain.stamp.repository.StampAlbumRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StampService {

    private static final LocalTime OFFICIAL_END_TIME = LocalTime.of(21, 0);
    private static final long STAMP_RADIUS_METERS = 40;
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private final ScheduleRepository scheduleRepository;
    private final PlaceRepository placeRepository;
    private final PetRepository petRepository;
    private final StampAlbumRepository stampAlbumRepository;
    private final PlaceVisitRepository placeVisitRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    public ScheduleFootprintResponse addFootprints(Long memberId, Long scheduleId, FootprintAddRequest request) {
        Schedule schedule = findOwnedSchedule(memberId, scheduleId);
        validateStarted(schedule);
        validateRecordableTime(schedule, LocalDateTime.now());

        StampAlbum album = findOrCreateAlbum(memberId, schedule);
        album.addDistance(request.distanceMeters());
        StampAlbum savedAlbum = stampAlbumRepository.save(album);
        return ScheduleFootprintResponse.from(savedAlbum);
    }

    @Transactional
    public StampAlbumResponse savePhotos(Long memberId, Long scheduleId, List<MultipartFile> photos) {
        if (photos == null || photos.size() != 2 || photos.stream().anyMatch(photo -> photo == null || photo.isEmpty())) {
            throw new GeneralException(StampErrorCode.INVALID_PHOTO_COUNT);
        }
        Schedule schedule = findOwnedSchedule(memberId, scheduleId);
        StampAlbum album = findOrCreateAlbum(memberId, schedule);
        List<String> imageUrls = photos.stream()
                .map(photo -> imageStorageService.upload(photo, "stamp-albums"))
                .toList();
        album.replacePhotos(imageUrls);
        return toAlbumResponse(memberId, stampAlbumRepository.save(album));
    }

    public StampAlbumResponse getAlbum(Long memberId, Long scheduleId) {
        Schedule schedule = findOwnedSchedule(memberId, scheduleId);
        validateAlbumReadable(schedule, LocalDateTime.now());

        StampAlbum album = stampAlbumRepository.findByScheduleIdAndMemberId(scheduleId, memberId)
                .orElseGet(() -> findOrCreateAlbum(memberId, schedule));
        return toAlbumResponse(memberId, album);
    }

    public PetFootprintResponse getPetFootprints(Long memberId, Long petId) {
        petRepository.findByIdAndMemberId(petId, memberId)
                .orElseThrow(() -> new GeneralException(StampErrorCode.PET_NOT_FOUND));
        long totalDistanceMeters = stampAlbumRepository.sumTotalDistanceMetersByPetIdAndMemberId(petId, memberId);
        return PetFootprintResponse.of(petId, totalDistanceMeters);
    }

    public MyPageStampsResponse getMyPageStamps(Long memberId) {
        List<MyPageStampItemResponse> stamps = earnedStampNames(memberId).stream()
                .map(MyPageStampItemResponse::of)
                .toList();
        return MyPageStampsResponse.from(stamps);
    }

    public TravelRecordsResponse getTravelRecords(Long memberId) {
        Map<Long, StampAlbum> albumsByScheduleId = albumsByScheduleId(memberId);
        List<TravelRecordItemResponse> records = completedSchedules(memberId, albumsByScheduleId).stream()
                .map(schedule -> TravelRecordItemResponse.from(schedule, albumsByScheduleId.get(schedule.getId())))
                .toList();
        return TravelRecordsResponse.of(records, earnedStampNames(memberId).size());
    }

    @Transactional
    public PlaceVisitResponse visitPlace(Long memberId, Long placeId, PlaceVisitCreateRequest request) {
        Schedule schedule = findOwnedSchedule(memberId, request.scheduleId());
        validateStarted(schedule);

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(ScheduleErrorCode.INVALID_PLACE_SELECTION));
        StampType stampType = StampType.fromPlace(place)
                .orElseThrow(() -> new GeneralException(StampErrorCode.UNSUPPORTED_STAMP_PLACE));
        long distanceMeters = Math.round(haversineMeters(
                request.longitude(),
                request.latitude(),
                place.getLongitude(),
                place.getLatitude()
        ));
        if (distanceMeters > STAMP_RADIUS_METERS) {
            throw new GeneralException(StampErrorCode.PLACE_TOO_FAR);
        }

        PlaceVisit visit = placeVisitRepository.findByMemberIdAndScheduleIdAndPlaceId(memberId, schedule.getId(), placeId)
                .orElseGet(() -> placeVisitRepository.save(new PlaceVisit(
                        schedule.getMember(),
                        schedule,
                        place,
                        LocalDateTime.now()
                )));
        return PlaceVisitResponse.of(visit, stampType);
    }

    private StampAlbum findOrCreateAlbum(Long memberId, Schedule schedule) {
        return stampAlbumRepository.findByScheduleIdAndMemberId(schedule.getId(), memberId)
                .orElseGet(() -> {
                    Pet representativePet = petRepository.findFirstByMemberIdAndRepresentativeTrue(memberId)
                            .orElseThrow(() -> new GeneralException(StampErrorCode.REPRESENTATIVE_PET_NOT_FOUND));
                    return new StampAlbum(schedule, schedule.getMember(), representativePet);
                });
    }

    private Schedule findOwnedSchedule(Long memberId, Long scheduleId) {
        return scheduleRepository.findByIdAndMemberId(scheduleId, memberId)
                .orElseThrow(() -> new GeneralException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
    }

    private void validateStarted(Schedule schedule) {
        if (!schedule.isStarted() || schedule.getStartedAt() == null) {
            throw new GeneralException(StampErrorCode.SCHEDULE_NOT_STARTED);
        }
    }

    private void validateRecordableTime(Schedule schedule, LocalDateTime recordedAt) {
        LocalDateTime officialEndAt = schedule.getTravelDate().atTime(OFFICIAL_END_TIME);
        if (recordedAt.isBefore(schedule.getStartedAt()) || recordedAt.isAfter(officialEndAt)) {
            throw new GeneralException(StampErrorCode.LOCATION_TIME_OUT_OF_RANGE);
        }
    }

    private void validateAlbumReadable(Schedule schedule, LocalDateTime requestedAt) {
        LocalDateTime officialEndAt = schedule.getTravelDate().atTime(OFFICIAL_END_TIME);
        if (!schedule.isStarted() || schedule.getStartedAt() == null || requestedAt.isBefore(officialEndAt)) {
            throw new GeneralException(StampErrorCode.STAMP_ALBUM_NOT_READY);
        }
    }

    private double haversineMeters(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
        double fromLatitudeRadians = Math.toRadians(fromLatitude);
        double toLatitudeRadians = Math.toRadians(toLatitude);
        double latitudeDelta = Math.toRadians(toLatitude - fromLatitude);
        double longitudeDelta = Math.toRadians(toLongitude - fromLongitude);

        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
                + Math.cos(fromLatitudeRadians) * Math.cos(toLatitudeRadians)
                * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private StampAlbumResponse toAlbumResponse(Long memberId, StampAlbum album) {
        List<PlaceVisit> visits = placeVisitRepository.findAllByMemberIdAndScheduleIdOrderByVisitedAtAsc(
                memberId,
                album.getSchedule().getId()
        );
        return StampAlbumResponse.from(album, visits);
    }

    private Set<String> earnedStampNames(Long memberId) {
        Set<String> stampNames = new LinkedHashSet<>();
        stampNames.add(StampType.WELCOME_DOG.getDisplayName());
        Map<Long, StampAlbum> albumsByScheduleId = albumsByScheduleId(memberId);
        if (!completedSchedules(memberId, albumsByScheduleId).isEmpty()) {
            stampNames.add(StampType.PERFECT_TRIP.getDisplayName());
        }
        placeVisitRepository.findAllWithPlaceByMemberId(memberId).stream()
                .map(PlaceVisit::getPlace)
                .map(StampType::fromPlace)
                .flatMap(java.util.Optional::stream)
                .map(StampType::getDisplayName)
                .forEach(stampNames::add);
        return stampNames;
    }

    private List<Schedule> completedSchedules(Long memberId, Map<Long, StampAlbum> albumsByScheduleId) {
        LocalDate today = LocalDate.now();
        return scheduleRepository.findStartedSchedulesWithItemsByMemberId(memberId).stream()
                .filter(schedule -> hasAlbumPhoto(albumsByScheduleId.get(schedule.getId()))
                        || today.isAfter(schedule.getTravelDate()))
                .sorted(Comparator.comparing(Schedule::getTravelDate).reversed()
                        .thenComparing(Comparator.comparing(Schedule::getId).reversed()))
                .toList();
    }

    private Map<Long, StampAlbum> albumsByScheduleId(Long memberId) {
        Map<Long, StampAlbum> albumsByScheduleId = new LinkedHashMap<>();
        stampAlbumRepository.findAllWithPhotosByMemberId(memberId)
                .forEach(album -> albumsByScheduleId.put(album.getSchedule().getId(), album));
        return albumsByScheduleId;
    }

    private boolean hasAlbumPhoto(StampAlbum album) {
        return album != null && !album.getPhotos().isEmpty();
    }
}
