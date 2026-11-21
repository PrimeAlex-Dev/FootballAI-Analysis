package com.alexstudio.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
class UniqueNameApiTests {
    @LocalServerPort
    private int port;
    @Autowired
    private DataSource dataSource;

    @Test
    void teamNamesAreUniqueAcrossTypes() throws Exception {
        verifyUniqueNames("teams", "CLUB", "NATIONAL");
    }
    @Test
    void competitionNamesAreUniqueAcrossTypes() throws Exception {
        verifyUniqueNames("competitions", "LEAGUE", "CUP");
    }
    private void verifyUniqueNames(String table, String firstType, String otherType) throws Exception {
        String name = "Unique " + UUID.randomUUID();
        String other = name + " other";
        int first = create(table, name, firstType);
        int second = create(table, other, otherType);
        try {
            assertThat(call("POST", table, null, "  " + name.toUpperCase(java.util.Locale.ROOT) + "  ", otherType)
                    .statusCode()).isEqualTo(409);
            assertThat(call("POST", table, null, name, firstType).statusCode()).isEqualTo(409);
            assertThat(call("PUT", table, second, name, otherType).statusCode()).isEqualTo(409);
            assertThat(call("PUT", table, second, " " + name.toLowerCase(java.util.Locale.ROOT) + " ", firstType)
                    .statusCode()).isEqualTo(409);
            assertThat(call("GET", table, second, null, null).body()).contains(other);
            assertThat(call("PUT", table, first, name, otherType).statusCode()).isEqualTo(200);
            assertThat(call("PUT", table, first, name.toUpperCase(java.util.Locale.ROOT), firstType).statusCode())
                    .isEqualTo(200);
            try (var connection = dataSource.getConnection();
                    var statement = connection.prepareStatement("INSERT INTO " + table + " (name,type) VALUES (?,?)")) {
                statement.setString(1, name.toLowerCase(java.util.Locale.ROOT));
                statement.setString(2, otherType);
                assertThatThrownBy(statement::executeUpdate).isInstanceOf(java.sql.SQLException.class).satisfies(
                        error -> assertThat(((java.sql.SQLException) error).getSQLState()).isEqualTo("23505"));
            }
        } finally {
            assertThat(call("DELETE", table, first, null, null).statusCode()).isEqualTo(204);
            assertThat(call("DELETE", table, second, null, null).statusCode()).isEqualTo(204);
        }
    }
    private int create(String table, String name, String type) throws Exception {
        var response = call("POST", table, null, name, type);
        assertThat(response.statusCode()).isEqualTo(201);
        return JsonPath.read(response.body(), "$.id");
    }
    private HttpResponse<String> call(String method, String table, Integer id, String name, String type)
            throws Exception {
        var builder = HttpRequest
                .newBuilder(URI.create("http://localhost:" + port + "/api/" + table + (id == null ? "" : "/" + id)))
                .timeout(java.time.Duration.ofSeconds(10));
        if (name == null)
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        else
            builder.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers
                    .ofString("{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"logo\":null}"));
        try (var client = HttpClient.newHttpClient()) {
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
