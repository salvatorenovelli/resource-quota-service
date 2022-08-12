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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = QuotaControllerTest.TestEnvInitializer.class)
class QuotaControllerTest {

    @Container
    public static GenericContainer<?> redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);


    @Autowired
    UserQuotaService userQuotaService;

    @Autowired
    CrudResourceQuotaPersistence resourceQuotaRepository;

    @Autowired
    CrudUserQuotaPersistence userQuotaPersistence;

    @BeforeEach
    void setUp() {
        userQuotaService.registerQuotaManagerFactory(QuantityOverTimeQuotaManager.class.getName(),
                listener -> new QuantityOverTimeQuotaManager(listener, new TestClock()));
    }

    @Test
    void name() throws InterruptedException {

        String name = QuantityOverTimeQuotaManager.class.getName();
        QuantityOverTimeState defaultState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), 10, Instant.EPOCH);
        ResourceQuota entity = new ResourceQuota("resource.si", name, defaultState);

        System.out.println("resourceQuotaRepository = " + resourceQuotaRepository.findAll());
        System.out.println(resourceQuotaRepository.save(entity));
        System.out.println("resourceQuotaRepository = " + resourceQuotaRepository.findAll());

        QuantityOverTimeState initialState = new QuantityOverTimeState(QuantityOverTimeLimit.limitOf(10, Duration.ofDays(1)), 5, Instant.EPOCH);

        userQuotaPersistence.save(new UserQuotaState(UserQuotaId.create("salvatore", "resource.si"), name, initialState));


        System.out.println("userQuotaPersistence.findAll() = " + userQuotaPersistence.findAll());



        boolean salvatore = userQuotaService.tryAcquire("salvatore", "resource.si", 6);

        assertFalse(salvatore);
        System.out.println(salvatore);

//        Thread.sleep(9999999);
    }


    static class TestEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            redis.start();
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.redis.host=" + redis.getHost(),
                    "spring.redis.port=" + redis.getMappedPort(6379)
            );
            values.applyTo(applicationContext);
        }
    }

}