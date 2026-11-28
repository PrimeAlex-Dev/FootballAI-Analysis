package com.alexstudio.ai.matchanalysis.service;

import java.util.regex.Pattern;
import com.alexstudio.ai.common.exception.BusinessException;
import com.alexstudio.ai.match.service.MatchService;
import com.alexstudio.ai.matchanalysis.dto.*;
import com.alexstudio.ai.matchanalysis.entity.*;
import com.alexstudio.ai.matchanalysis.repository.MatchAnalysisRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchAnalysisService {
    private static final Pattern WORD = Pattern.compile("\\S+", Pattern.UNICODE_CHARACTER_CLASS);
    private final MatchAnalysisRepository repository;
    private final MatchService matches;

    public MatchAnalysisService(MatchAnalysisRepository repository, MatchService matches) {
        this.repository = repository;
        this.matches = matches;
    }

    @Transactional(readOnly = true)
    public MatchAnalysisResponse get(Long matchId) {
        matches.get(matchId);
        return response(repository.findByMatchId(matchId).orElseThrow(() -> missing()));
    }

    @Transactional(readOnly = true)
    public MatchAnalysisResponse getPublic(Long matchId) {
        matches.getPublic(matchId);
        MatchAnalysis analysis = repository.findByMatchId(matchId).orElseThrow(() -> missing());
        if (analysis.getStatus() != AnalysisStatus.PUBLISHED)
            throw missing();
        return response(analysis);
    }

    @Transactional
    public MatchAnalysisResponse create(Long matchId, MatchAnalysisRequest request) {
        matches.get(matchId);
        String[] content = validate(request);
        if (repository.existsByMatchId(matchId))
            throw new BusinessException(HttpStatus.CONFLICT, "Match analysis already exists");
        return response(repository.saveAndFlush(new MatchAnalysis(matchId, content[0], content[1], request.status())));
    }

    @Transactional
    public MatchAnalysisResponse update(Long matchId, MatchAnalysisRequest request) {
        matches.get(matchId);
        String[] content = validate(request);
        MatchAnalysis analysis = repository.findByMatchId(matchId).orElseThrow(() -> missing());
        analysis.update(content[0], content[1], request.status());
        repository.flush();
        return response(analysis);
    }

    @Transactional
    public void delete(Long matchId) {
        matches.get(matchId);
        MatchAnalysis analysis = repository.findByMatchId(matchId).orElseThrow(() -> missing());
        repository.delete(analysis);
        repository.flush();
    }

    private String[] validate(MatchAnalysisRequest request) {
        String analysis = request.analysisContent() == null ? "" : request.analysisContent().strip();
        String prediction = request.predictionContent() == null ? "" : request.predictionContent().strip();
        if (request.status() == null)
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Analysis status is required");
        if (words(analysis) > 20000 || words(prediction) > 2000)
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Match analysis word limit exceeded");
        if (request.status() == AnalysisStatus.PUBLISHED && (words(analysis) == 0 || words(prediction) == 0))
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Published analysis requires both contents");
        return new String[]{analysis, prediction};
    }

    private int words(String content) {
        int count = 0;
        var matcher = WORD.matcher(content);
        while (matcher.find())
            count++;
        return count;
    }

    private MatchAnalysisResponse response(MatchAnalysis analysis) {
        return new MatchAnalysisResponse(analysis.getMatchId(), analysis.getAnalysisContent(),
                analysis.getPredictionContent(), analysis.getStatus());
    }

    private BusinessException missing() {
        return new BusinessException(HttpStatus.NOT_FOUND, "Match analysis not found");
    }
}
