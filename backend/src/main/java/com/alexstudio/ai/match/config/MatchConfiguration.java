package com.alexstudio.ai.match.config;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
@Configuration
@EnableScheduling
public class MatchConfiguration {
    @Bean
    public Clock matchClock() {
        return Clock.systemUTC();
    }
}
