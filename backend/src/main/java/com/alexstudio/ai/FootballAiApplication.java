package com.alexstudio.ai;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FootballAiApplication {

    public static void main(String[] args) {
        // PostgreSQL may not recognize the legacy timezone ID supplied by Windows/JVM.
        if ("Asia/Saigon".equals(TimeZone.getDefault().getID())) {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        }
        SpringApplication.run(FootballAiApplication.class, args);
    }
}
