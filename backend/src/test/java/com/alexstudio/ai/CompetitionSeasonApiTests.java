package com.alexstudio.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.alexstudio.ai.competition.repository.CompetitionRepository;
import com.alexstudio.ai.season.repository.SeasonRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@ActiveProfiles("test")
class CompetitionSeasonApiTests {

    @LocalServerPort
    private int port;

    @Autowired
    private CompetitionRepository competitions;

    @Autowired
    private SeasonRepository seasons;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void clearData() {
        seasons.deleteAll();
        competitions.deleteAll();
    }

    @Test
    void competitionCrudSearchAndValidation() throws Exception {
        HttpResponse<String> created = request("POST", "/api/competitions",
                "{\"name\":\" Premier League \",\"type\":\"LEAGUE\",\"logo\":\" logo.png \"}");
        assertThat(created.statusCode()).isEqualTo(201);
        int id = JsonPath.read(created.body(), "$.id");
        assertThat(JsonPath.<String>read(created.body(), "$.name")).isEqualTo("Premier League");
        assertThat(JsonPath.<String>read(created.body(), "$.logo")).isEqualTo("logo.png");

        assertThat(request("GET", "/api/competitions/" + id, null).statusCode()).isEqualTo(200);
        assertThat(request("GET", "/api/competitions/999999", null).statusCode()).isEqualTo(404);
        assertThat(request("GET", "/api/competitions/-1", null).statusCode()).isEqualTo(400);
        assertThat(request("GET", "/api/competitions/no-id", null).statusCode()).isEqualTo(400);
        assertThat(request("POST", "/api/competitions", "{\"name\":\"   \",\"type\":\"CUP\"}").statusCode())
                .isEqualTo(400);
        assertThat(request("POST", "/api/competitions", "{\"name\":\"Cup\",\"type\":\"OTHER\"}").statusCode())
                .isEqualTo(400);
        assertThat(request("GET", "/api/competitions?name=premier&type=LEAGUE&page=0&size=1", null).body())
                .contains("Premier League", "totalElements");
        assertThat(JsonPath.<Integer>read(request("GET", "/api/competitions?name=%25", null).body(),
                "$.page.totalElements")).isZero();

        HttpResponse<String> updated = request("PUT", "/api/competitions/" + id,
                "{\"name\":\" FA Cup \",\"type\":\"CUP\",\"logo\":null}");
        assertThat(updated.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<String>read(updated.body(), "$.name")).isEqualTo("FA Cup");
        HttpResponse<String> deleted = request("DELETE", "/api/competitions/" + id, null);
        assertThat(deleted.statusCode()).withFailMessage(deleted.body()).isEqualTo(204);
    }

    @Test
    void seasonsBelongToCompetitionAndPreventDuplicateOrParentDeletion() throws Exception {
        int first = createCompetition("Premier League");
        int second = createCompetition("La Liga");
        String firstPath = "/api/competitions/" + first + "/seasons";
        String secondPath = "/api/competitions/" + second + "/seasons";

        assertThat(request("POST", "/api/competitions/999999/seasons", "{\"name\":\"2026\"}").statusCode())
                .isEqualTo(404);
        assertThat(request("POST", firstPath, "{\"name\":\"abc\"}").statusCode()).isEqualTo(400);
        assertThat(request("POST", firstPath, "{\"name\":\"2026/2029\"}").statusCode()).isEqualTo(400);
        assertThat(request("POST", firstPath, "{\"name\":\"   \"}").statusCode()).isEqualTo(400);

        HttpResponse<String> created = request("POST", firstPath, "{\"name\":\" 2026/2027 \"}");
        assertThat(created.statusCode()).withFailMessage(created.body()).isEqualTo(201);
        int seasonId = JsonPath.read(created.body(), "$.id");
        assertThat(JsonPath.<Integer>read(created.body(), "$.competitionId")).isEqualTo(first);
        assertThat(JsonPath.<String>read(created.body(), "$.name")).isEqualTo("2026/2027");

        assertThat(request("POST", firstPath, "{\"name\":\"2026/2027\"}").statusCode()).isEqualTo(409);
        assertThat(request("GET", "/api/competitions/-1/seasons", null).statusCode()).isEqualTo(400);
        assertThat(request("POST", secondPath, "{\"name\":\"2026/2027\"}").statusCode()).isEqualTo(201);
        assertThat(request("GET", firstPath + "/" + seasonId, null).statusCode()).isEqualTo(200);
        assertThat(request("GET", secondPath + "/" + seasonId, null).statusCode()).isEqualTo(404);
        assertThat(request("GET", firstPath + "?page=0&size=1", null).body()).contains("2026/2027");
        assertThat(request("DELETE", "/api/competitions/" + first, null).statusCode()).isEqualTo(409);

        HttpResponse<String> anotherSeason = request("POST", firstPath, "{\"name\":\"2025/2026\"}");
        assertThat(anotherSeason.statusCode()).isEqualTo(201);
        assertThat(request("PUT", firstPath + "/" + seasonId, "{\"name\":\"2025/2026\"}").statusCode()).isEqualTo(409);

        HttpResponse<String> updated = request("PUT", firstPath + "/" + seasonId, "{\"name\":\"2027\"}");
        assertThat(updated.statusCode()).isEqualTo(200);
        assertThat(JsonPath.<String>read(updated.body(), "$.name")).isEqualTo("2027");
        assertThat(request("DELETE", firstPath + "/" + seasonId, null).statusCode()).isEqualTo(204);
        int anotherSeasonId = JsonPath.read(anotherSeason.body(), "$.id");
        assertThat(request("DELETE", firstPath + "/" + anotherSeasonId, null).statusCode()).isEqualTo(204);
        assertThat(request("DELETE", "/api/competitions/" + first, null).statusCode()).isEqualTo(204);
    }

    @Test
    void databaseRejectsDuplicateAndOrphanSeasons() throws Exception {
        int id = createCompetition("FIFA World Cup");
        jdbc.update("INSERT INTO seasons (competition_id, name) VALUES (?, ?)", id, "2026");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO seasons (competition_id, name) VALUES (?, ?)", id, "2026"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(
                () -> jdbc.update("INSERT INTO seasons (competition_id, name) VALUES (?, ?)", 999999, "2022"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("DELETE FROM competitions WHERE id = ?", id))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    private int createCompetition(String name) throws Exception {
        HttpResponse<String> response = request("POST", "/api/competitions",
                "{\"name\":\"" + name + "\",\"type\":\"LEAGUE\"}");
        assertThat(response.statusCode()).isEqualTo(201);
        return JsonPath.read(response.body(), "$.id");
    }

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10));
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json").method(method,
                    HttpRequest.BodyPublishers.ofString(body));
        }
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
