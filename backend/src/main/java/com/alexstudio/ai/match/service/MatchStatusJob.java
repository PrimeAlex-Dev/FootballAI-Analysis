package com.alexstudio.ai.match.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class MatchStatusJob {
    private static final Logger LOG = LoggerFactory.getLogger(MatchStatusJob.class);
    private final MatchService service;
    public MatchStatusJob(MatchService service) {
        this.service = service;
    }
    @Scheduled(fixedDelayString = "${match.live-update-delay-ms:15000}", initialDelayString = "${match.live-update-delay-ms:15000}")
    public void run() {
        int changed = service.promoteDue();
        if (changed > 0)
            LOG.info("Promoted {} scheduled matches to LIVE", changed);
    }
}
