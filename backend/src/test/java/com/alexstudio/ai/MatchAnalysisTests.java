package com.alexstudio.ai;

import java.util.UUID;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.alexstudio.ai.common.exception.BusinessException;
import com.alexstudio.ai.match.service.MatchService;
import com.alexstudio.ai.matchanalysis.dto.MatchAnalysisRequest;
import com.alexstudio.ai.matchanalysis.entity.AnalysisStatus;
import com.alexstudio.ai.matchanalysis.service.MatchAnalysisService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@ActiveProfiles("test")
class MatchAnalysisTests {
    @LocalServerPort
    int port;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    MatchAnalysisService analyses;
    @Autowired
    MatchService matches;
    long matchId, linkId, seasonId, competitionId, homeId, awayId;

    @BeforeEach
    void setup() {
        String prefix = "Analysis QA " + UUID.randomUUID();
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
        jdbc.update("delete from match_analyses where match_id=?", matchId);
        jdbc.update("delete from matches where id=?", matchId);
        jdbc.update("delete from competition_seasons where id=?", linkId);
        jdbc.update("delete from seasons where id=?", seasonId);
        jdbc.update("delete from competitions where id=?", competitionId);
        jdbc.update("delete from teams where id in (?,?)", homeId, awayId);
    }

    @Test
    void draftPublishHideAndDelete() {
        analyses.create(matchId, new MatchAnalysisRequest("", "", AnalysisStatus.DRAFT));
        assertThatThrownBy(() -> analyses.getPublic(matchId)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> analyses.create(matchId, new MatchAnalysisRequest("a", "b", AnalysisStatus.PUBLISHED)))
                .isInstanceOf(BusinessException.class);
        analyses.update(matchId, new MatchAnalysisRequest("Phân tích\ntrận đấu", "3-0", AnalysisStatus.PUBLISHED));
        assertThat(analyses.getPublic(matchId).analysisContent()).isEqualTo("Phân tích\ntrận đấu");
        matches.setHidden(matchId, true);
        assertThatThrownBy(() -> analyses.getPublic(matchId)).isInstanceOf(BusinessException.class);
        assertThat(analyses.get(matchId).status()).isEqualTo(AnalysisStatus.PUBLISHED);
        matches.setHidden(matchId, false);
        analyses.update(matchId, new MatchAnalysisRequest("", "", AnalysisStatus.DRAFT));
        assertThatThrownBy(() -> analyses.getPublic(matchId)).isInstanceOf(BusinessException.class);
        analyses.delete(matchId);
        assertThat(matches.get(matchId).id()).isEqualTo(matchId);
        assertThatThrownBy(() -> analyses.get(matchId)).isInstanceOf(BusinessException.class);
        analyses.create(matchId, new MatchAnalysisRequest("a", "b", AnalysisStatus.PUBLISHED));
    }

    @Test
    void wordLimitsAndDatabaseConstraints() {
        String analysis = "a ".repeat(20000).trim();
        String prediction = "b ".repeat(2000).trim();
        analyses.create(matchId, new MatchAnalysisRequest(analysis, prediction, AnalysisStatus.PUBLISHED));
        assertThatThrownBy(() -> analyses.update(matchId,
                new MatchAnalysisRequest(analysis + " extra", prediction, AnalysisStatus.PUBLISHED)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> analyses.update(matchId,
                new MatchAnalysisRequest(analysis, prediction + " extra", AnalysisStatus.PUBLISHED)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> analyses.update(matchId, new MatchAnalysisRequest(" ", "b", AnalysisStatus.PUBLISHED)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> analyses.update(matchId, new MatchAnalysisRequest("a", "b", null)))
                .isInstanceOf(BusinessException.class);
        assertThat(jdbc.queryForObject("select count(*) from match_analyses where match_id=?", Integer.class, matchId))
                .isEqualTo(1);
        assertThatThrownBy(() -> jdbc.update("update match_analyses set status='OTHER' where match_id=?", matchId))
                .isInstanceOf(Exception.class);
    }

    @Test
    void deletingMatchCascadesAnalysis() {
        analyses.create(matchId, new MatchAnalysisRequest("a", "b", AnalysisStatus.PUBLISHED));
        matches.delete(matchId);
        assertThat(jdbc.queryForObject("select count(*) from match_analyses where match_id=?", Integer.class, matchId))
                .isZero();
    }

    @Test
    void apiContractAndPublicVisibility() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String admin = "http://localhost:" + port + "/api/matches/" + matchId + "/analysis";
        String customer = "http://localhost:" + port + "/api/public/matches/" + matchId + "/analysis";
        assertThat(call(client, "GET", customer, null).statusCode()).isEqualTo(404);
        assertThat(call(client, "POST", admin, """
                {"analysisContent":"draft","predictionContent":"","status":"DRAFT"}
                """).statusCode()).isEqualTo(201);
        assertThat(call(client, "GET", customer, null).statusCode()).isEqualTo(404);
        assertThat(call(client, "POST", admin, """
                {"analysisContent":"x","predictionContent":"y","status":"PUBLISHED"}
                """).statusCode()).isEqualTo(409);
        assertThat(call(client, "PUT", admin, """
                {"analysisContent":"line 1\\nline 2","predictionContent":"3-0","status":"PUBLISHED"}
                """).statusCode()).isEqualTo(200);
        var published = call(client, "GET", customer, null);
        assertThat(published.statusCode()).isEqualTo(200);
        assertThat(published.body()).contains("line 1\\nline 2", "3-0");
        assertThat(call(client, "PUT", admin, """
                {"analysisContent":"","predictionContent":"x","status":"PUBLISHED"}
                """).statusCode()).isEqualTo(400);
        assertThat(call(client, "PUT", admin, """
                {"analysisContent":"x","predictionContent":"y","status":null}
                """).statusCode()).isEqualTo(400);
        assertThat(call(client, "DELETE", admin, null).statusCode()).isEqualTo(204);
        assertThat(call(client, "GET", customer, null).statusCode()).isEqualTo(404);
    }

    private HttpResponse<String> call(HttpClient client, String method, String url, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
                .method(method,
                        body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
