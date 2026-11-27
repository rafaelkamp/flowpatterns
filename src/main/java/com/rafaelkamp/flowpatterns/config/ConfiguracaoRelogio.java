package com.rafaelkamp.flowpatterns.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Optional;

@Configuration
public class ConfiguracaoRelogio {

    private static final String FUSO_PADRAO = "America/Sao_Paulo";

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.system(ZoneId.of(FUSO_PADRAO));
    }

    @Bean
    public DateTimeProvider dateTimeProvider(Clock clock) {
        return () -> Optional.of(clock.instant());
    }
}
