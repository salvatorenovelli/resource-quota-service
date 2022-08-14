package com.myseotoolbox.resourcequota;

import org.junit.jupiter.api.BeforeEach;
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

    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(DataRedisContainerTest.class);

    private static final int REDIS_PORT = 6379;

    private static final GenericContainer<?> redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(REDIS_PORT);

    @Autowired
    RedisConnectionFactory connectionFactory;

    @BeforeEach
    void dataRedisContainerTestSetup() {
        connectionFactory.getConnection().flushAll();
    }

    static class RedisContainerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            if(!redis.isRunning()){
                redis.start();
                log.info("Redis server is available at {}:{}", redis.getHost(), redis.getMappedPort(REDIS_PORT));
                TestPropertyValues values = TestPropertyValues.of(
                        "spring.redis.host=" + redis.getHost(),
                        "spring.redis.port=" + redis.getMappedPort(REDIS_PORT)
                );
                values.applyTo(applicationContext);
            }

        }
    }

}