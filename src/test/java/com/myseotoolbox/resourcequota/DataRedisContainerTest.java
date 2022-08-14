package com.myseotoolbox.resourcequota;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@ContextConfiguration(initializers = DataRedisContainerTest.RedisContainerContextInitializer.class)
class DataRedisContainerTest {
    @Container
    private static final GenericContainer<?> redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);

    @Autowired
    RedisConnectionFactory connectionFactory;

    @BeforeEach
    void dataRedisContainerTestSetup() {
        connectionFactory.getConnection().flushAll();
    }

    static class RedisContainerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.redis.host=" + redis.getHost(),
                    "spring.redis.port=" + redis.getMappedPort(6379)
            );
            values.applyTo(applicationContext);
        }
    }

}