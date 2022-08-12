package com.myseotoolbox.resourcequota.persistence;

import io.github.quota4j.model.ResourceQuota;
import io.github.quota4j.persistence.ResourceQuotaPersistence;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class CrudResourceQuotaPersistence implements ResourceQuotaPersistence {

    private static final String KEY = "ResourceQuota";
    private final HashOperations<String, String, ResourceQuota> hashOperations;

    public CrudResourceQuotaPersistence(RedisTemplate redisTemplate) {
        hashOperations = redisTemplate.opsForHash();
    }


    @Override
    public ResourceQuota save(ResourceQuota resourceQuota) {
        hashOperations.put(KEY, resourceQuota.id(), resourceQuota);
        return resourceQuota;
    }

    @Override
    public Optional<ResourceQuota> findById(String resourceId) {
        return Optional.ofNullable(hashOperations.get(KEY, resourceId));
    }

    public Collection<ResourceQuota> findAll() {
        return hashOperations.values(KEY);
    }
}
