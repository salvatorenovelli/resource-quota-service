package com.myseotoolbox.resourcequota;

import com.myseotoolbox.resourcequota.persistence.RedisResourceQuotaPersistence;
import com.myseotoolbox.resourcequota.persistence.RedisUserQuotaPersistence;
import io.github.quota4j.UserQuotaService;
import io.github.quota4j.model.ResourceQuota;
import io.github.quota4j.model.UserQuotaId;
import io.github.quota4j.model.UserQuotaState;
import io.github.quota4j.quotamanager.quantityovertime.QuantityOverTimeLimit;
import io.github.quota4j.quotamanager.quantityovertime.QuantityOverTimeQuotaManager;
import io.github.quota4j.quotamanager.quantityovertime.QuantityOverTimeState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserQuotaServiceTest extends DataRedisContainerTest {
    public static final String RESOURCE_ID = "group.resourceId";
    public static final String USERNAME = "salvatore";
    @Autowired
    UserQuotaService sut;

    @Autowired
    RedisResourceQuotaPersistence resourceQuotaRepository;

    @Autowired
    RedisUserQuotaPersistence userQuotaPersistence;

    @BeforeEach
    void setUp() {
        sut.registerQuotaManagerFactory(QuantityOverTimeQuotaManager.class.getName(),
                listener -> new QuantityOverTimeQuotaManager(listener, new TestClock()));

        initializeTestResourceQuota();
    }

    @Test
    void shouldAllowAcquire() {
        assertTrue(sut.tryAcquire(USERNAME, RESOURCE_ID, 6));
    }

    @Test
    void shouldDeclineIfNotEnoughResources() {
        sut.tryAcquire(USERNAME, RESOURCE_ID, 6);
        assertFalse(sut.tryAcquire(USERNAME, RESOURCE_ID, 6));
    }

    @Test
    void shouldRestoreUserQuotaState() {
        initUserQuotaState(USERNAME, 5);
        assertFalse(sut.tryAcquire(USERNAME, RESOURCE_ID, 6));
    }

    @Test
    void shouldCountRemainingTokens() {
        initUserQuotaState(USERNAME, 5);
        sut.tryAcquire(USERNAME, RESOURCE_ID, 1);
        assertTrue(sut.tryAcquire(USERNAME, RESOURCE_ID, 4));
    }

    void initUserQuotaState(String username, int remainingTokens) {
        QuantityOverTimeState initialState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), remainingTokens, Instant.EPOCH);
        userQuotaPersistence.save(new UserQuotaState(UserQuotaId.create(username, RESOURCE_ID), QuantityOverTimeQuotaManager.class.getName(), initialState));
    }

    private void initializeTestResourceQuota() {
        QuantityOverTimeState defaultState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), 10, Instant.EPOCH);
        ResourceQuota entity = new ResourceQuota(RESOURCE_ID, QuantityOverTimeQuotaManager.class.getName(), defaultState);
        resourceQuotaRepository.save(entity);
    }
}
