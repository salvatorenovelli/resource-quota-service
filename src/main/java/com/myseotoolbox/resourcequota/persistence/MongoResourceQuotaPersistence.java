package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.quota4j.model.ResourceQuota;
import com.myseotoolbox.quota4j.persistence.ResourceQuotaPersistence;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class MongoResourceQuotaPersistence implements ResourceQuotaPersistence {

    private final ResourceQuotaRepository resourceQuotaRepository;

    public MongoResourceQuotaPersistence(ResourceQuotaRepository resourceQuotaRepository) {
        this.resourceQuotaRepository = resourceQuotaRepository;
    }

    @Override
    public ResourceQuota save(ResourceQuota resourceQuota) {
        return resourceQuotaRepository.save(resourceQuota);
    }

    @Override
    public Optional<ResourceQuota> findById(String resourceId) {
        return resourceQuotaRepository.findById(resourceId);
    }

    public Collection<ResourceQuota> findAll() {
        return resourceQuotaRepository.findAll();
    }
}
