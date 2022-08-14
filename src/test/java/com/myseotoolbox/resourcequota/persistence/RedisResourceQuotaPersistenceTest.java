package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.resourcequota.DataRedisContainerTest;
import io.github.quota4j.model.ResourceQuota;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

class RedisResourceQuotaPersistenceTest extends DataRedisContainerTest {

    public static final ResourceQuota TEST_RESOURCE_QUOTA = new ResourceQuota("ID1", "ClassName", "Initial State");
    public static final ResourceQuota TEST_RESOURCE_QUOTA2 = new ResourceQuota("ID2", "ClassName", "Initial State");
    @Autowired
    RedisResourceQuotaPersistence sut;

    @BeforeEach
    void setUp() {
        sut.save(TEST_RESOURCE_QUOTA);
        sut.save(TEST_RESOURCE_QUOTA2);
    }

    @Test
    void shouldFindMultiple() {
        assertThat(sut.findAll(), Matchers.contains(
                new ResourceQuota("ID1", "ClassName", "Initial State"),
                new ResourceQuota("ID2", "ClassName", "Initial State")
        ));
    }

    @Test
    void shouldFind() {
        assertThat(sut.findById(TEST_RESOURCE_QUOTA.id()).get(), is(TEST_RESOURCE_QUOTA));
    }

    @Test
    void shouldReturnEmptyIdNoMatch() {
        assertFalse(sut.findById("Wrong ID").isPresent());
    }
}