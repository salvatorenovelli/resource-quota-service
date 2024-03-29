package com.myseotoolbox.resourcequota;

import com.myseotoolbox.quota4j.QuotaService;
import com.myseotoolbox.quota4j.model.Quota;
import com.myseotoolbox.quota4j.model.QuotaState;
import com.myseotoolbox.quota4j.model.QuotaStateId;
import com.myseotoolbox.quota4j.quotamanager.quantityovertime.QuantityOverTimeLimit;
import com.myseotoolbox.quota4j.quotamanager.quantityovertime.QuantityOverTimeQuotaManager;
import com.myseotoolbox.quota4j.quotamanager.quantityovertime.QuantityOverTimeState;
import com.myseotoolbox.resourcequota.persistence.MongoQuotaStatePersistence;
import com.myseotoolbox.resourcequota.persistence.MongoResourceQuotaPersistence;
import com.myseotoolbox.resourcequota.persistence.QuotaStateRepository;
import com.myseotoolbox.resourcequota.persistence.QuotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@ComponentScan(value = "com.myseotoolbox.resourcequota.persistence")
public class QuotaServiceTest {
    public static final String RESOURCE_ID = "group.id";
    public static final String OWNER_ID = "salvatore";


    @Autowired
    QuotaRepository quotaRepository;
    @Autowired
    QuotaStateRepository quotaStateRepository;

    @Autowired
    MongoResourceQuotaPersistence resourceQuotaPersistence;
    @Autowired
    MongoQuotaStatePersistence quotaPersistence;

    QuotaService sut;

    @BeforeEach
    void setUp() {
        quotaStateRepository.deleteAll();
        quotaRepository.deleteAll();

        sut = new QuotaService(resourceQuotaPersistence, quotaPersistence);

        sut.registerQuotaManagerFactory(QuantityOverTimeQuotaManager.class.getName(),
                () -> new QuantityOverTimeQuotaManager(new TestClock()));

        initializeTestResourceQuota();
    }

    @Test
    void shouldAllowAcquire() {
        assertTrue(sut.tryAcquire(OWNER_ID, RESOURCE_ID, 6).result());
    }

    @Test
    void shouldDeclineIfNotEnoughResources() {
        sut.tryAcquire(OWNER_ID, RESOURCE_ID, 6);
        assertFalse(sut.tryAcquire(OWNER_ID, RESOURCE_ID, 6).result());
    }

    @Test
    void shouldRestoreQuotaState() {
        initQuotaState(OWNER_ID, 5);
        assertFalse(sut.tryAcquire(OWNER_ID, RESOURCE_ID, 6).result());
    }

    @Test
    void shouldCountRemainingTokens() {
        initQuotaState(OWNER_ID, 5);
        sut.tryAcquire(OWNER_ID, RESOURCE_ID, 1);
        assertTrue(sut.tryAcquire(OWNER_ID, RESOURCE_ID, 4).result());
    }

    void initQuotaState(String ownerId, int remainingTokens) {
        QuantityOverTimeState initialState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), remainingTokens, Instant.EPOCH);
        quotaPersistence.save(new QuotaState(QuotaStateId.create(ownerId, RESOURCE_ID), QuantityOverTimeQuotaManager.class.getName(), initialState));
    }

    private void initializeTestResourceQuota() {
        QuantityOverTimeState defaultState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), 10, Instant.EPOCH);
        Quota entity = new Quota(RESOURCE_ID, QuantityOverTimeQuotaManager.class.getName(), defaultState);
        resourceQuotaPersistence.save(entity);
    }
}
