package com.myseotoolbox.resourcequota;

import io.github.quota4j.QuotaService;
import io.github.quota4j.persistence.ResourceQuotaPersistence;
import io.github.quota4j.persistence.QuotaPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public QuotaService getQuotaService(ResourceQuotaPersistence resourceQuotaPersistence, QuotaPersistence quotaPersistence) {
        return new QuotaService(resourceQuotaPersistence, quotaPersistence);
    }
}

