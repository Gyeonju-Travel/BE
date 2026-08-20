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
import com.example.gyeonjutravel.domain.home.enums.DogCondition;
import com.example.gyeonjutravel.domain.home.enums.RecommendedRouteStatus;
import com.example.gyeonjutravel.domain.home.enums.RecommendedRouteStep;
import com.example.gyeonjutravel.domain.home.dto.response.RecommendedRouteStatusResponse;
import com.example.gyeonjutravel.domain.home.exception.RecommendedRouteErrorCode;
import com.example.gyeonjutravel.domain.pet.entity.enums.DogSize;
import com.example.gyeonjutravel.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.gyeonjutravel.domain.schedule.dto.response.DepartureResponse;
import com.example.gyeonjutravel.domain.schedule.dto.response.ScheduleResponse;
import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;
import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.domain.schedule.service.NearestNeighborOptimizer;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleService;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.MatrixPreview;
import com.example.gyeonjutravel.domain.schedule.service.ScheduleMatrixCache.PlaceCoordinate;
import com.example.gyeonjutravel.domain.stamp.entity.StampType;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
@Slf4j
public class RecommendedRouteService {

    private final PlaceRepository placeRepository;
    private final PetRepository petRepository;
    private final ScheduleMatrixCache scheduleMatrixCache;
    private final ScheduleService scheduleService;
    private final RecommendedPlaceSelector recommendedPlaceSelector;
    private final NearestNeighborOptimizer nearestNeighborOptimizer;
    private final Map<Long, RecommendedRouteJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final AtomicLong recommendationIdGenerator = new AtomicLong(1);
    private static final long MAX_START_WALKING_DISTANCE_METERS = 4_000;
    private static final long MAX_ROUTE_SEGMENT_DURATION_SECONDS = 7_200;
    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final int UNPRIORITIZED_PLACE_RANK = 1_000;
    private static final String PINK_MUHLY_PLACE_NAME = "경주 핑크뮬리(경주 핑크뮬리 군락지)";

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
            log.warn("Recommended route job failed. memberId={}, jobId={}", memberId, jobId, exception);
            jobs.put(jobId, RecommendedRouteJob.failed(memberId, failureMessage(exception)));
        }
    }

    private String failureMessage(Exception exception) {
        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }
        return exception.getClass().getSimpleName();
    }

    private RecommendedRouteResult createPreview(Long memberId, Long recommendationId, RecommendedRouteRequest request) {
        Pet representativePet = petRepository.findFirstByMemberIdAndRepresentativeTrue(memberId)
                .orElseThrow(() -> new GeneralException(RecommendedRouteErrorCode.REPRESENTATIVE_PET_NOT_FOUND));
        List<Place> dataset = placesNearDeparture(placeRepository.findAll(), request.departureArea());
        validateDataset(dataset);

        jobs.put(recommendationId, RecommendedRouteJob.creating(memberId, RecommendedRouteStep.CONDITION_CHECKING));
        List<Long> recommendedPlaceIds = recommendedPlaceSelector.select(
                request.departureArea(),
                request.date(),
                request.condition(),
                representativePet,
                dataset
        );
        List<Place> recommendedPlaces = normalizeRecommendedPlaces(
                request,
                representativePet,
                dataset,
                findRecommendedPlaces(dataset, recommendedPlaceIds)
        );
        validateRecommendedPlaces(recommendedPlaces, targetPlaceCount(representativePet.getSize(), request.condition()));

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
        recommendedPlaces = optimizeRouteOrder(recommendedPlaces, matrixPreview);
        validateWalkingDurations(recommendedPlaces, matrixPreview);

        return toResult(request, recommendedPlaces, matrixPreview);
    }

    private List<Place> optimizeRouteOrder(List<Place> places, MatrixPreview matrixPreview) {
        Map<Long, Place> placesById = new HashMap<>();
        places.forEach(place -> placesById.put(place.getId(), place));
        return nearestNeighborOptimizer.optimize(
                        places.stream()
                                .map(Place::getId)
                                .toList(),
                        matrixPreview.matrix()
                )
                .stream()
                .map(placeId -> {
                    Place place = placesById.get(placeId);
                    if (place == null) {
                        throw new GeneralException(RecommendedRouteErrorCode.INVALID_AI_RESPONSE);
                    }
                    return place;
                })
                .toList();
    }

    private List<Place> placesNearDeparture(List<Place> places, DepartureArea departureArea) {
        return places.stream()
                .filter(place -> distanceMeters(
                        departureArea.getLongitude(),
                        departureArea.getLatitude(),
                        place.getLongitude(),
                        place.getLatitude()
                ) <= MAX_START_WALKING_DISTANCE_METERS)
                .toList();
    }

    private void validateWalkingDurations(List<Place> recommendedPlaces, MatrixPreview matrixPreview) {
        String previousNodeKey = ScheduleMatrixCache.START_NODE_KEY;
        for (Place place : recommendedPlaces) {
            String placeNodeKey = ScheduleMatrixCache.placeNodeKey(place.getId());
            WalkingRoute route = matrixPreview.matrix()
                    .findRoute(previousNodeKey, placeNodeKey)
                    .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));
            if (route.durationSeconds() > MAX_ROUTE_SEGMENT_DURATION_SECONDS) {
                throw new GeneralException(RecommendedRouteErrorCode.RECOMMENDED_ROUTE_TOO_FAR);
            }
            previousNodeKey = placeNodeKey;
        }
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

    private void validateRecommendedPlaces(List<Place> places, int targetCount) {
        Set<Long> placeIds = new HashSet<>();
        Set<PlaceCategory> categories = EnumSet.noneOf(PlaceCategory.class);
        for (Place place : places) {
            placeIds.add(place.getId());
            categories.add(place.getCategory());
        }
        if (places.size() != targetCount || placeIds.size() != places.size()
                || !categories.containsAll(List.of(
                PlaceCategory.ATTRACTION,
                PlaceCategory.RESTAURANT,
                PlaceCategory.CAFE
        )) || hasInvalidFoodPlaceCount(places, targetCount)) {
            throw new GeneralException(RecommendedRouteErrorCode.INVALID_AI_RESPONSE);
        }
    }

    private List<Place> normalizeRecommendedPlaces(
            RecommendedRouteRequest request,
            Pet representativePet,
            List<Place> dataset,
            List<Place> aiRecommendedPlaces
    ) {
        int targetCount = targetPlaceCount(representativePet.getSize(), request.condition());
        int restaurantCount = 1;
        int cafeCount = targetCount == 5 ? 2 : 1;
        int attractionCount = targetCount - restaurantCount - cafeCount;

        List<Place> attractions = candidatesByCategory(
                PlaceCategory.ATTRACTION,
                request.departureArea(),
                aiRecommendedPlaces,
                dataset
        );
        List<Place> restaurants = candidatesByCategory(
                PlaceCategory.RESTAURANT,
                request.departureArea(),
                aiRecommendedPlaces,
                dataset
        );
        List<Place> cafes = candidatesByCategory(
                PlaceCategory.CAFE,
                request.departureArea(),
                aiRecommendedPlaces,
                dataset
        );
        if (attractions.size() < attractionCount || restaurants.size() < restaurantCount || cafes.size() < cafeCount) {
            throw new GeneralException(RecommendedRouteErrorCode.INVALID_AI_RESPONSE);
        }

        List<Place> selectedAttractions = attractions.subList(0, attractionCount);
        List<Place> selectedRestaurants = restaurants.subList(0, restaurantCount);
        List<Place> selectedCafes = cafes.subList(0, cafeCount);
        List<Place> orderedPlaces = new ArrayList<>();
        orderedPlaces.add(selectedAttractions.get(0));
        orderedPlaces.add(selectedRestaurants.get(0));
        if (selectedAttractions.size() > 1) {
            orderedPlaces.add(selectedAttractions.get(1));
        }
        orderedPlaces.add(selectedCafes.get(0));
        if (selectedAttractions.size() > 2) {
            orderedPlaces.addAll(selectedAttractions.subList(2, selectedAttractions.size()));
        }
        if (selectedCafes.size() > 1) {
            orderedPlaces.addAll(selectedCafes.subList(1, selectedCafes.size()));
        }
        return orderedPlaces;
    }

    private List<Place> candidatesByCategory(
            PlaceCategory category,
            DepartureArea departureArea,
            List<Place> aiRecommendedPlaces,
            List<Place> dataset
    ) {
        Map<Long, Place> candidates = new LinkedHashMap<>();
        Map<Long, Integer> aiRanks = new HashMap<>();
        for (int index = 0; index < aiRecommendedPlaces.size(); index++) {
            aiRanks.put(aiRecommendedPlaces.get(index).getId(), index);
        }
        aiRecommendedPlaces.stream()
                .filter(place -> place.getCategory() == category)
                .filter(place -> isSelectableCandidate(category, departureArea, place))
                .forEach(place -> candidates.put(place.getId(), place));
        dataset.stream()
                .filter(place -> place.getCategory() == category)
                .filter(place -> isSelectableCandidate(category, departureArea, place))
                .forEach(place -> candidates.putIfAbsent(place.getId(), place));
        return candidates.values().stream()
                .sorted(candidateComparator(category, departureArea, aiRanks))
                .toList();
    }

    private boolean isSelectableCandidate(PlaceCategory category, DepartureArea departureArea, Place place) {
        if (category == PlaceCategory.ATTRACTION) {
            return isRecommendedAttraction(place) && !isDeparturePlace(departureArea, place);
        }
        return !isDeparturePlace(departureArea, place);
    }

    private boolean isRecommendedAttraction(Place place) {
        return StampType.fromPlace(place).isPresent() || PINK_MUHLY_PLACE_NAME.equals(place.getName());
    }

    private Comparator<Place> candidateComparator(
            PlaceCategory category,
            DepartureArea departureArea,
            Map<Long, Integer> aiRanks
    ) {
        return Comparator
                .comparingInt((Place place) -> attractionPriority(category, departureArea, place))
                .thenComparingInt(place -> areaPriority(departureArea, place))
                .thenComparingInt(place -> aiRanks.getOrDefault(place.getId(), UNPRIORITIZED_PLACE_RANK))
                .thenComparing(Place::getId);
    }

    private int attractionPriority(PlaceCategory category, DepartureArea departureArea, Place place) {
        if (category != PlaceCategory.ATTRACTION) {
            return UNPRIORITIZED_PLACE_RANK;
        }
        List<String> priorityNames = priorityAttractionNames(departureArea);
        int index = priorityNames.indexOf(place.getName());
        return index == -1 ? UNPRIORITIZED_PLACE_RANK : index;
    }

    private List<String> priorityAttractionNames(DepartureArea departureArea) {
        return switch (departureArea) {
            case CHEOMSEONGDAE -> List.of(
                    PINK_MUHLY_PLACE_NAME,
                    "경주 계림",
                    "경주 황리단길"
            );
            case GYOCHON_VILLAGE -> List.of(
                    "월정교",
                    PINK_MUHLY_PLACE_NAME,
                    "경주 첨성대",
                    "경주 계림"
            );
            case HWANGRIDAN_GIL -> List.of(
                    PINK_MUHLY_PLACE_NAME,
                    "경주 첨성대",
                    "경주 계림",
                    "월정교"
            );
            case GEUMRIDAN_GIL -> List.of(
                    "경주 황리단길",
                    PINK_MUHLY_PLACE_NAME,
                    "경주 첨성대",
                    "경주 계림"
            );
        };
    }

    private int areaPriority(DepartureArea departureArea, Place place) {
        if (place.getArea() == null) {
            return UNPRIORITIZED_PLACE_RANK;
        }
        List<String> priorityAreas = priorityAreaNames(departureArea);
        int index = priorityAreas.indexOf(place.getArea());
        return index == -1 ? UNPRIORITIZED_PLACE_RANK : index;
    }

    private List<String> priorityAreaNames(DepartureArea departureArea) {
        return switch (departureArea) {
            case GEUMRIDAN_GIL -> List.of("금리단길", "황리단길", "첨성대", "교촌마을");
            case HWANGRIDAN_GIL -> List.of("황리단길", "첨성대", "교촌마을", "금리단길");
            case CHEOMSEONGDAE -> List.of("첨성대", "교촌마을", "황리단길", "금리단길");
            case GYOCHON_VILLAGE -> List.of("교촌마을", "첨성대", "황리단길", "금리단길");
        };
    }

    private int targetPlaceCount(DogSize dogSize, DogCondition condition) {
        if (condition == DogCondition.BAD) {
            return 3;
        }
        if (dogSize == DogSize.SMALL) {
            return condition == DogCondition.BEST ? 4 : 3;
        }
        return condition == DogCondition.BEST ? 5 : 4;
    }

    private boolean isDeparturePlace(
            DepartureArea departureArea,
            Place place
    ) {
        return departurePlaceNames(departureArea).contains(place.getName());
    }

    private Set<String> departurePlaceNames(DepartureArea departureArea) {
        return switch (departureArea) {
            case HWANGRIDAN_GIL -> Set.of("경주 황리단길");
            case GEUMRIDAN_GIL -> Set.of("경주읍성");
            case CHEOMSEONGDAE -> Set.of("경주 첨성대");
            case GYOCHON_VILLAGE -> Set.of("경주 교촌마을");
        };
    }

    private boolean hasInvalidFoodPlaceCount(List<Place> places, int targetCount) {
        long restaurantCount = places.stream()
                .filter(place -> place.getCategory() == PlaceCategory.RESTAURANT)
                .count();
        long cafeCount = places.stream()
                .filter(place -> place.getCategory() == PlaceCategory.CAFE)
                .count();
        return restaurantCount != 1 || cafeCount != (targetCount == 5 ? 2 : 1);
    }

    private double distanceMeters(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
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
