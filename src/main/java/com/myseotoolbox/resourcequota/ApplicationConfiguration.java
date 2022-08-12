package com.myseotoolbox.resourcequota;

import io.github.quota4j.UserQuotaService;
import io.github.quota4j.persistence.ResourceQuotaPersistence;
import io.github.quota4j.persistence.UserQuotaPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public UserQuotaService getUserQuotaService(ResourceQuotaPersistence resourceQuotaPersistence, UserQuotaPersistence userQuotaPersistence) {
        return new UserQuotaService(resourceQuotaPersistence, userQuotaPersistence);
    }
}

