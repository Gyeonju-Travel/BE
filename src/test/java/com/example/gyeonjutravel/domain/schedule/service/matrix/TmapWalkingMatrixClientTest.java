package com.example.gyeonjutravel.domain.schedule.service.matrix;

import com.example.gyeonjutravel.global.tmap.MatrixNode;
import com.example.gyeonjutravel.global.tmap.WalkingMatrix;
import com.example.gyeonjutravel.global.tmap.WalkingRoute;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TmapWalkingMatrixClientTest {

    @Test
    void requestsPedestrianMatrixAndMapsDurationAndDistance() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TmapWalkingMatrixClient client = new TmapWalkingMatrixClient(
                builder,
                "https://apis.openapi.sk.com",
                "test-app-key"
        );
        server.expect(once(), requestTo("https://apis.openapi.sk.com/tmap/matrix?version=1"))
                .andExpect(header("appKey", "test-app-key"))
                .andExpect(content().json("""
                        {
                          "origins": [
                            {"lon": "129.20995370588062", "lat": "35.83740829748873"},
                            {"lon": "129.21", "lat": "35.84"}
                          ],
                          "destinations": [
                            {"lon": "129.20995370588062", "lat": "35.83740829748873"},
                            {"lon": "129.21", "lat": "35.84"}
                          ],
                          "transportMode": "pedestrian",
                          "metric": "Recommendation"
                        }
                        """, JsonCompareMode.STRICT))
                .andRespond(withSuccess("""
                        {
                          "matrixRoutes": [
                            {"status":"Ok","originIndex":0,"destinationIndex":0,"duration":0,"distance":0},
                            {"status":"Ok","originIndex":0,"destinationIndex":1,"duration":720,"distance":849.6},
                            {"status":"Ok","originIndex":1,"destinationIndex":0,"duration":750,"distance":870.2},
                            {"status":"Ok","originIndex":1,"destinationIndex":1,"duration":0,"distance":0}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        WalkingMatrix matrix = client.calculate(List.of(
                new MatrixNode("START", 129.20995370588062, 35.83740829748873),
                new MatrixNode("PLACE:1", 129.21, 35.84)
        ));

        assertThat(matrix.findRoute("START", "PLACE:1")).get()
                .extracting(WalkingRoute::durationSeconds, WalkingRoute::distanceMeters)
                .containsExactly(720L, 850L);
        assertThat(matrix.findRoute("PLACE:1", "START")).get()
                .extracting(WalkingRoute::durationSeconds, WalkingRoute::distanceMeters)
                .containsExactly(750L, 870L);
        server.verify();
    }
}
