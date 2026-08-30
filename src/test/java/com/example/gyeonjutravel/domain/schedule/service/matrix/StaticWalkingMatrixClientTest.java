package com.example.gyeonjutravel.domain.schedule.service.matrix;

import com.example.gyeonjutravel.global.tmap.MatrixNode;
import com.example.gyeonjutravel.global.tmap.WalkingMatrix;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StaticWalkingMatrixClientTest {

    private final StaticWalkingMatrixClient client = new StaticWalkingMatrixClient();

    @Test
    void returnsStoredWalkingTimeInBothDirections() {
        WalkingMatrix matrix = client.calculate(List.of(
                new MatrixNode("START", 129.20995370588062, 35.83740829748873),
                new MatrixNode("PLACE:1", 129.2114316, 35.83688753)
        ));

        assertThat(matrix.findRoute("START", "PLACE:1")).get()
                .extracting(WalkingRoute::durationSeconds, WalkingRoute::distanceMeters)
                .containsExactly(240L, 268L);
        assertThat(matrix.findRoute("PLACE:1", "START")).get()
                .extracting(WalkingRoute::durationSeconds, WalkingRoute::distanceMeters)
                .containsExactly(240L, 268L);
    }

    @Test
    void matchesNewCafeCoordinatesFromNodeData() {
        WalkingMatrix matrix = client.calculate(List.of(
                new MatrixNode("START", 129.213503362291, 35.8423816699688),
                new MatrixNode("PLACE:82", 129.2141147, 35.84687184)
        ));

        assertThat(matrix.findRoute("START", "PLACE:82")).isPresent();
    }

    @Test
    void coversEveryPlaceNodeFromEachDeparture() throws Exception {
        List<TestNode> dataNodes;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/data/walking-nodes.csv"),
                StandardCharsets.UTF_8
        ))) {
            dataNodes = reader.lines()
                    .skip(1)
                    .map(line -> line.split(",", -1))
                    .map(columns -> new TestNode(
                            columns[0],
                            "출발지".equals(columns[2]),
                            Double.parseDouble(columns[3]),
                            Double.parseDouble(columns[4])
                    ))
                    .toList();
        }

        List<TestNode> placeNodes = dataNodes.stream().filter(node -> !node.departure()).toList();
        for (TestNode departure : dataNodes.stream().filter(TestNode::departure).toList()) {
            List<MatrixNode> requestedNodes = new ArrayList<>();
            requestedNodes.add(new MatrixNode("START", departure.longitude(), departure.latitude()));
            placeNodes.forEach(node -> requestedNodes.add(new MatrixNode(
                    node.id(),
                    node.longitude(),
                    node.latitude()
            )));

            WalkingMatrix matrix = client.calculate(requestedNodes);

            assertThat(matrix.routes()).hasSize(requestedNodes.size() * (requestedNodes.size() - 1));
        }
    }

    private record TestNode(String id, boolean departure, double longitude, double latitude) {
    }
}
