package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.quota4j.model.Quota;
import com.myseotoolbox.quota4j.persistence.QuotaPersistence;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class MongoResourceQuotaPersistence implements QuotaPersistence {

    private final QuotaRepository quotaRepository;

    public MongoResourceQuotaPersistence(QuotaRepository quotaRepository) {
        this.quotaRepository = quotaRepository;
    }

    @Override
    public Quota save(Quota resourceQuota) {
        return quotaRepository.save(resourceQuota);
    }

    @Override
    public Optional<Quota> findById(String resourceId) {
        return quotaRepository.findById(resourceId);
    }

    public Collection<Quota> findAll() {
        return quotaRepository.findAll();
    }
}
