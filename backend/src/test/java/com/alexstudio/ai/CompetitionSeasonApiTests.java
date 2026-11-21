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
