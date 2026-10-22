package com.mfi.tracker.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** The single source of "today" for status and alert rules. */
    @Bean
    public Clock clock(@Value("${app.fixed-date:}") String fixedDate) {
        ZoneId zone = ZoneId.systemDefault();
        if (fixedDate == null || fixedDate.isBlank()) {
            return Clock.system(zone);
        }
        return Clock.fixed(LocalDate.parse(fixedDate.trim()).atStartOfDay(zone).toInstant(), zone);
    }
}
