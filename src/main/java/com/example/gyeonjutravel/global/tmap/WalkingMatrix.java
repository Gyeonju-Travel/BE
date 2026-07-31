package com.example.gyeonjutravel.global.tmap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class WalkingMatrix {

    private final List<String> nodeKeys;
    private final Map<RouteKey, WalkingRoute> routes;

    public WalkingMatrix(List<String> nodeKeys, List<WalkingRoute> routes) {
        this.nodeKeys = List.copyOf(nodeKeys);
        Map<RouteKey, WalkingRoute> routeMap = new LinkedHashMap<>();
        routes.forEach(route -> routeMap.put(
                new RouteKey(route.fromNodeKey(), route.toNodeKey()),
                route
        ));
        this.routes = Collections.unmodifiableMap(routeMap);
    }

    public List<String> nodeKeys() {
        return nodeKeys;
    }

    public List<WalkingRoute> routes() {
        return routes.values().stream().toList();
    }

    public Optional<WalkingRoute> findRoute(String fromNodeKey, String toNodeKey) {
        return Optional.ofNullable(routes.get(new RouteKey(fromNodeKey, toNodeKey)));
    }

    private record RouteKey(String fromNodeKey, String toNodeKey) {
    }
}
