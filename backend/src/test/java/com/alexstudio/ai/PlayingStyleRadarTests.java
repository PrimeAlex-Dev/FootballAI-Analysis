package com.alexstudio.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@ActiveProfiles("test")
class PlayingStyleRadarTests {
    @LocalServerPort
    int port;
    @Autowired
    JdbcTemplate jdbc;
    final List<Long> teams = new ArrayList<>();
    final List<Long> matches = new ArrayList<>();
    long competition, season, link, target;

    @BeforeEach
    void setup() {
        String prefix = "Radar QA " + UUID.randomUUID();
        for (int index = 0; index < 5; index++)
            teams.add(jdbc.queryForObject("insert into teams(name,type) values (?, 'CLUB') returning id", Long.class,
                    prefix + " Team " + index));
        competition = jdbc.queryForObject("insert into competitions(name,type) values (?, 'LEAGUE') returning id",
                Long.class, prefix);
        season = jdbc.queryForObject("insert into seasons(name) values ('2199/2200') returning id", Long.class);
        link = jdbc.queryForObject(
                "insert into competition_seasons(competition_id,season_id) values (?,?) returning id", Long.class,
                competition, season);
        target = match(teams.get(0), teams.get(1), "2199-01-10T00:00:00Z", "SCHEDULED", null, null);
        for (int index = 0; index < 5; index++) {
            long id = match(teams.get(index), teams.get((index + 1) % 5), "2199-01-0" + (index + 1) + "T00:00:00Z",
                    "FINISHED", 1, 0);
            jdbc.update("""
                    insert into match_statistics(match_id,created_at,updated_at,
                        home_possession,away_possession,home_passes_final_third,away_passes_final_third,
                        home_xg,away_xg,home_shots_on_target,away_shots_on_target,
                        home_touches_in_box,away_touches_in_box,home_shots,away_shots,
                        home_pass_accuracy,away_pass_accuracy)
                    values (?,now(),now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """, id, 60 + index, 40 - index, 50 + index, 30 - index, index == 0 ? 0.0 : 2.0 + index * 0.1,
                    0.5 + index * 0.1, 6 + index, 2 + index, 18 + index, 8 + index, 12 + index, 5 + index, 85, 70);
        }
    }

    @AfterEach
    void cleanup() {
        for (long id : matches) {
            jdbc.update("delete from match_statistics where match_id=?", id);
            jdbc.update("delete from matches where id=?", id);
        }
        jdbc.update("delete from competition_seasons where id=?", link);
        jdbc.update("delete from seasons where id=?", season);
        jdbc.update("delete from competitions where id=?", competition);
        for (long id : teams)
            jdbc.update("delete from teams where id=?", id);
    }

    @Test
    void radarUsesBothSidesAndSharedReferenceWithoutChangingData() throws Exception {
        long missing = match(teams.get(0), teams.get(2), "2199-01-09T00:00:00Z", "FINISHED", 0, 0);
        long hidden = match(teams.get(0), teams.get(3), "2199-01-09T12:00:00Z", "FINISHED", 0, 0);
        jdbc.update("update matches set is_hidden=true where id=?", hidden);
        String body = get(target).body();
        assertThat(JsonPath.<String>read(body, "$.status")).isEqualTo("READY");
        assertThat(JsonPath.<Integer>read(body, "$.home.historyMatches")).isEqualTo(3);
        assertThat(JsonPath.<Integer>read(body, "$.home.axes[0].primary.availableMatches")).isEqualTo(2);
        assertThat(JsonPath.<Double>read(body, "$.home.axes[0].primary.average")).isEqualTo(48.0);
        assertThat(JsonPath.<Double>read(body, "$.home.axes[5].primary.average")).isEqualTo(1.45);
        assertThat(JsonPath.<Double>read(body, "$.away.axes[5].primary.average")).isEqualTo(0.3);
        assertThat(JsonPath.<Double>read(body, "$.away.axes[5].score"))
                .isGreaterThan(JsonPath.<Double>read(body, "$.home.axes[5].score"));
        assertThat(JsonPath.<Integer>read(body, "$.home.axes[2].primary.availableMatches")).isEqualTo(2);
        assertThat(JsonPath.<Double>read(body, "$.home.axes[2].primary.average")).isEqualTo(0.45);
        assertThat(JsonPath.<Integer>read(body, "$.home.axes[0].referenceProfiles")).isEqualTo(5);
        assertThat(JsonPath.<Double>read(body, "$.home.axes[0].score")).isBetween(0.0, 100.0);
        assertThat(get(target).body()).isEqualTo(body);
        assertThat(
                jdbc.queryForObject("select count(*) from match_statistics where match_id=?", Integer.class, missing))
                .isZero();
        jdbc.update("update matches set is_hidden=true where id=?", target);
        assertThat(get(target).statusCode()).isEqualTo(404);
    }

    @Test
    void insufficientSamplesNeverBecomeZeroScores() throws Exception {
        jdbc.update("delete from match_statistics where match_id=?", matches.get(1));
        String body = get(target).body();
        assertThat(JsonPath.<String>read(body, "$.status")).isEqualTo("INSUFFICIENT_REFERENCE_DATA");
        assertThat(JsonPath.<Object>read(body, "$.home.axes[0].score")).isNull();
        jdbc.update("update matches set status='POSTPONED' where id=?", target);
        assertThat(JsonPath.<String>read(get(target).body(), "$.status")).isEqualTo("NOT_APPLICABLE");
    }

    private long match(long home, long away, String kickoff, String status, Integer homeScore, Integer awayScore) {
        long id = jdbc.queryForObject("""
                insert into matches(competition_season_id,home_team_id,away_team_id,kickoff_time,status,
                    home_score,away_score) values (?,?,?,cast(? as timestamptz),?,?,?) returning id
                """, Long.class, link, home, away, kickoff, status, homeScore, awayScore);
        matches.add(id);
        return id;
    }

    private HttpResponse<String> get(long id) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest
                    .newBuilder(URI
                            .create("http://localhost:" + port + "/api/public/matches/" + id + "/playing-style-radar"))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
