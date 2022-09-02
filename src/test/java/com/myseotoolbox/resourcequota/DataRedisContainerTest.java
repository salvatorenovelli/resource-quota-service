package com.myseotoolbox.resourcequota;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(initializers = DataRedisContainerTest.RedisContainerContextInitializer.class)
public class DataRedisContainerTest {

    private static final Logger log = LogManager.getLogger(DataRedisContainerTest.class);

    private static final int REDIS_PORT = 6379;

    @Autowired
    RedisConnectionFactory connectionFactory;

    @AfterEach
    void dataRedisContainerTestSetup() {
        connectionFactory.getConnection().flushAll();
    }

    static class RedisContainerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            GenericContainer<?> redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(REDIS_PORT);
            redis.start();
            while (!redis.isRunning()) {
                busyWaitIMSorry();
            }


            log.info("Redis server is available at {}:{}", redis.getHost(), redis.getMappedPort(REDIS_PORT));

            TestPropertyValues values = TestPropertyValues.of(
                    "spring.redis.host=" + redis.getHost(),
                    "spring.redis.port=" + redis.getMappedPort(REDIS_PORT)
            );
            values.applyTo(applicationContext);

        }

        private static void busyWaitIMSorry()  {
            try {
                Thread.sleep(50);
                log.info("Busy wait..zZzZzzZ...");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}