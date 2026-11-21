package com.alexstudio.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@ActiveProfiles("test")
class TeamApiTests {
    @LocalServerPort
    private int port;
    @Autowired
    private DataSource dataSource;

    @Test
    void crudAndNotFound() throws Exception {
        int id = create("  Team CRUD  ", "CLUB");
        try {
            HttpResponse<String> get = request("GET", "/api/teams/" + id, null);
            assertThat(get.statusCode()).isEqualTo(200);
            assertThat(JsonPath.<String>read(get.body(), "$.name")).isEqualTo("Team CRUD");
            assertThat(JsonPath.<Object>read(get.body(), "$.logo")).isNull();
            HttpResponse<String> updated = request("PUT", "/api/teams/" + id,
                    "{\"name\":\"Renamed Team\",\"type\":\"NATIONAL\",\"logo\":\" /logos/team.png \"}");
            assertThat(updated.statusCode()).isEqualTo(200);
            assertThat(JsonPath.<String>read(updated.body(), "$.type")).isEqualTo("NATIONAL");
            assertThat(JsonPath.<String>read(updated.body(), "$.logo")).isEqualTo("/logos/team.png");
            assertThat(request("GET", "/api/teams/" + id, null).body()).contains("Renamed Team");
        } finally {
            assertThat(request("DELETE", "/api/teams/" + id, null).statusCode()).isEqualTo(204);
        }
        assertThat(request("GET", "/api/teams/" + id, null).statusCode()).isEqualTo(404);
        assertThat(request("PUT", "/api/teams/" + id, "{\"name\":\"Missing\",\"type\":\"CLUB\"}").statusCode())
                .isEqualTo(404);
        assertThat(request("DELETE", "/api/teams/" + id, null).statusCode()).isEqualTo(404);
    }

    @Test
    void searchFilterPaginationAndStableSorting() throws Exception {
        String prefix = "qa" + UUID.randomUUID().toString().replace("-", "");
        List<Integer> ids = new ArrayList<>();
        try {
            ids.add(create(prefix + " Beta", "CLUB"));
            ids.add(create(prefix + " Alpha", "CLUB"));
            ids.add(create(prefix + " Alpha2", "CLUB"));
            ids.add(create(prefix + " National", "NATIONAL"));
            String path = "/api/teams?name=%20" + prefix.toUpperCase(java.util.Locale.ROOT) + "%20&type=CLUB&size=1";
            HttpResponse<String> first = request("GET", path + "&page=0", null);
            assertThat(first.statusCode()).isEqualTo(200);
            assertThat(JsonPath.<Integer>read(first.body(), "$.page.totalElements")).isEqualTo(3);
            assertThat(JsonPath.<Integer>read(first.body(), "$.page.totalPages")).isEqualTo(3);
            assertThat(JsonPath.<Integer>read(first.body(), "$.content[0].id")).isEqualTo(ids.get(1));
            assertThat(JsonPath.<Integer>read(request("GET", path + "&page=1", null).body(), "$.content[0].id"))
                    .isEqualTo(ids.get(2));
            assertThat(JsonPath.<String>read(request("GET", path + "&page=2", null).body(), "$.content[0].name"))
                    .endsWith("Beta");
            assertThat(total("name=" + prefix + "&type=NATIONAL")).isEqualTo(1);
            assertThat(total("name=" + prefix)).isEqualTo(4);
            assertThat(total("name=missing" + prefix)).isZero();
            for (String name : new String[]{"%25", "_", "%5C"}) {
                assertThat(total("name=" + prefix + name)).isZero();
            }
            assertThat(total("name=%20%20")).isEqualTo(total(""));
        } finally {
            for (int id : ids)
                request("DELETE", "/api/teams/" + id, null);
        }
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        for (String body : new String[]{"{}", "{\"name\":\"  \u005ct \" ,\"type\":\"CLUB\"}", "{\"name\":\"A\"}",
                "{\"name\":\"A\",\"type\":\"WOMEN\"}", "{\"name\":null,\"type\":\"CLUB\"}"}) {
            HttpResponse<String> result = request("POST", "/api/teams", body);
            assertThat(result.statusCode()).isEqualTo(400);
            assertThat(result.headers().firstValue("content-type").orElse("")).contains("application/problem+json");
        }
        for (String path : new String[]{"/api/teams/0", "/api/teams/-1", "/api/teams/not-a-number",
                "/api/teams?page=-1", "/api/teams?size=0", "/api/teams?size=101", "/api/teams?size=no",
                "/api/teams?type=WOMEN"}) {
            assertThat(request("GET", path, null).statusCode()).as(path).isEqualTo(400);
        }
        int id = create("Valid input test", "CLUB");
        try {
            assertThat(request("PUT", "/api/teams/" + id, "{\"name\":\" \",\"type\":\"CLUB\"}").statusCode())
                    .isEqualTo(400);
            assertThat(request("GET", "/api/teams/" + id, null).body()).contains("Valid input test");
        } finally {
            request("DELETE", "/api/teams/" + id, null);
        }
    }

    @Test
    void databaseConstraintsAndReferencedDeleteAreEnforced() throws Exception {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            assertThatThrownBy(() -> statement.executeUpdate("INSERT INTO teams(name,type) VALUES (NULL,'CLUB')"))
                    .isInstanceOf(java.sql.SQLException.class);
            assertThatThrownBy(() -> statement.executeUpdate("INSERT INTO teams(name,type) VALUES ('','CLUB')"))
                    .isInstanceOf(java.sql.SQLException.class);
            assertThatThrownBy(() -> statement.executeUpdate("INSERT INTO teams(name,type) VALUES ('A',NULL)"))
                    .isInstanceOf(java.sql.SQLException.class);
            assertThatThrownBy(() -> statement.executeUpdate("INSERT INTO teams(name,type) VALUES ('A','WOMEN')"))
                    .isInstanceOf(java.sql.SQLException.class);
            statement.execute(
                    "CREATE TABLE team_reference_test (team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE RESTRICT)");
            int id = create("Referenced team", "CLUB");
            try {
                statement.executeUpdate("INSERT INTO team_reference_test(team_id) VALUES (" + id + ")");
                HttpResponse<String> result = request("DELETE", "/api/teams/" + id, null);
                assertThat(result.statusCode()).isEqualTo(409);
                assertThat(result.body()).contains("Team has dependent data");
                assertThat(request("GET", "/api/teams/" + id, null).statusCode()).isEqualTo(200);
                assertThat(statement.executeQuery("SELECT count(*) FROM team_reference_test").next()).isTrue();
            } finally {
                statement.execute("DROP TABLE team_reference_test");
                request("DELETE", "/api/teams/" + id, null);
            }
        }
    }

    private int total(String query) throws Exception {
        HttpResponse<String> result = request("GET", "/api/teams?" + query, null);
        assertThat(result.statusCode()).isEqualTo(200);
        return JsonPath.read(result.body(), "$.page.totalElements");
    }
    private int create(String name, String type) throws Exception {
        HttpResponse<String> result = request("POST", "/api/teams",
                "{\"name\":\"" + name + "\",\"type\":\"" + type + "\"}");
        assertThat(result.statusCode()).isEqualTo(201);
        return JsonPath.read(result.body(), "$.id");
    }
    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10));
        if (body == null)
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        else
            builder.header("Content-Type", "application/json").method(method,
                    HttpRequest.BodyPublishers.ofString(body));
        try (var client = HttpClient.newHttpClient()) {
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
