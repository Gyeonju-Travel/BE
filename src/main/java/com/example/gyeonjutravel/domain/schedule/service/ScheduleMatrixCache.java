package com.example.gyeonjutravel.domain.schedule.service;

import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;
import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tmap.MatrixNode;
import com.example.gyeonjutravel.global.tmap.WalkingMatrix;
import com.example.gyeonjutravel.global.tmap.WalkingMatrixClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScheduleMatrixCache {

    public static final String START_NODE_KEY = "START";

    private final WalkingMatrixClient walkingMatrixClient;
    private final Duration matrixTtl;
    private final Duration previewTtl;
    private final Clock clock;
    private final Map<MatrixCacheKey, CachedMatrix> matrices = new ConcurrentHashMap<>();
    private final Map<String, SchedulePreview> previews = new ConcurrentHashMap<>();

    @Autowired
    public ScheduleMatrixCache(
            WalkingMatrixClient walkingMatrixClient,
            @Value("${app.schedule.matrix-cache-ttl:24h}") Duration matrixTtl,
            @Value("${app.schedule.preview-ttl:30m}") Duration previewTtl
    ) {
        this(walkingMatrixClient, matrixTtl, previewTtl, Clock.systemUTC());
    }

    ScheduleMatrixCache(
            WalkingMatrixClient walkingMatrixClient,
            Duration matrixTtl,
            Duration previewTtl,
            Clock clock
    ) {
        this.walkingMatrixClient = walkingMatrixClient;
        this.matrixTtl = matrixTtl;
        this.previewTtl = previewTtl;
        this.clock = clock;
    }

    public MatrixPreview createPreview(
            Long memberId,
            LocalDate date,
            DepartureArea departureArea,
            List<PlaceCoordinate> places
    ) {
        List<PlaceCoordinate> sortedPlaces = places.stream()
                .sorted(Comparator.comparing(PlaceCoordinate::placeId))
                .toList();
        MatrixCacheKey cacheKey = new MatrixCacheKey(
                departureArea,
                sortedPlaces.stream().map(PlaceCoordinate::placeId).toList()
        );
        Instant now = clock.instant();
        CachedMatrix cachedMatrix = matrices.compute(cacheKey, (key, existing) -> {
            if (existing != null && existing.expiresAt().isAfter(now)) {
                return existing;
            }
            WalkingMatrix matrix = walkingMatrixClient.calculate(toNodes(departureArea, sortedPlaces));
            return new CachedMatrix(matrix, now.plus(matrixTtl));
        });

        String token = UUID.randomUUID().toString();
        Instant previewExpiresAt = now.plus(previewTtl);
        SchedulePreview preview = new SchedulePreview(
                memberId,
                date,
                departureArea,
                sortedPlaces.stream().map(PlaceCoordinate::placeId).toList(),
                cachedMatrix.matrix(),
                previewExpiresAt
        );
        previews.put(token, preview);
        removeExpired(now);
        return new MatrixPreview(token, cachedMatrix.matrix(), previewExpiresAt);
    }

    public MatrixPreview createPreviewFromMatrix(
            Long memberId,
            LocalDate date,
            DepartureArea departureArea,
            List<Long> placeIds,
            WalkingMatrix matrix
    ) {
        Instant now = clock.instant();
        String token = UUID.randomUUID().toString();
        Instant expiresAt = now.plus(previewTtl);
        previews.put(token, new SchedulePreview(
                memberId, date, departureArea, List.copyOf(placeIds), matrix, expiresAt
        ));
        removeExpired(now);
        return new MatrixPreview(token, matrix, expiresAt);
    }

    public SchedulePreview getPreview(String token, Long memberId) {
        SchedulePreview preview = previews.get(token);
        if (preview == null || !preview.memberId().equals(memberId)
                || !preview.expiresAt().isAfter(clock.instant())) {
            if (preview != null && !preview.expiresAt().isAfter(clock.instant())) {
                previews.remove(token);
            }
            throw new GeneralException(ScheduleErrorCode.PREVIEW_EXPIRED);
        }
        return preview;
    }

    public void consumePreview(String token) {
        previews.remove(token);
    }

    public boolean isWalkable(PlaceCoordinate place) {
        return walkingMatrixClient.isWalkable(new MatrixNode(
                placeNodeKey(place.placeId()),
                place.longitude(),
                place.latitude()
        ));
    }

    private List<MatrixNode> toNodes(DepartureArea departureArea, List<PlaceCoordinate> places) {
        List<MatrixNode> nodes = new ArrayList<>();
        nodes.add(new MatrixNode(
                START_NODE_KEY,
                departureArea.getLongitude(),
                departureArea.getLatitude()
        ));
        places.forEach(place -> nodes.add(new MatrixNode(
                placeNodeKey(place.placeId()),
                place.longitude(),
                place.latitude()
        )));
        return nodes;
    }

    private void removeExpired(Instant now) {
        previews.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        matrices.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }

    public static String placeNodeKey(Long placeId) {
        return "PLACE:" + placeId;
    }

    public record PlaceCoordinate(Long placeId, double longitude, double latitude) {
    }

    public record MatrixPreview(String token, WalkingMatrix matrix, Instant expiresAt) {
    }

    public record SchedulePreview(
            Long memberId,
            LocalDate date,
            DepartureArea departureArea,
            List<Long> placeIds,
            WalkingMatrix matrix,
            Instant expiresAt
    ) {
    }

    private record MatrixCacheKey(DepartureArea departureArea, List<Long> placeIds) {
    }

    private record CachedMatrix(WalkingMatrix matrix, Instant expiresAt) {
    }
}
