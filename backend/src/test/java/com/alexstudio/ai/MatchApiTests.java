package com.alexstudio.ai;

import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import javax.sql.DataSource;
import com.jayway.jsonpath.JsonPath;
import com.alexstudio.ai.match.service.MatchService;
import com.alexstudio.ai.match.service.MatchStatusJob;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS, properties = "match.live-update-delay-ms=3600000")
@ActiveProfiles("test")
class MatchApiTests {
    @LocalServerPort
    private int port;
    @Autowired
    private MatchService service;
    @Autowired
    private MatchStatusJob job;
    @Autowired
    private DataSource dataSource;
    @MockitoBean
    private Clock clock;
    private static final Instant NOW = Instant.parse("2035-01-01T00:00:00Z");
    private final List<Integer> matches = new ArrayList<>();
    private int competition, season, link, home, away;
    private String prefix;
    @BeforeEach
    void setup() throws Exception {
        when(clock.instant()).thenReturn(NOW);
        prefix = "Match QA " + UUID.randomUUID();
        home = id(call("POST", "/api/teams",
                "{\"name\":\"" + prefix + " Home\",\"type\":\"CLUB\",\"logo\":\"/home.png\"}"));
        away = id(call("POST", "/api/teams", "{\"name\":\"" + prefix + " Away\",\"type\":\"CLUB\"}"));
        competition = id(call("POST", "/api/competitions", "{\"name\":\"" + prefix + " League\",\"type\":\"LEAGUE\"}"));
        // A dedicated year for this fixture; all records are removed after each test.
        season = id(call("POST", "/api/seasons", "{\"name\":\"2188/2189\"}"));
        link = id(call("POST", "/api/competition-seasons",
                "{\"competitionId\":" + competition + ",\"seasonId\":" + season + "}"));
    }
    @AfterEach
    void cleanup() throws Exception {
        for (int value : matches)
            call("DELETE", "/api/matches/" + value, null);
        call("DELETE", "/api/competition-seasons/" + link, null);
        call("DELETE", "/api/seasons/" + season, null);
        call("DELETE", "/api/competitions/" + competition, null);
        call("DELETE", "/api/teams/" + home, null);
        call("DELETE", "/api/teams/" + away, null);
    }
    @Test
    void crudDefaultFutureAndHistoricalScores() throws Exception {
        String future = body(null, NOW.plusSeconds(60), null, null);
        int match = create(future);
        var response = get(match);
        assertThat(read(response, "$.status")).isEqualTo("SCHEDULED");
        assertThat(read(response, "$.homeScore")).isNull();
        assertThat(read(response, "$.awayScore")).isNull();
        assertThat(read(response, "$.homeTeamName")).isEqualTo(prefix + " Home");
        assertThat(read(response, "$.homeTeamLogo")).isEqualTo("/home.png");
        assertThat(read(response, "$.competitionSeasonName")).isEqualTo(prefix + " League 2188/2189");
        assertThat(call("PUT", "/api/matches/" + match, body("FINISHED", NOW.minusSeconds(60), 0, 0)).statusCode())
                .isEqualTo(200);
        assertThat(read(get(match), "$.homeScore")).isEqualTo(0);
        assertThat(read(get(match), "$.status")).isEqualTo("FINISHED");
        int historical = create(body("FINISHED", NOW.minusSeconds(120), 2, 1));
        assertThat(read(get(historical), "$.awayScore")).isEqualTo(1);
        assertThat(call("DELETE", "/api/matches/" + historical, null).statusCode()).isEqualTo(204);
        assertThat(call("GET", "/api/matches/" + historical, null).statusCode()).isEqualTo(404);
        assertThat(call("PUT", "/api/matches/" + historical, future).statusCode()).isEqualTo(404);
        assertThat(call("DELETE", "/api/matches/" + historical, null).statusCode()).isEqualTo(404);
    }
    @Test
    void vietnamOffsetPreservesInstantAndFilters() throws Exception {
        String vietnamTime = "2035-01-01T20:00:00+07:00";
        int match = create(body("SCHEDULED", Instant.parse("2035-01-01T13:00:00Z"), null, null)
                .replace("2035-01-01T13:00:00Z", vietnamTime));
        String result = (String) read(get(match), "$.kickoffTime");
        assertThat(result).endsWith("+07:00");
        assertThat(result).startsWith("2035-01-01T20:00:00");
        assertThat(Instant.parse(result)).isEqualTo(Instant.parse("2035-01-01T13:00:00Z"));
        assertThat(total(
                "competitionSeasonId=" + link + "&from=2035-01-01T20:00:00%2B07:00&to=2035-01-01T20:00:00%2B07:00"))
                .isEqualTo(1);
        assertThat(total("competitionSeasonId=" + link + "&from=2035-01-01T13:00:00Z&to=2035-01-01T13:00:00Z"))
                .isEqualTo(1);
    }
    @Test
    void automaticLiveIsConditionalRepeatableAndUsesLatestKickoff() throws Exception {
        int due = create(body("SCHEDULED", NOW.plusSeconds(1), null, null));
        int moved = create(body("SCHEDULED", NOW.plusSeconds(1), null, null));
        call("PUT", "/api/matches/" + moved, body("SCHEDULED", NOW.plusSeconds(600), null, null));
        int live = create(body("LIVE", NOW.minusSeconds(20), null, null));
        int finished = create(body("FINISHED", NOW.minusSeconds(20), 0, 0));
        int postponed = create(body("POSTPONED", NOW.minusSeconds(20), null, null));
        int cancelled = create(body("CANCELLED", NOW.minusSeconds(20), null, null));
        int past = create(body("SCHEDULED", NOW.minusSeconds(1), 7, 8));
        assertThat(read(get(past), "$.status")).isEqualTo("LIVE");
        assertThat(read(get(past), "$.homeScore")).isNull();
        assertThat(service.promoteDue()).isZero();
        when(clock.instant()).thenReturn(NOW.plusSeconds(1));
        job.run();
        assertThat(read(get(due), "$.status")).isEqualTo("LIVE");
        assertThat(read(get(due), "$.homeScore")).isNull();
        assertThat(read(get(due), "$.awayScore")).isNull();
        assertThat(service.promoteDue()).isZero();
        assertThat(read(get(moved), "$.status")).isEqualTo("SCHEDULED");
        assertThat(read(get(live), "$.status")).isEqualTo("LIVE");
        assertThat(read(get(finished), "$.status")).isEqualTo("FINISHED");
        assertThat(read(get(postponed), "$.status")).isEqualTo("POSTPONED");
        assertThat(read(get(cancelled), "$.status")).isEqualTo("CANCELLED");
        assertThat(call("PUT", "/api/matches/" + moved, body("SCHEDULED", NOW, null, null)).statusCode())
                .isEqualTo(200);
        assertThat(read(get(moved), "$.status")).isEqualTo("LIVE");
    }
    @Test
    void statusScoreRulesAndInputValidation() throws Exception {
        int live = create(body("LIVE", NOW, null, null));
        assertThat(call("PUT", "/api/matches/" + live, body(null, NOW, null, null)).statusCode()).isEqualTo(400);
        assertThat(call("PUT", "/api/matches/" + live, body("LIVE", NOW, 1, 0)).statusCode()).isEqualTo(200);
        for (String status : List.of("SCHEDULED", "POSTPONED", "CANCELLED")) {
            var result = call("PUT", "/api/matches/" + live, body(status, NOW.plusSeconds(60), 3, 2));
            assertThat(result.statusCode()).isEqualTo(200);
            assertThat(read(result, "$.homeScore")).isNull();
            assertThat(read(result, "$.awayScore")).isNull();
        }
        for (String invalid : List.of(body("LIVE", NOW, 1, null), body("FINISHED", NOW, null, null),
                body("LIVE", NOW, -1, 0), body("UNKNOWN", NOW, null, null),
                body("LIVE", NOW, null, null).replace("\"homeScore\":null", "\"homeScore\":1.5")
                        .replace("\"awayScore\":null", "\"awayScore\":0"),
                body("LIVE", NOW, null, null).replace("\"homeScore\":null", "\"homeScore\":2147483648"),
                body("SCHEDULED", NOW, null, null).replace("\"awayTeamId\":" + away, "\"awayTeamId\":" + home),
                body("SCHEDULED", NOW, null, null).replace("\"kickoffTime\":\"" + NOW + "\"", "\"kickoffTime\":null"),
                "{}")) {
            assertThat(call("POST", "/api/matches", invalid).statusCode()).as(invalid).isEqualTo(400);
        }
        for (String path : List.of("/api/matches/0", "/api/matches?page=-1", "/api/matches?size=0",
                "/api/matches?size=101", "/api/matches?teamId=0", "/api/matches?status=OTHER",
                "/api/matches?direction=OTHER", "/api/matches?from=bad",
                "/api/matches?from=2036-01-01T00:00:00Z&to=2035-01-01T00:00:00Z"))
            assertThat(call("GET", path, null).statusCode()).as(path).isEqualTo(400);
        assertThat(call("POST", "/api/matches",
                body("LIVE", NOW, null, null).replace("\"homeTeamId\":" + home, "\"homeTeamId\":999999999"))
                .statusCode()).isEqualTo(404);
        assertThat(call("POST", "/api/matches", body("LIVE", NOW, null, null).replace("\"competitionSeasonId\":" + link,
                "\"competitionSeasonId\":999999999")).statusCode()).isEqualTo(404);
    }
    @Test
    void searchFiltersSortingAndPagination() throws Exception {
        int earlier = create(body("FINISHED", NOW.minusSeconds(60), 1, 0));
        int later = create(body("FINISHED", NOW, 0, 0));
        int tie = create(body("FINISHED", NOW, 0, 2));
        int reverse = create(body("FINISHED", NOW.minusSeconds(120), 1, 2).replace(
                "\"homeTeamId\":" + home + ",\"awayTeamId\":" + away,
                "\"homeTeamId\":" + away + ",\"awayTeamId\":" + home));
        String path = "/api/matches?competitionSeasonId=" + link + "&teamId=" + home + "&status=FINISHED&from="
                + NOW.minusSeconds(60) + "&to=" + NOW + "&size=1";
        var page = call("GET", path, null);
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(read(page, "$.page.totalElements")).isEqualTo(3);
        assertThat(read(page, "$.content[0].id")).isEqualTo(tie);
        assertThat(read(call("GET", path + "&page=1", null), "$.content[0].id")).isEqualTo(later);
        assertThat(read(call("GET", path + "&direction=ASC", null), "$.content[0].id")).isEqualTo(earlier);
        assertThat(total("competitionSeasonId=" + link + "&name=HOME")).isEqualTo(4);
        assertThat(total("competitionSeasonId=" + link + "&name=away")).isEqualTo(4);
        assertThat(total("competitionSeasonId=" + link + "&homeTeamId=" + home)).isEqualTo(3);
        assertThat(total("competitionSeasonId=" + link + "&awayTeamId=" + home)).isEqualTo(1);
        assertThat(total("competitionSeasonId=" + link + "&teamId=" + home)).isEqualTo(4);
        assertThat(total("competitionSeasonId=" + link + "&status=LIVE")).isZero();
        assertThat(total("name=%25")).isZero();
        assertThat(total("name=not-a-team-" + UUID.randomUUID())).isZero();
    }
    @Test
    void foreignKeysAndChecksProtectData() throws Exception {
        int match = create(body("FINISHED", NOW, 0, 0));
        assertThat(call("DELETE", "/api/teams/" + home, null).statusCode()).isEqualTo(409);
        assertThat(call("DELETE", "/api/teams/" + away, null).statusCode()).isEqualTo(409);
        assertThat(call("DELETE", "/api/competition-seasons/" + link, null).statusCode()).isEqualTo(409);
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            String base = "INSERT INTO matches(competition_season_id,home_team_id,away_team_id,kickoff_time,status,home_score,away_score) VALUES (";
            for (String values : List.of(link + "," + home + "," + home + ",now(),'LIVE',null,null",
                    link + "," + home + "," + away + ",now(),'FINISHED',null,null",
                    link + "," + home + "," + away + ",now(),'LIVE',1,null",
                    link + "," + home + "," + away + ",now(),'LIVE',-1,0",
                    link + "," + home + "," + away + ",now(),'SCHEDULED',0,0",
                    "999999999," + home + "," + away + ",now(),'LIVE',null,null",
                    link + ",999999999," + away + ",now(),'LIVE',null,null")) {
                assertThatThrownBy(() -> statement.executeUpdate(base + values + ")"))
                        .isInstanceOf(java.sql.SQLException.class);
            }
        }
        assertThat(read(get(match), "$.homeScore")).isEqualTo(0);
    }
    private int total(String query) throws Exception {
        var response = call("GET", "/api/matches?" + query, null);
        assertThat(response.statusCode()).isEqualTo(200);
        return JsonPath.read(response.body(), "$.page.totalElements");
    }
    private int create(String body) throws Exception {
        int id = id(call("POST", "/api/matches", body));
        matches.add(id);
        return id;
    }
    private String body(String status, Instant kickoff, Integer homeScore, Integer awayScore) {
        return "{\"competitionSeasonId\":" + link + ",\"homeTeamId\":" + home + ",\"awayTeamId\":" + away
                + ",\"kickoffTime\":\"" + kickoff + "\",\"status\":" + (status == null ? "null" : "\"" + status + "\"")
                + ",\"homeScore\":" + homeScore + ",\"awayScore\":" + awayScore + ",\"round\":\" Round 1 \"}";
    }
    private HttpResponse<String> get(int id) throws Exception {
        return call("GET", "/api/matches/" + id, null);
    }
    private Object read(HttpResponse<String> response, String path) {
        return JsonPath.read(response.body(), path);
    }
    private int id(HttpResponse<String> response) {
        assertThat(response.statusCode()).withFailMessage(response.body()).isEqualTo(201);
        return JsonPath.read(response.body(), "$.id");
    }
    private HttpResponse<String> call(String method, String path, String body) throws Exception {
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
