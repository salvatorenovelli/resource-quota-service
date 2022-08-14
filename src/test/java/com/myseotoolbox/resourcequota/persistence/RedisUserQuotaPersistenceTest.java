package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.resourcequota.DataRedisContainerTest;
import io.github.quota4j.model.UserQuotaId;
import io.github.quota4j.model.UserQuotaState;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;


class RedisUserQuotaPersistenceTest extends DataRedisContainerTest {


    public static final UserQuotaId USER_QUOTA_ID = UserQuotaId.create("username", "resourceId");
    public static final UserQuotaState TEST_USER_QUOTA_STATE = new UserQuotaState(USER_QUOTA_ID, "className", "state");
    @Autowired
    RedisUserQuotaPersistence sut;

    @BeforeEach
    void setUp() {
        sut.save(TEST_USER_QUOTA_STATE);
    }

    @Test
    void shouldSave() {
        assertThat(sut.findAll(), Matchers.contains(TEST_USER_QUOTA_STATE));
    }

    @Test
    void shouldFindById() {
        assertThat(sut.findById(USER_QUOTA_ID).get(), Matchers.is(TEST_USER_QUOTA_STATE));
    }
}