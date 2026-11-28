package com.alexstudio.ai;
import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@ActiveProfiles("test")
class StandingApiTests {
    @LocalServerPort
    int port;
    @Autowired
    JdbcTemplate jdbc;
    long home, away, competition, season, link;
    @BeforeEach
    void setup() {
        String prefix = "Standing QA " + UUID.randomUUID();
        home = jdbc.queryForObject("insert into teams(name,type) values (?, 'CLUB') returning id", Long.class,
                prefix + " Home");
        away = jdbc.queryForObject("insert into teams(name,type) values (?, 'CLUB') returning id", Long.class,
                prefix + " Away");
        competition = jdbc.queryForObject("insert into competitions(name,type) values (?, 'LEAGUE') returning id",
                Long.class, prefix + " League");
        season = jdbc.queryForObject("insert into seasons(name) values ('2192/2193') returning id", Long.class);
        link = jdbc.queryForObject(
                "insert into competition_seasons(competition_id,season_id) values (?,?) returning id", Long.class,
                competition, season);
    }
    @AfterEach
    void cleanup() {
        jdbc.update(
                "delete from standing_rows where standing_id in (select id from standings where competition_season_id=?)",
                link);
        jdbc.update("delete from standings where competition_season_id=?", link);
        jdbc.update("delete from competition_seasons where id=?", link);
        jdbc.update("delete from seasons where id=?", season);
        jdbc.update("delete from competitions where id=?", competition);
        jdbc.update("delete from teams where id in (?,?)", home, away);
    }
    @Test
    void importPreviewUpdateAndDelete() throws Exception {
        String base = "/api/competition-seasons/" + link + "/standing";
        String first = "{\"tables\":[[{\"rank\":1,\"team\":\"Alias Home\",\"matchesPlayed\":11,\"wins\":9,\"draws\":1,\"losses\":1,\"goalsFor\":17,\"goalsAgainst\":7,\"goalDifference\":10,\"points\":28}]]}";
        assertThat(call("GET", base, null).statusCode()).isEqualTo(404);
        assertThat(call("POST", base + "/preview", first).body()).contains("\"teamId\":null");
        assertThat(call("POST", base, first).statusCode()).isEqualTo(400);
        String mapped = first.substring(0, first.length() - 1)
                + ",\"mappings\":[{\"tableIndex\":0,\"rowIndex\":0,\"teamId\":" + home + "}]}";
        assertThat(call("POST", base, mapped).statusCode()).isEqualTo(201);
        assertThat(call("POST", base, mapped).statusCode()).isEqualTo(409);
        assertThat(call("GET", base, null).body()).contains("\"points\":28");
        assertThat(call("GET", "/api/standings?competitionSeasonId=" + link + "&page=0&size=10", null).body())
                .contains("\"totalElements\":1");
        String second = "{\"tables\":[[{\"rank\":1,\"team\":\""
                + jdbc.queryForObject("select name from teams where id=?", String.class, away)
                + "\",\"matchesPlayed\":1,\"wins\":0,\"draws\":0,\"losses\":1,\"goalsFor\":0,\"goalsAgainst\":2,\"goalDifference\":-2,\"points\":-1}]]}";
        assertThat(call("PUT", base, second).statusCode()).isEqualTo(200);
        assertThat(call("GET", base, null).body()).contains("\"points\":-1").doesNotContain("\"points\":28");
        assertThat(call("PUT", base, "{\"tables\":[]}").statusCode()).isEqualTo(400);
        assertThat(call("GET", base, null).body()).contains("\"points\":-1");
        assertThat(call("DELETE", base, null).statusCode()).isEqualTo(204);
        assertThat(call("GET", base, null).statusCode()).isEqualTo(404);
    }
    @Test
    void multipleTablesKeepOrderAndProtectReferences() throws Exception {
        String base = "/api/competition-seasons/" + link + "/standing";
        String firstName = jdbc.queryForObject("select name from teams where id=?", String.class, home);
        String secondName = jdbc.queryForObject("select name from teams where id=?", String.class, away);
        String first = row(firstName, 1, 7);
        String second = row(secondName, 1, 5);
        String input = "{\"tables\":[[" + first + "],[" + second + "]]}";
        assertThat(call("POST", base, input).statusCode()).isEqualTo(201);
        assertThat(call("GET", base, null).body()).contains("\"sourceTeam\":\"" + firstName + "\"");
        assertThat(jdbc.queryForObject(
                "select count(*) from standing_rows where standing_id=(select id from standings where competition_season_id=?)",
                Integer.class, link)).isEqualTo(2);
        assertThat(call("DELETE", "/api/teams/" + home, null).statusCode()).isEqualTo(409);
        assertThat(call("DELETE", "/api/competition-seasons/" + link, null).statusCode()).isEqualTo(409);
        assertThat(call("PUT", base, "{\"tables\":[[" + first + "," + first + "]]}").statusCode()).isEqualTo(400);
        assertThat(jdbc.queryForObject(
                "select count(*) from standing_rows where standing_id=(select id from standings where competition_season_id=?)",
                Integer.class, link)).isEqualTo(2);
    }
    private String row(String name, int rank, int points) {
        return "{\"rank\":" + rank + ",\"team\":\"" + name
                + "\",\"matchesPlayed\":1,\"wins\":0,\"draws\":0,\"losses\":1,\"goalsFor\":0,\"goalsAgainst\":1,\"goalDifference\":-1,\"points\":"
                + points + "}";
    }
    private HttpResponse<String> call(String method, String path, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        request.method(method,
                body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
