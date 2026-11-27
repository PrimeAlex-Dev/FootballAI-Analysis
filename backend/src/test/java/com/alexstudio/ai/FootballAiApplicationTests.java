package com.alexstudio.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

import com.jayway.jsonpath.JsonPath;
import com.alexstudio.ai.common.config.DataInitializer;
import com.alexstudio.ai.match.repository.MatchRepository;
import com.alexstudio.ai.match.service.MatchService;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@ActiveProfiles("test")
class FootballAiApplicationTests {

    private final EntityManagerFactory entityManagerFactory;
    private final Flyway flyway;
    private final DataInitializer dataInitializer;
    private final MatchRepository matchRepository;
    private final MatchService matchService;

    @Autowired
    FootballAiApplicationTests(EntityManagerFactory entityManagerFactory, Flyway flyway,
            DataInitializer dataInitializer, MatchRepository matchRepository, MatchService matchService) {
        this.entityManagerFactory = entityManagerFactory;
        this.flyway = flyway;
        this.dataInitializer = dataInitializer;
        this.matchRepository = matchRepository;
        this.matchService = matchService;
    }

    @LocalServerPort
    private int port;

    @Test
    void jpaConnectsToPostgresql() {
        try (var entityManager = entityManagerFactory.createEntityManager()) {
            Object result = entityManager.createNativeQuery("SELECT 1").getSingleResult();

            assertThat(((Number) result).intValue()).isEqualTo(1);
            assertThat(entityManager.createNativeQuery("SELECT current_database()").getSingleResult())
                    .isEqualTo("football_ai_test");
        }
    }

    @Test
    void flywayHistoryIsInitializedAndMigrationsAreValid() throws Exception {
        flyway.validate();

        try (var connection = flyway.getConfiguration().getDataSource().getConnection();
                var statement = connection.createStatement();
                var result = statement.executeQuery("SELECT to_regclass('public.flyway_schema_history') IS NOT NULL")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getBoolean(1)).isTrue();
        }

        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void applicationStartsAndHealthEndpointReturnsUp() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/actuator/health"))
                .timeout(Duration.ofSeconds(10)).GET().build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            String status = JsonPath.read(response.body(), "$.status");
            assertThat(status).isEqualTo("UP");
        }
    }

    @Test
    void openApiAndSwaggerUiAreAvailable() throws Exception {
        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
            HttpRequest docsRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/v3/api-docs")).timeout(Duration.ofSeconds(10)).GET()
                    .build();
            HttpResponse<String> docs = client.send(docsRequest, HttpResponse.BodyHandlers.ofString());
            assertThat(docs.statusCode()).isEqualTo(200);
            assertThat(docs.body()).contains("/api/competitions");
            assertThat(docs.body()).doesNotContain("/actuator/health");

            HttpRequest uiRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/swagger-ui.html")).timeout(Duration.ofSeconds(10))
                    .GET().build();
            HttpResponse<String> ui = client.send(uiRequest, HttpResponse.BodyHandlers.ofString());
            assertThat(ui.statusCode()).isEqualTo(200);
            assertThat(ui.body()).contains("swagger-ui");
        }
    }

    @Test
    void sampleMatchHasFormAndHeadToHeadWithoutDuplicateSeeds() {
        long count = matchRepository.count();
        dataInitializer.run();
        assertThat(matchRepository.count()).isEqualTo(count);

        Instant kickoff = Instant.parse("2026-11-27T16:30:00Z");
        var sample = matchRepository.findAll().stream().filter(match -> match.getKickoffTime().equals(kickoff))
                .filter(match -> match.getHomeScore() == null && match.getAwayScore() == null).findFirst()
                .orElseThrow();
        var form = matchService.recentForm(sample.getId());
        var headToHead = matchService.headToHead(sample.getId(), 0, 5);
        assertThat(form.home()).hasSize(8);
        assertThat(form.away()).hasSize(8);
        assertThat(headToHead.matches().getMetadata().totalElements()).isEqualTo(6);
        assertThat(headToHead.matches().getContent()).hasSize(5);
    }
}
