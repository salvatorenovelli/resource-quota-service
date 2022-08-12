package com.myseotoolbox.resourcequota.persistence;

import io.github.quota4j.model.UserQuotaId;
import io.github.quota4j.model.UserQuotaState;
import io.github.quota4j.persistence.UserQuotaPersistence;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class CrudUserQuotaPersistence implements UserQuotaPersistence {

    private static final String KEY = "UserQuota";
    private final HashOperations<String, UserQuotaId, UserQuotaState> hashOperations;

    public CrudUserQuotaPersistence(RedisTemplate redisTemplate) {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public UserQuotaState save(UserQuotaState userQuotaState) {
        hashOperations.put(KEY, userQuotaState.id(), userQuotaState);
        return userQuotaState;
    }

    @Override
    public Optional<UserQuotaState> findById(UserQuotaId userQuotaId) {
        return Optional.ofNullable(hashOperations.get(KEY, userQuotaId));
    }

    public Collection<UserQuotaState> findAll() {
        return hashOperations.values(KEY);
    }
}
