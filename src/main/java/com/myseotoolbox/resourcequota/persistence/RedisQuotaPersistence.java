package com.myseotoolbox.resourcequota.persistence;

import io.github.quota4j.model.QuotaId;
import io.github.quota4j.model.QuotaState;
import io.github.quota4j.persistence.QuotaPersistence;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class RedisQuotaPersistence implements QuotaPersistence {

    private static final String KEY = "QuotaState";
    private final HashOperations<String, QuotaId, QuotaState> hashOperations;

    public RedisQuotaPersistence(RedisTemplate redisTemplate) {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public QuotaState save(QuotaState quotaState) {
        hashOperations.put(KEY, quotaState.id(), quotaState);
        return quotaState;
    }

    @Override
    public Optional<QuotaState> findById(QuotaId quotaId) {
        return Optional.ofNullable(hashOperations.get(KEY, quotaId));
    }

    public Collection<QuotaState> findAll() {
        return hashOperations.values(KEY);
    }
}
