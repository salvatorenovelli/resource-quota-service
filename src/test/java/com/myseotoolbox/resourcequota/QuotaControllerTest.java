package com.myseotoolbox.resourcequota;

import com.myseotoolbox.resourcequota.persistence.CrudResourceQuotaPersistence;
import com.myseotoolbox.resourcequota.persistence.CrudUserQuotaPersistence;
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


class QuotaControllerTest extends DataRedisContainerTest {

    @Autowired
    UserQuotaService sut;

    @Autowired
    CrudResourceQuotaPersistence resourceQuotaRepository;

    @Autowired
    CrudUserQuotaPersistence userQuotaPersistence;

    @BeforeEach
    void setUp() {
        sut.registerQuotaManagerFactory(QuantityOverTimeQuotaManager.class.getName(),
                listener -> new QuantityOverTimeQuotaManager(listener, new TestClock()));
    }

    @Test
    void name() {

        String name = QuantityOverTimeQuotaManager.class.getName();
        QuantityOverTimeState defaultState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), 10, Instant.EPOCH);
        ResourceQuota entity = new ResourceQuota("resource.si", name, defaultState);

        System.out.println("resourceQuotaRepository = " + resourceQuotaRepository.findAll());
        System.out.println(resourceQuotaRepository.save(entity));
        System.out.println("resourceQuotaRepository = " + resourceQuotaRepository.findAll());

        QuantityOverTimeState initialState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), 5, Instant.EPOCH);

        userQuotaPersistence.save(new UserQuotaState(UserQuotaId.create("salvatore", "resource.si"), name, initialState));


        System.out.println("userQuotaPersistence.findAll() = " + userQuotaPersistence.findAll());


        boolean res = sut.tryAcquire("salvatore", "resource.si", 6);

        assertFalse(res);
    }


}