package com.example.gyeonjutravel.domain.schedule.service.matrix;

import com.example.gyeonjutravel.domain.schedule.exception.ScheduleErrorCode;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.tmap.MatrixNode;
import com.example.gyeonjutravel.global.tmap.WalkingMatrix;
import com.example.gyeonjutravel.global.tmap.WalkingMatrixClient;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Component
public class TmapWalkingMatrixClient implements WalkingMatrixClient {

    private final RestClient restClient;
    private final String appKey;

    public TmapWalkingMatrixClient(
            RestClient.Builder restClientBuilder,
            @Value("${tmap.base-url:https://apis.openapi.sk.com}") String baseUrl,
            @Value("${tmap.app-key:}") String appKey
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.appKey = appKey;
    }

    @Override
    public WalkingMatrix calculate(List<MatrixNode> nodes) {
        if (appKey == null || appKey.isBlank()) {
            throw new GeneralException(ScheduleErrorCode.TMAP_NOT_CONFIGURED);
        }

        List<TmapPoint> points = nodes.stream()
                .map(node -> new TmapPoint(Double.toString(node.longitude()), Double.toString(node.latitude())))
                .toList();
        TmapMatrixRequest request = new TmapMatrixRequest(
                points,
                points,
                "pedestrian",
                "Recommendation"
        );

        try {
            TmapMatrixResponse response = restClient.post()
                    .uri("/tmap/matrix?version=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("appKey", appKey)
                    .body(request)
                    .retrieve()
                    .body(TmapMatrixResponse.class);
            return toWalkingMatrix(nodes, response);
        } catch (GeneralException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new GeneralException(ScheduleErrorCode.TMAP_REQUEST_FAILED, exception);
        }
    }

    private WalkingMatrix toWalkingMatrix(List<MatrixNode> nodes, TmapMatrixResponse response) {
        if (response == null || response.matrixRoutes() == null) {
            throw new GeneralException(ScheduleErrorCode.TMAP_REQUEST_FAILED);
        }

        List<WalkingRoute> routes = new ArrayList<>();
        for (TmapRoute route : response.matrixRoutes()) {
            if (!"Ok".equalsIgnoreCase(route.status())
                    || route.originIndex() < 0 || route.originIndex() >= nodes.size()
                    || route.destinationIndex() < 0 || route.destinationIndex() >= nodes.size()) {
                throw new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND);
            }
            if (route.originIndex() == route.destinationIndex()) {
                continue;
            }
            routes.add(new WalkingRoute(
                    nodes.get(route.originIndex()).key(),
                    nodes.get(route.destinationIndex()).key(),
                    route.duration(),
                    Math.round(route.distance())
            ));
        }

        int expectedRouteCount = nodes.size() * (nodes.size() - 1);
        if (routes.size() != expectedRouteCount) {
            throw new GeneralException(ScheduleErrorCode.WALKING_ROUTE_NOT_FOUND);
        }
        return new WalkingMatrix(nodes.stream().map(MatrixNode::key).toList(), routes);
    }

    private record TmapPoint(String lon, String lat) {
    }

    private record TmapMatrixRequest(
            List<TmapPoint> origins,
            List<TmapPoint> destinations,
            String transportMode,
            String metric
    ) {
    }

    private record TmapMatrixResponse(List<TmapRoute> matrixRoutes) {
    }

    private record TmapRoute(
            String status,
            int originIndex,
            int destinationIndex,
            long duration,
            double distance
    ) {
    }
}
