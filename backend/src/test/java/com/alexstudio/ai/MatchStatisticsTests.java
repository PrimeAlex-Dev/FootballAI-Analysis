package com.alexstudio.ai;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import tools.jackson.databind.json.JsonMapper;
import com.alexstudio.ai.match.service.MatchService;
import com.alexstudio.ai.matchstatistics.service.MatchStatisticsService;
import com.alexstudio.ai.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@ActiveProfiles("test")
class MatchStatisticsTests {
    @LocalServerPort
    int port;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MatchStatisticsService statistics;
    @Autowired
    MatchService matches;
    JsonMapper json = new JsonMapper();
    long matchId, linkId, seasonId, competitionId, homeId, awayId;
    @BeforeEach
    void setup() {
        String prefix = "Stats QA " + UUID.randomUUID();
        homeId = jdbc.queryForObject("insert into teams(name,type) values (?, 'CLUB') returning id", Long.class,
                prefix + " Home");
        awayId = jdbc.queryForObject("insert into teams(name,type) values (?, 'CLUB') returning id", Long.class,
                prefix + " Away");
        competitionId = jdbc.queryForObject("insert into competitions(name,type) values (?, 'LEAGUE') returning id",
                Long.class, prefix + " League");
        seasonId = jdbc.queryForObject("insert into seasons(name) values ('2190/2191') returning id", Long.class);
        linkId = jdbc.queryForObject(
                "insert into competition_seasons(competition_id,season_id) values (?,?) returning id", Long.class,
                competitionId, seasonId);
        matchId = jdbc.queryForObject(
                "insert into matches(competition_season_id,home_team_id,away_team_id,kickoff_time,status) values (?,?,?,'2035-01-01T00:00:00Z','SCHEDULED') returning id",
                Long.class, linkId, homeId, awayId);
    }
    @AfterEach
    void cleanup() {
        jdbc.update("delete from match_statistics where match_id=?", matchId);
        jdbc.update("delete from matches where id=?", matchId);
        jdbc.update("delete from competition_seasons where id=?", linkId);
        jdbc.update("delete from seasons where id=?", seasonId);
        jdbc.update("delete from competitions where id=?", competitionId);
        jdbc.update("delete from teams where id in (?,?)", homeId, awayId);
    }
    @Test
    void previewCreateUpdateAndPresence() throws Exception {
        var input = json.readTree("{\"home\":{\"shots\":0,\"xg\":4.35},\"away\":{\"shots\":null,\"possession\":37}}");
        assertThat(matches.get(matchId).hasStatistics()).isFalse();
        assertThat(statistics.preview(matchId, input).home().getShots()).isZero();
        assertThat(
                jdbc.queryForObject("select count(*) from match_statistics where match_id=?", Integer.class, matchId))
                .isZero();
        var created = statistics.create(matchId, input);
        assertThat(created.home().getShots()).isZero();
        assertThat(created.away().getShots()).isNull();
        assertThat(created.home().getXg()).isEqualByComparingTo("4.35");
        assertThat(created.createdAt()).isNotNull();
        assertThat(matches.get(matchId).hasStatistics()).isTrue();
        assertThat(
                matches.list(null, linkId, null, null, null, null, null, null, Sort.Direction.DESC, 0, 20).getContent())
                .anySatisfy(item -> {
                    if (item.id().equals(matchId))
                        assertThat(item.hasStatistics()).isTrue();
                });
        assertThat(statistics.get(matchId).away().getPossession()).isEqualTo(37);
        assertThatThrownBy(() -> statistics.create(matchId, input)).isInstanceOf(BusinessException.class);
        var replacement = json.readTree("{\"home\":{\"shots\":5},\"away\":{}}");
        var updated = statistics.update(matchId, replacement);
        assertThat(updated.home().getShots()).isEqualTo(5);
        assertThat(updated.home().getXg()).isNull();
        assertThat(updated.away().getPossession()).isNull();
        assertThat(updated.createdAt()).isEqualTo(created.createdAt());
        assertThat(
                jdbc.queryForObject("select count(*) from match_statistics where match_id=?", Integer.class, matchId))
                .isEqualTo(1);
    }
    @Test
    void rejectsInvalidPayloadAndMissingRecords() throws Exception {
        for (String invalid : new String[]{"{}", "{\"home\":{},\"away\":{},\"source\":1}",
                "{\"home\":{\"fouls\":null},\"away\":{}}", "{\"home\":{\"shots\":1.5},\"away\":{}}",
                "{\"home\":{\"shots\":-1},\"away\":{}}", "{\"home\":{\"possession\":101},\"away\":{}}",
                "{\"home\":{\"xg\":-1},\"away\":{}}"})
            assertThatThrownBy(() -> statistics.preview(matchId, json.readTree(invalid))).as(invalid)
                    .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> statistics.get(matchId)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> statistics.update(matchId, json.readTree("{\"home\":{},\"away\":{}}")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> statistics.preview(Long.MAX_VALUE, json.readTree("{\"home\":{},\"away\":{}}")))
                .isInstanceOf(BusinessException.class);
    }
    @Test
    void apiPreviewCreateUpdateAndValidation() throws Exception {
        String path = "/api/matches/" + matchId + "/statistics";
        String input = "{\"home\":{\"shots\":0,\"xg\":4.35},\"away\":{\"shots\":10}}";
        assertThat(call("GET", path, null).statusCode()).isEqualTo(404);
        assertThat(call("POST", path + "/preview", input).statusCode()).isEqualTo(200);
        assertThat(matches.get(matchId).hasStatistics()).isFalse();
        assertThat(call("POST", path, input).statusCode()).isEqualTo(201);
        assertThat(call("DELETE", "/api/matches/" + matchId, null).statusCode()).isEqualTo(409);
        assertThat(call("POST", path, input).statusCode()).isEqualTo(409);
        assertThat(call("GET", path, null).body()).contains("\"shots\":0");
        assertThat(call("POST", path + "/preview", "{\"home\":{\"fouls\":null},\"away\":{}}").statusCode())
                .isEqualTo(400);
        assertThat(call("PUT", path, "{\"home\":{},\"away\":{}}").statusCode()).isEqualTo(200);
        assertThat(call("GET", path, null).body()).contains("\"shots\":null");
    }
    private HttpResponse<String> call(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        builder.method(method,
                body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
