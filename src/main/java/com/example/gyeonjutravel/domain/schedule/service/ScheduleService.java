package com.example.gyeonjutravel.domain.schedule.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
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
import com.example.gyeonjutravel.domain.schedule.dto.response.WalkingRouteResponse;
import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.domain.schedule.repository.ScheduleRepository;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.MatrixPreview;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.PlaceCoordinate;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.SchedulePreview;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleMatrixCache scheduleMatrixCache;
    private final NearestNeighborOptimizer nearestNeighborOptimizer;

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
            WalkingRoute route = matrixPreview.matrix()
                    .findRoute(previousNodeKey, placeNodeKey)
                    .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));
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

    public ScheduleDateResponse getByDate(Long memberId, java.time.LocalDate date) {
        List<ScheduleDetailResponse> schedules = scheduleRepository
                .findAllByMemberIdAndTravelDateWithItems(memberId, date)
                .stream()
                .map(ScheduleDetailResponse::from)
                .toList();
        return ScheduleDateResponse.of(date, schedules);
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
            WalkingRoute route = preview.matrix()
                    .findRoute(previousNodeKey, ScheduleMatrixCache.placeNodeKey(placeId))
                    .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));

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
    public void delete(Long memberId, ScheduleDeleteRequest request) {
        List<Long> scheduleIds = request.scheduleIds();
        if (new HashSet<>(scheduleIds).size() != scheduleIds.size()) {
            throw new GeneralException(ScheduleErrorCode.INVALID_SCHEDULE_IDS);
        }

        List<Schedule> schedules = scheduleRepository.findAllByMemberIdAndIdIn(memberId, scheduleIds);
        if (schedules.size() != scheduleIds.size()) {
            throw new GeneralException(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
        }
        scheduleRepository.deleteAll(schedules);
    }

    private List<Place> findBookmarkedPlaces(Long memberId, List<Long> placeIds) {
        List<Place> places = placeRepository.findBookmarkedPlacesByMemberIdAndIds(memberId, placeIds);
        if (places.size() != placeIds.size()) {
            throw new GeneralException(ScheduleErrorCode.PLACE_NOT_BOOKMARKED);
        }
        return places;
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
