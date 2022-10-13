package com.myseotoolbox.resourcequota;

import com.myseotoolbox.quota4j.QuotaService;
import com.myseotoolbox.quota4j.model.Quota;
import com.myseotoolbox.quota4j.persistence.QuotaPersistence;
import com.myseotoolbox.quota4j.quotamanager.quantityovertime.QuantityOverTimeLimit;
import com.myseotoolbox.quota4j.quotamanager.quantityovertime.QuantityOverTimeQuotaManager;
import com.myseotoolbox.quota4j.quotamanager.quantityovertime.QuantityOverTimeState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Profile("!test")
@Configuration
public class DatabasePrimer {
    final QuotaService quotaService;
    final QuotaPersistence resourceQuotaPersistence;
    public DatabasePrimer(QuotaService quotaService, QuotaPersistence resourceQuotaPersistence) {this.quotaService = quotaService;
        this.resourceQuotaPersistence = resourceQuotaPersistence;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void init() {
        quotaService.registerQuotaManagerFactory(QuantityOverTimeQuotaManager.class.getName(),
                () -> new QuantityOverTimeQuotaManager(Clock.systemDefaultZone()));

        QuantityOverTimeState defaultState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(1, Duration.ofSeconds(1)), 10, Instant.EPOCH);
        Quota entity = new Quota("crawler", QuantityOverTimeQuotaManager.class.getName(), defaultState);
        resourceQuotaPersistence.save(entity);
    }

}
