package com.alexstudio.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
class FootballAiApplicationTests {

    private final EntityManagerFactory entityManagerFactory;
    private final Flyway flyway;

    @Autowired
    FootballAiApplicationTests(EntityManagerFactory entityManagerFactory, Flyway flyway) {
        this.entityManagerFactory = entityManagerFactory;
        this.flyway = flyway;
    }

    @LocalServerPort
    private int port;

    @Test
    void jpaConnectsToPostgresql() {
        try (var entityManager = entityManagerFactory.createEntityManager()) {
            Object result = entityManager.createNativeQuery("SELECT 1").getSingleResult();

            assertThat(((Number) result).intValue()).isEqualTo(1);
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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/actuator/health"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            String status = JsonPath.read(response.body(), "$.status");
            assertThat(status).isEqualTo("UP");
        }
    }
}
