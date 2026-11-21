package com.alexstudio.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@ActiveProfiles("test")
class CompetitionSeasonApiTests {
    @LocalServerPort
    private int port;

    @Test
    void independentSeasonsAndCompetitionSeasonLinks() throws Exception {
        int competitionId = id(request("POST", "/api/competitions", "{\"name\":\"Bundesliga\",\"type\":\"LEAGUE\"}"));
        int seasonId = id(request("POST", "/api/seasons", "{\"name\":\"2030/2031\"}"));
        assertThat(request("POST", "/api/seasons", "{\"name\":\"2030/2031\"}").statusCode()).isEqualTo(409);
        assertThat(request("POST", "/api/seasons", "{\"name\":\"2030/2033\"}").statusCode()).isEqualTo(400);
        String path = "/api/competition-seasons";
        String body = "{\"competitionId\":" + competitionId + ",\"seasonId\":" + seasonId + "}";
        HttpResponse<String> created = request("POST", path, body);
        assertThat(created.statusCode()).isEqualTo(201);
        int linkId = id(created);
        assertThat(JsonPath.<String>read(created.body(), "$.displayName")).isEqualTo("Bundesliga 2030/2031");
        assertThat(request("POST", path, body).statusCode()).isEqualTo(409);
        assertThat(request("POST", path, "{\"competitionId\":999999,\"seasonId\":" + seasonId + "}").statusCode())
                .isEqualTo(404);
        assertThat(request("GET", path + "?seasonId=" + seasonId, null).body()).contains("Bundesliga 2030/2031");
        assertThat(request("GET", path + "/" + linkId, null).statusCode()).isEqualTo(200);
        assertThat(request("PUT", "/api/seasons/" + seasonId, "{\"name\":\"2031/2032\"}").statusCode()).isEqualTo(409);
        assertThat(request("DELETE", "/api/seasons/" + seasonId, null).statusCode()).isEqualTo(409);
        assertThat(request("DELETE", "/api/competitions/" + competitionId, null).statusCode()).isEqualTo(409);
        assertThat(request("DELETE", path + "/" + linkId, null).statusCode()).isEqualTo(204);
        assertThat(request("DELETE", "/api/seasons/" + seasonId, null).statusCode()).isEqualTo(204);
        assertThat(request("DELETE", "/api/competitions/" + competitionId, null).statusCode()).isEqualTo(204);
    }

    @Test
    void searchSeasonsByNameWithServerPagination() throws Exception {
        int first = id(request("POST", "/api/seasons", "{\"name\":\"2190/2191\"}"));
        int second = id(request("POST", "/api/seasons", "{\"name\":\"2191/2192\"}"));
        try {
            HttpResponse<String> page = request("GET", "/api/seasons?name=%202191%20&size=1&page=0", null);
            assertThat(page.statusCode()).isEqualTo(200);
            assertThat(JsonPath.<Integer>read(page.body(), "$.page.totalElements")).isEqualTo(2);
            assertThat(JsonPath.<String>read(page.body(), "$.content[0].name")).isEqualTo("2191/2192");
            HttpResponse<String> next = request("GET", "/api/seasons?name=2191&size=1&page=1", null);
            assertThat(JsonPath.<String>read(next.body(), "$.content[0].name")).isEqualTo("2190/2191");
            assertThat(JsonPath.<Integer>read(request("GET", "/api/seasons?name=not-found", null).body(),
                    "$.page.totalElements")).isZero();
            for (String literal : new String[]{"%25", "_", "%5C"}) {
                assertThat(JsonPath.<Integer>read(request("GET", "/api/seasons?name=" + literal, null).body(),
                        "$.page.totalElements")).isZero();
            }
            int all = JsonPath.read(request("GET", "/api/seasons", null).body(), "$.page.totalElements");
            assertThat(JsonPath.<Integer>read(request("GET", "/api/seasons?name=%20%20", null).body(),
                    "$.page.totalElements")).isEqualTo(all);
        } finally {
            assertThat(request("DELETE", "/api/seasons/" + first, null).statusCode()).isEqualTo(204);
            assertThat(request("DELETE", "/api/seasons/" + second, null).statusCode()).isEqualTo(204);
        }
    }

    private int id(HttpResponse<String> response) {
        assertThat(response.statusCode()).isEqualTo(201);
        return JsonPath.read(response.body(), "$.id");
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10));
        if (body == null)
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        else
            builder.header("Content-Type", "application/json").method(method,
                    HttpRequest.BodyPublishers.ofString(body));
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
