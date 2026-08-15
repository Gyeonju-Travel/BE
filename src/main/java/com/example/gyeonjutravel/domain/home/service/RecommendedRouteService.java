package com.example.gyeonjutravel.domain.home.service;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.repository.PetRepository;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.domain.home.dto.request.RecommendedRouteRequest;
import com.example.gyeonjutravel.domain.home.dto.response.RecommendedRouteJobResponse;
import com.example.gyeonjutravel.domain.home.dto.response.RecommendedRoutePlaceResponse;
import com.example.gyeonjutravel.domain.home.dto.response.RecommendedRouteResultResponse;
import com.example.gyeonjutravel.domain.home.enums.RecommendedRouteStatus;
import com.example.gyeonjutravel.domain.home.enums.RecommendedRouteStep;
import com.example.gyeonjutravel.domain.home.dto.response.RecommendedRouteStatusResponse;
import com.example.gyeonjutravel.domain.home.exception.RecommendedRouteErrorCode;
import com.example.gyeonjutravel.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.gyeonjutravel.domain.schedule.dto.response.DepartureResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.ScheduleResponse;
import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleService;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.MatrixPreview;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.PlaceCoordinate;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendedRouteService {

    private final PlaceRepository placeRepository;
    private final PetRepository petRepository;
    private final ScheduleMatrixCache scheduleMatrixCache;
    private final ScheduleService scheduleService;
    private final RecommendedPlaceSelector recommendedPlaceSelector;
    private final Map<Long, RecommendedRouteJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final AtomicLong recommendationIdGenerator = new AtomicLong(1);

    public RecommendedRouteJobResponse create(Long memberId, RecommendedRouteRequest request) {
        Long recommendationId = recommendationIdGenerator.getAndIncrement();
        jobs.put(recommendationId, RecommendedRouteJob.creating(memberId, RecommendedRouteStep.DEPARTURE_ANALYZING));
        CompletableFuture.runAsync(() -> completeJob(memberId, recommendationId, request), executorService);
        return new RecommendedRouteJobResponse(recommendationId, RecommendedRouteStatus.CREATING);
    }

    public RecommendedRouteStatusResponse getStatus(Long memberId, Long recommendationId) {
        RecommendedRouteJob job = findJob(memberId, recommendationId);
        return new RecommendedRouteStatusResponse(
                recommendationId,
                job.status(),
                job.step(),
                job.step().getMessage(),
                job.errorMessage()
        );
    }

    public RecommendedRouteResultResponse getResult(Long memberId, Long recommendationId) {
        RecommendedRouteJob job = findJob(memberId, recommendationId);
        if (job.status() != RecommendedRouteStatus.COMPLETED || job.result() == null) {
            throw new GeneralException(RecommendedRouteErrorCode.JOB_NOT_FOUND);
        }
        return job.result().toResponse(recommendationId);
    }

    @Transactional
    public ScheduleResponse createSchedule(Long memberId, Long recommendationId) {
        RecommendedRouteJob job = findJob(memberId, recommendationId);
        if (job.status() != RecommendedRouteStatus.COMPLETED || job.result() == null) {
            throw new GeneralException(RecommendedRouteErrorCode.JOB_NOT_FOUND);
        }
        RecommendedRouteResult result = job.result();
        return scheduleService.createRecommended(
                memberId,
                new ScheduleCreateRequest(result.matrixToken(), result.placeIds())
        );
    }

    private RecommendedRouteJob findJob(Long memberId, Long jobId) {
        RecommendedRouteJob job = jobs.get(jobId);
        if (job == null || !job.memberId().equals(memberId)) {
            throw new GeneralException(RecommendedRouteErrorCode.JOB_NOT_FOUND);
        }
        return job;
    }

    private void completeJob(Long memberId, Long jobId, RecommendedRouteRequest request) {
        try {
            jobs.put(jobId, RecommendedRouteJob.creating(memberId, RecommendedRouteStep.COURSE_SEARCHING));
            RecommendedRouteResult result = createPreview(memberId, jobId, request);
            jobs.put(jobId, RecommendedRouteJob.completed(memberId, result));
        } catch (Exception exception) {
            jobs.put(jobId, RecommendedRouteJob.failed(memberId, exception.getMessage()));
        }
    }

    private RecommendedRouteResult createPreview(Long memberId, Long recommendationId, RecommendedRouteRequest request) {
        Pet representativePet = petRepository.findFirstByMemberIdAndRepresentativeTrue(memberId)
                .orElseThrow(() -> new GeneralException(RecommendedRouteErrorCode.REPRESENTATIVE_PET_NOT_FOUND));
        List<Place> dataset = placeRepository.findAll();
        validateDataset(dataset);

        jobs.put(recommendationId, RecommendedRouteJob.creating(memberId, RecommendedRouteStep.CONDITION_CHECKING));
        List<Long> recommendedPlaceIds = recommendedPlaceSelector.select(
                request.departureArea(),
                request.date(),
                request.condition(),
                representativePet,
                dataset
        );
        List<Place> recommendedPlaces = findRecommendedPlaces(dataset, recommendedPlaceIds);
        validateRecommendedPlaces(recommendedPlaces);

        jobs.put(recommendationId, RecommendedRouteJob.creating(memberId, RecommendedRouteStep.ROUTE_COMPLETED));
        MatrixPreview matrixPreview = scheduleMatrixCache.createPreview(
                memberId,
                request.date(),
                request.departureArea(),
                recommendedPlaces.stream()
                        .map(place -> new PlaceCoordinate(
                                place.getId(),
                                place.getLongitude(),
                                place.getLatitude()
                        ))
                        .toList()
        );

        return toResult(request, recommendedPlaces, matrixPreview);
    }

    private void validateDataset(List<Place> places) {
        Set<PlaceCategory> categories = EnumSet.noneOf(PlaceCategory.class);
        places.forEach(place -> categories.add(place.getCategory()));
        if (!categories.containsAll(List.of(
                PlaceCategory.ATTRACTION,
                PlaceCategory.RESTAURANT,
                PlaceCategory.CAFE
        ))) {
            throw new GeneralException(RecommendedRouteErrorCode.NOT_ENOUGH_PLACES);
        }
    }

    private List<Place> findRecommendedPlaces(List<Place> dataset, List<Long> recommendedPlaceIds) {
        Map<Long, Place> placesById = new HashMap<>();
        dataset.forEach(place -> placesById.put(place.getId(), place));

        List<Place> places = new ArrayList<>();
        for (Long placeId : recommendedPlaceIds) {
            Place place = placesById.get(placeId);
            if (place == null) {
                throw new GeneralException(RecommendedRouteErrorCode.INVALID_AI_RESPONSE);
            }
            places.add(place);
        }
        return places;
    }

    private void validateRecommendedPlaces(List<Place> places) {
        Set<Long> placeIds = new HashSet<>();
        Set<PlaceCategory> categories = EnumSet.noneOf(PlaceCategory.class);
        for (Place place : places) {
            placeIds.add(place.getId());
            categories.add(place.getCategory());
        }
        if (places.size() < 3 || places.size() > 5 || placeIds.size() != places.size()
                || !categories.containsAll(List.of(
                PlaceCategory.ATTRACTION,
                PlaceCategory.RESTAURANT,
                PlaceCategory.CAFE
        ))) {
            throw new GeneralException(RecommendedRouteErrorCode.INVALID_AI_RESPONSE);
        }
    }

    private RecommendedRouteResult toResult(
            RecommendedRouteRequest request,
            List<Place> recommendedPlaces,
            MatrixPreview matrixPreview
    ) {
        List<RecommendedRoutePlaceResponse> places = new ArrayList<>();
        List<Long> placeIds = new ArrayList<>();
        String previousNodeKey = ScheduleMatrixCache.START_NODE_KEY;

        for (int index = 0; index < recommendedPlaces.size(); index++) {
            Place place = recommendedPlaces.get(index);
            String placeNodeKey = ScheduleMatrixCache.placeNodeKey(place.getId());
            WalkingRoute route = matrixPreview.matrix()
                    .findRoute(previousNodeKey, placeNodeKey)
                    .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));

            places.add(RecommendedRoutePlaceResponse.of(
                    index + 1,
                    place,
                    route.durationSeconds(),
                    route.distanceMeters()
            ));
            placeIds.add(place.getId());
            previousNodeKey = placeNodeKey;
        }

        return new RecommendedRouteResult(
                matrixPreview.token(),
                request.date(),
                DepartureResponse.from(request.departureArea()),
                placeIds,
                places
        );
    }

    @PreDestroy
    void shutdown() {
        executorService.shutdown();
    }

    private record RecommendedRouteJob(
            Long memberId,
            RecommendedRouteStatus status,
            RecommendedRouteStep step,
            RecommendedRouteResult result,
            String errorMessage
    ) {
        static RecommendedRouteJob creating(Long memberId, RecommendedRouteStep step) {
            return new RecommendedRouteJob(memberId, RecommendedRouteStatus.CREATING, step, null, null);
        }

        static RecommendedRouteJob completed(Long memberId, RecommendedRouteResult result) {
            return new RecommendedRouteJob(
                    memberId,
                    RecommendedRouteStatus.COMPLETED,
                    RecommendedRouteStep.ROUTE_COMPLETED,
                    result,
                    null
            );
        }

        static RecommendedRouteJob failed(Long memberId, String errorMessage) {
            return new RecommendedRouteJob(
                    memberId,
                    RecommendedRouteStatus.FAILED,
                    RecommendedRouteStep.ROUTE_COMPLETED,
                    null,
                    errorMessage
            );
        }
    }

    private record RecommendedRouteResult(
            String matrixToken,
            java.time.LocalDate date,
            DepartureResponse departure,
            List<Long> placeIds,
            List<RecommendedRoutePlaceResponse> places
    ) {
        RecommendedRouteResultResponse toResponse(Long recommendationId) {
            return new RecommendedRouteResultResponse(recommendationId, date, departure, places);
        }
    }
}
