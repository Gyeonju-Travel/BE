package com.example.gyeonjutravel.domain.schedule.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.notification.repository.NotificationRepository;
import com.example.gyeonjutravel.domain.notification.service.NotificationService;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.gyeonjutravel.domain.schedule.dto.request.ScheduleDeleteRequest;
import com.example.gyeonjutravel.domain.schedule.dto.request.SchedulePreviewRequest;
import com.example.gyeonjutravel.domain.schedule.dto.response.DepartureResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.ScheduleDateResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.ScheduleDetailResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.SchedulePlaceResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.SchedulePreviewResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.ScheduleResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.ScheduleStartResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.WalkingRouteResponse;
import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.domain.schedule.repository.ScheduleRepository;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.MatrixPreview;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.PlaceCoordinate;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.SchedulePreview;
import com.example.gyeonjutravel.domain.stamp.repository.PlaceVisitRepository;
import com.example.gyeonjutravel.domain.stamp.repository.StampAlbumRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import com.example.gyeonjutravel.global.tmap.WalkingMatrix;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private static final LocalTime STAMP_ALBUM_READY_TIME = LocalTime.of(21, 0);

    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMatrixCache scheduleMatrixCache;
    private final NearestNeighborOptimizer nearestNeighborOptimizer;
    private final NotificationService notificationService;
    private final PlaceVisitRepository placeVisitRepository;
    private final StampAlbumRepository stampAlbumRepository;
    private final NotificationRepository notificationRepository;

    public SchedulePreviewResponse preview(Long memberId, SchedulePreviewRequest request) {
        validateUniquePlaceIds(request.placeIds());
        List<Place> places = findBookmarkedPlaces(memberId, request.placeIds());
        Map<Long, Place> placesById = indexPlaces(places);

        MatrixPreview matrixPreview = scheduleMatrixCache.createPreview(
                memberId,
                request.date(),
                request.departureArea(),
                places.stream()
                        .map(place -> new PlaceCoordinate(
                                place.getId(),
                                place.getLongitude(),
                                place.getLatitude()
                        ))
                        .toList()
        );
        List<Long> recommendedOrder = nearestNeighborOptimizer.optimize(
                request.placeIds(),
                matrixPreview.matrix()
        );

        List<SchedulePlaceResponse> recommendedPlaces = new ArrayList<>();
        Set<RouteKey> recommendedRouteKeys = new HashSet<>();
        String previousNodeKey = ScheduleMatrixCache.START_NODE_KEY;
        for (int index = 0; index < recommendedOrder.size(); index++) {
            Long placeId = recommendedOrder.get(index);
            String placeNodeKey = ScheduleMatrixCache.placeNodeKey(placeId);
            WalkingRoute route = requireWalkableRoute(matrixPreview.matrix(), previousNodeKey, placeNodeKey);
            recommendedPlaces.add(SchedulePlaceResponse.preview(
                    index + 1,
                    placesById.get(placeId),
                    route.durationSeconds(),
                    route.distanceMeters()
            ));
            recommendedRouteKeys.add(new RouteKey(previousNodeKey, placeNodeKey));
            previousNodeKey = placeNodeKey;
        }

        return new SchedulePreviewResponse(
                matrixPreview.token(),
                matrixPreview.expiresAt(),
                request.date(),
                DepartureResponse.from(request.departureArea()),
                recommendedPlaces,
                matrixPreview.matrix().routes().stream()
                        .filter(route -> !recommendedRouteKeys.contains(new RouteKey(
                                route.fromNodeKey(),
                                route.toNodeKey()
                        )))
                        .map(WalkingRouteResponse::from)
                        .toList()
        );
    }

    public SchedulePreviewResponse updatePreview(
            Long memberId,
            Long scheduleId,
            SchedulePreviewRequest request
    ) {
        Schedule schedule = findOwnedSchedule(memberId, scheduleId);
        validateUniquePlaceIds(request.placeIds());
        if (isDateOnlyChange(schedule, request)) {
            return previewDateChange(memberId, schedule, request);
        }
        return preview(memberId, request);
    }

    public ScheduleDateResponse getByDate(Long memberId, java.time.LocalDate date) {
        List<ScheduleDetailResponse> schedules = scheduleRepository
                .findAllByMemberIdAndTravelDateWithItems(memberId, date)
                .stream()
                .map(ScheduleDetailResponse::from)
                .toList();
        return ScheduleDateResponse.of(date, schedules);
    }

    @Transactional
    public ScheduleStartResponse start(Long memberId, Long scheduleId) {
        Schedule schedule = findOwnedSchedule(memberId, scheduleId);
        LocalDateTime now = LocalDateTime.now();
        schedule.start(now);
        if (!now.toLocalTime().isBefore(STAMP_ALBUM_READY_TIME)
                && now.toLocalDate().equals(schedule.getTravelDate())) {
            notificationService.createStampAlbumReadyNotificationIfAbsent(schedule);
        }
        return ScheduleStartResponse.from(schedule);
    }

    @Transactional
    public ScheduleStartResponse cancelStart(Long memberId, Long scheduleId) {
        Schedule schedule = findOwnedSchedule(memberId, scheduleId);
        schedule.cancelStart();
        return ScheduleStartResponse.from(schedule);
    }

    @Transactional
    public ScheduleResponse create(Long memberId, ScheduleCreateRequest request) {
        SchedulePreview preview = scheduleMatrixCache.getPreview(request.matrixToken(), memberId);
        validateOrder(preview.placeIds(), request.orderedPlaceIds());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
        List<Place> places = findBookmarkedPlaces(memberId, request.orderedPlaceIds());
        Map<Long, Place> placesById = indexPlaces(places);

        Schedule schedule = new Schedule(member, preview.date(), preview.departureArea());
        List<SchedulePlaceResponse> savedPlaces = new ArrayList<>();
        String previousNodeKey = ScheduleMatrixCache.START_NODE_KEY;

        for (int index = 0; index < request.orderedPlaceIds().size(); index++) {
            Long placeId = request.orderedPlaceIds().get(index);
            Place place = placesById.get(placeId);
            WalkingRoute route = requireWalkableRoute(
                    preview.matrix(),
                    previousNodeKey,
                    ScheduleMatrixCache.placeNodeKey(placeId)
            );

            schedule.addItem(
                    place,
                    index + 1,
                    route.durationSeconds(),
                    route.distanceMeters()
            );
            savedPlaces.add(SchedulePlaceResponse.saved(
                    index + 1,
                    place,
                    route.durationSeconds(),
                    route.distanceMeters()
            ));
            previousNodeKey = ScheduleMatrixCache.placeNodeKey(placeId);
        }

        Schedule savedSchedule = scheduleRepository.save(schedule);
        scheduleMatrixCache.consumePreview(request.matrixToken());
        return new ScheduleResponse(
                savedSchedule.getId(),
                savedSchedule.getTravelDate(),
                DepartureResponse.from(savedSchedule.getDepartureArea()),
                savedPlaces
        );
    }

    @Transactional
    public ScheduleResponse createRecommended(Long memberId, ScheduleCreateRequest request) {
        SchedulePreview preview = scheduleMatrixCache.getPreview(request.matrixToken(), memberId);
        validateOrder(preview.placeIds(), request.orderedPlaceIds());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
        List<Place> places = placeRepository.findAllById(request.orderedPlaceIds());
        if (places.size() != request.orderedPlaceIds().size()) {
            throw new GeneralException(ScheduleErrorCode.INVALID_PLACE_SELECTION);
        }
        Map<Long, Place> placesById = indexPlaces(places);

        Schedule schedule = new Schedule(member, preview.date(), preview.departureArea());
        List<SchedulePlaceResponse> savedPlaces = addItems(schedule, request.orderedPlaceIds(), placesById, preview);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        scheduleMatrixCache.consumePreview(request.matrixToken());
        return new ScheduleResponse(
                savedSchedule.getId(),
                savedSchedule.getTravelDate(),
                DepartureResponse.from(savedSchedule.getDepartureArea()),
                savedPlaces
        );
    }

    @Transactional
    public ScheduleResponse update(Long memberId, Long scheduleId, ScheduleCreateRequest request) {
        Schedule schedule = findOwnedSchedule(memberId, scheduleId);
        SchedulePreview preview = scheduleMatrixCache.getPreview(request.matrixToken(), memberId);
        validateOrder(preview.placeIds(), request.orderedPlaceIds());

        List<Place> places = findBookmarkedPlaces(memberId, request.orderedPlaceIds());
        Map<Long, Place> placesById = indexPlaces(places);

        schedule.updateDate(preview.date());
        schedule.updateRoute(preview.departureArea());
        scheduleRepository.flush();
        List<SchedulePlaceResponse> savedPlaces = addItems(schedule, request.orderedPlaceIds(), placesById, preview);

        scheduleMatrixCache.consumePreview(request.matrixToken());
        return toResponse(schedule, savedPlaces);
    }

    @Transactional
    public void delete(Long memberId, ScheduleDeleteRequest request) {
        List<Long> scheduleIds = request.scheduleIds();
        if (new HashSet<>(scheduleIds).size() != scheduleIds.size()) {
            throw new GeneralException(ScheduleErrorCode.INVALID_SCHEDULE_IDS);
        }

        List<Schedule> schedules = scheduleRepository.findAllByMemberIdAndIdIn(memberId, scheduleIds);
        if (schedules.size() != scheduleIds.size()) {
            throw new GeneralException(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
        }
        deleteScheduleDependents(memberId, scheduleIds);
        scheduleRepository.deleteItemsByMemberIdAndScheduleIdIn(memberId, scheduleIds);
        scheduleRepository.deleteAllByMemberIdAndIdIn(memberId, scheduleIds);
    }

    private void deleteScheduleDependents(Long memberId, List<Long> scheduleIds) {
        placeVisitRepository.deleteAllByMemberIdAndScheduleIdIn(memberId, scheduleIds);
        notificationRepository.deleteAllByMemberIdAndScheduleIdIn(memberId, scheduleIds);
        stampAlbumRepository.deletePhotosByMemberIdAndScheduleIdIn(memberId, scheduleIds);
        stampAlbumRepository.deleteAllByMemberIdAndScheduleIdIn(memberId, scheduleIds);
    }

    private List<Place> findBookmarkedPlaces(Long memberId, List<Long> placeIds) {
        List<Place> places = placeRepository.findBookmarkedPlacesByMemberIdAndIds(memberId, placeIds);
        if (places.size() != placeIds.size()) {
            throw new GeneralException(ScheduleErrorCode.PLACE_NOT_BOOKMARKED);
        }
        return places;
    }

    private Schedule findOwnedSchedule(Long memberId, Long scheduleId) {
        return scheduleRepository.findByIdAndMemberId(scheduleId, memberId)
                .orElseThrow(() -> new GeneralException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
    }

    private boolean isDateOnlyChange(Schedule schedule, SchedulePreviewRequest request) {
        List<Long> savedPlaceIds = schedule.getItems().stream()
                .map(item -> item.getPlace().getId())
                .toList();
        return !schedule.getTravelDate().equals(request.date())
                && schedule.getDepartureArea() == request.departureArea()
                && savedPlaceIds.size() == request.placeIds().size()
                && Set.copyOf(savedPlaceIds).equals(Set.copyOf(request.placeIds()));
    }

    private SchedulePreviewResponse previewDateChange(
            Long memberId,
            Schedule schedule,
            SchedulePreviewRequest request
    ) {
        List<String> nodeKeys = new ArrayList<>();
        List<WalkingRoute> routes = new ArrayList<>();
        nodeKeys.add(ScheduleMatrixCache.START_NODE_KEY);
        String previousNodeKey = ScheduleMatrixCache.START_NODE_KEY;

        for (var item : schedule.getItems()) {
            String placeNodeKey = ScheduleMatrixCache.placeNodeKey(item.getPlace().getId());
            nodeKeys.add(placeNodeKey);
            routes.add(new WalkingRoute(
                    previousNodeKey,
                    placeNodeKey,
                    item.getWalkingDurationSeconds(),
                    item.getWalkingDistanceMeters()
            ));
            previousNodeKey = placeNodeKey;
        }

        MatrixPreview matrixPreview = scheduleMatrixCache.createPreviewFromMatrix(
                memberId,
                request.date(),
                schedule.getDepartureArea(),
                schedule.getItems().stream().map(item -> item.getPlace().getId()).toList(),
                new WalkingMatrix(nodeKeys, routes)
        );
        List<SchedulePlaceResponse> places = schedule.getItems().stream()
                .map(item -> SchedulePlaceResponse.preview(
                        item.getVisitOrder(),
                        item.getPlace(),
                        item.getWalkingDurationSeconds(),
                        item.getWalkingDistanceMeters()
                ))
                .toList();
        return new SchedulePreviewResponse(
                matrixPreview.token(),
                matrixPreview.expiresAt(),
                request.date(),
                DepartureResponse.from(schedule.getDepartureArea()),
                places,
                List.of()
        );
    }

    private List<SchedulePlaceResponse> addItems(
            Schedule schedule,
            List<Long> orderedPlaceIds,
            Map<Long, Place> placesById,
            SchedulePreview preview
    ) {
        List<SchedulePlaceResponse> savedPlaces = new ArrayList<>();
        String previousNodeKey = ScheduleMatrixCache.START_NODE_KEY;
        for (int index = 0; index < orderedPlaceIds.size(); index++) {
            Long placeId = orderedPlaceIds.get(index);
            Place place = placesById.get(placeId);
            WalkingRoute route = requireWalkableRoute(
                    preview.matrix(),
                    previousNodeKey,
                    ScheduleMatrixCache.placeNodeKey(placeId)
            );
            schedule.addItem(place, index + 1, route.durationSeconds(), route.distanceMeters());
            savedPlaces.add(SchedulePlaceResponse.saved(
                    index + 1, place, route.durationSeconds(), route.distanceMeters()
            ));
            previousNodeKey = ScheduleMatrixCache.placeNodeKey(placeId);
        }
        return savedPlaces;
    }

    private ScheduleResponse toResponse(Schedule schedule, List<SchedulePlaceResponse> places) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getTravelDate(),
                DepartureResponse.from(schedule.getDepartureArea()),
                places
        );
    }

    private WalkingRoute requireWalkableRoute(WalkingMatrix matrix, String fromNodeKey, String toNodeKey) {
        WalkingRoute route = matrix.findRoute(fromNodeKey, toNodeKey)
                .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));
        if (route.durationSeconds() == null || route.distanceMeters() == null) {
            throw new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND);
        }
        return route;
    }

    private Map<Long, Place> indexPlaces(List<Place> places) {
        Map<Long, Place> placesById = new HashMap<>();
        places.forEach(place -> placesById.put(place.getId(), place));
        return placesById;
    }

    private void validateUniquePlaceIds(List<Long> placeIds) {
        if (placeIds == null || placeIds.isEmpty()
                || new HashSet<>(placeIds).size() != placeIds.size()) {
            throw new GeneralException(ScheduleErrorCode.INVALID_PLACE_SELECTION);
        }
    }

    private void validateOrder(List<Long> previewPlaceIds, List<Long> orderedPlaceIds) {
        if (orderedPlaceIds == null
                || orderedPlaceIds.size() != previewPlaceIds.size()
                || new HashSet<>(orderedPlaceIds).size() != orderedPlaceIds.size()
                || !Set.copyOf(orderedPlaceIds).equals(Set.copyOf(previewPlaceIds))) {
            throw new GeneralException(ScheduleErrorCode.INVALID_PLACE_ORDER);
        }
    }

    private record RouteKey(String fromNodeKey, String toNodeKey) {
    }
}
