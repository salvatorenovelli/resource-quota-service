package com.myseotoolbox.resourcequota;

import com.myseotoolbox.quota4j.QuotaService;
import com.myseotoolbox.quota4j.persistence.QuotaPersistence;
import com.myseotoolbox.quota4j.persistence.QuotaStatePersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public QuotaService getQuotaService(QuotaPersistence resourceQuotaPersistence, QuotaStatePersistence quotaStatePersistence) {
        return new QuotaService(resourceQuotaPersistence, quotaStatePersistence);
    }
}

