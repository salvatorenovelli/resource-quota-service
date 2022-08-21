package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.resourcequota.DataRedisContainerTest;
import io.github.quota4j.model.QuotaId;
import io.github.quota4j.model.QuotaState;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;


class RedisQuotaPersistenceTest extends DataRedisContainerTest {


    public static final QuotaId QUOTA_ID = QuotaId.create("ownerId", "resourceId");
    public static final QuotaState TEST_QUOTA_STATE = new QuotaState(QUOTA_ID, "className", "state");
    @Autowired
    RedisQuotaPersistence sut;

    @BeforeEach
    void setUp() {
        sut.save(TEST_QUOTA_STATE);
    }

    @Test
    void shouldSave() {
        assertThat(sut.findAll(), Matchers.contains(TEST_QUOTA_STATE));
    }

    @Test
    void shouldFindById() {
        assertThat(sut.findById(QUOTA_ID).get(), Matchers.is(TEST_QUOTA_STATE));
    }
}