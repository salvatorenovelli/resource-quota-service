package com.myseotoolbox.resourcequota;

import com.myseotoolbox.quota4j.QuotaService;
import com.myseotoolbox.quota4j.persistence.QuotaPersistence;
import com.myseotoolbox.quota4j.persistence.ResourceQuotaPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public QuotaService getQuotaService(ResourceQuotaPersistence resourceQuotaPersistence, QuotaPersistence quotaPersistence) {
        return new QuotaService(resourceQuotaPersistence, quotaPersistence);
    }
}

