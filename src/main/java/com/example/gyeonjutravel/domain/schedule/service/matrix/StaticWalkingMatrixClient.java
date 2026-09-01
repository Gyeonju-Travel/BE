package com.example.gyeonjutravel.domain.schedule.service.matrix;

import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tmap.MatrixNode;
import com.example.gyeonjutravel.global.tmap.WalkingMatrix;
import com.example.gyeonjutravel.global.tmap.WalkingMatrixClient;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StaticWalkingMatrixClient implements WalkingMatrixClient {

    private static final String START_NODE_KEY = "START";
    private static final double NODE_MATCH_RADIUS_METERS = 30.0;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final List<KnownNode> knownNodes;
    private final Map<KnownRouteKey, RouteData> walkingRoutes;

    public StaticWalkingMatrixClient() {
        this.knownNodes = loadNodes("data/walking-nodes.csv");
        this.walkingRoutes = loadWalkingRoutes("data/walking-times.csv");
    }

    @Override
    public WalkingMatrix calculate(List<MatrixNode> nodes) {
        List<KnownNode> matchedNodes = nodes.stream()
                .map(this::matchKnownNode)
                .toList();
        List<WalkingRoute> routes = new ArrayList<>();

        for (int fromIndex = 0; fromIndex < nodes.size(); fromIndex++) {
            for (int toIndex = 0; toIndex < nodes.size(); toIndex++) {
                if (fromIndex == toIndex) {
                    continue;
                }
                RouteData route = findWalkingRoute(matchedNodes.get(fromIndex), matchedNodes.get(toIndex));
                routes.add(new WalkingRoute(
                        nodes.get(fromIndex).key(),
                        nodes.get(toIndex).key(),
                        route.durationSeconds(),
                        route.distanceMeters()
                ));
            }
        }

        return new WalkingMatrix(nodes.stream().map(MatrixNode::key).toList(), routes);
    }

    @Override
    public boolean isWalkable(MatrixNode requestedNode) {
        boolean departure = START_NODE_KEY.equals(requestedNode.key());
        return knownNodes.stream()
                .filter(node -> node.departure() == departure && node.walkable())
                .anyMatch(node -> distanceMeters(
                        requestedNode.longitude(),
                        requestedNode.latitude(),
                        node.longitude(),
                        node.latitude()
                ) <= NODE_MATCH_RADIUS_METERS);
    }

    private KnownNode matchKnownNode(MatrixNode requestedNode) {
        boolean departure = START_NODE_KEY.equals(requestedNode.key());
        return knownNodes.stream()
                .filter(node -> node.departure() == departure)
                .map(node -> new NodeDistance(
                        node,
                        distanceMeters(
                                requestedNode.longitude(),
                                requestedNode.latitude(),
                                node.longitude(),
                                node.latitude()
                        )
                ))
                .filter(candidate -> candidate.distanceMeters() <= NODE_MATCH_RADIUS_METERS)
                .min(Comparator.comparingDouble(NodeDistance::distanceMeters))
                .map(NodeDistance::node)
                .orElseThrow(() -> new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND));
    }

    private RouteData findWalkingRoute(KnownNode from, KnownNode to) {
        if (from.id().equals(to.id())) {
            return new RouteData(0L, 0L);
        }
        return walkingRoutes.getOrDefault(KnownRouteKey.of(from.id(), to.id()), RouteData.unavailable());
    }

    private List<KnownNode> loadNodes(String resourcePath) {
        List<KnownNode> nodes = new ArrayList<>();
        for (String line : readDataLines(resourcePath)) {
            String[] columns = line.split(",", -1);
            if (columns.length != 6) {
                throw invalidResource(resourcePath);
            }
            nodes.add(new KnownNode(
                    columns[0],
                    "출발지".equals(columns[2]),
                    parseDouble(columns[3], resourcePath),
                    parseDouble(columns[4], resourcePath),
                    "Y".equalsIgnoreCase(columns[5])
            ));
        }
        return List.copyOf(nodes);
    }

    private Map<KnownRouteKey, RouteData> loadWalkingRoutes(String resourcePath) {
        Map<KnownRouteKey, RouteData> routes = new HashMap<>();
        for (String line : readDataLines(resourcePath)) {
            String[] columns = line.split(",", -1);
            if (columns.length != 6) {
                throw invalidResource(resourcePath);
            }
            KnownRouteKey key = KnownRouteKey.of(columns[0], columns[2]);
            RouteData previous = routes.put(
                    key,
                    parseRouteData(columns[4], columns[5], resourcePath)
            );
            if (previous != null && !previous.equals(RouteData.unavailable())) {
                throw invalidResource(resourcePath);
            }
        }
        return Map.copyOf(routes);
    }

    private List<String> readDataLines(String resourcePath) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(),
                StandardCharsets.UTF_8
        ))) {
            return reader.lines().skip(1).filter(line -> !line.isBlank()).toList();
        } catch (IOException exception) {
            throw new IllegalStateException("도보시간 데이터 파일을 읽을 수 없습니다: " + resourcePath, exception);
        }
    }

    private double parseDouble(String value, String resourcePath) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw invalidResource(resourcePath, exception);
        }
    }

    private RouteData parseRouteData(String walkingMinutes, String distanceMeters, String resourcePath) {
        if (walkingMinutes.isBlank() || distanceMeters.isBlank()) {
            return RouteData.unavailable();
        }
        return new RouteData(parseLong(walkingMinutes, resourcePath) * 60L, parseLong(distanceMeters, resourcePath));
    }

    private long parseLong(String value, String resourcePath) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw invalidResource(resourcePath, exception);
        }
    }

    private IllegalStateException invalidResource(String resourcePath) {
        return new IllegalStateException("도보시간 데이터 형식이 올바르지 않습니다: " + resourcePath);
    }

    private IllegalStateException invalidResource(String resourcePath, Exception cause) {
        return new IllegalStateException("도보시간 데이터 형식이 올바르지 않습니다: " + resourcePath, cause);
    }

    private double distanceMeters(double fromLongitude, double fromLatitude, double toLongitude, double toLatitude) {
        double fromLatitudeRadians = Math.toRadians(fromLatitude);
        double toLatitudeRadians = Math.toRadians(toLatitude);
        double latitudeDelta = Math.toRadians(toLatitude - fromLatitude);
        double longitudeDelta = Math.toRadians(toLongitude - fromLongitude);

        double a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2)
                + Math.cos(fromLatitudeRadians) * Math.cos(toLatitudeRadians)
                * Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private record KnownNode(String id, boolean departure, double longitude, double latitude, boolean walkable) {
    }

    private record NodeDistance(KnownNode node, double distanceMeters) {
    }

    private record KnownRouteKey(String firstNodeId, String secondNodeId) {
        private static KnownRouteKey of(String firstNodeId, String secondNodeId) {
            if (firstNodeId.compareTo(secondNodeId) <= 0) {
                return new KnownRouteKey(firstNodeId, secondNodeId);
            }
            return new KnownRouteKey(secondNodeId, firstNodeId);
        }
    }

    private record RouteData(Long durationSeconds, Long distanceMeters) {
        private static RouteData unavailable() {
            return new RouteData(null, null);
        }
    }
}
