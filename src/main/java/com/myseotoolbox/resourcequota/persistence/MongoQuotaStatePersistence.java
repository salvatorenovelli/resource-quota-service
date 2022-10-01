package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.quota4j.model.QuotaState;
import com.myseotoolbox.quota4j.model.QuotaStateId;
import com.myseotoolbox.quota4j.persistence.QuotaStatePersistence;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class MongoQuotaStatePersistence implements QuotaStatePersistence {

    private final QuotaStateRepository quotaStateRepository;

    public MongoQuotaStatePersistence(QuotaStateRepository quotaStateRepository) {
        this.quotaStateRepository = quotaStateRepository;
    }

    @Override
    public QuotaState save(QuotaState quotaState) {
        return quotaStateRepository.save(quotaState);
    }

    @Override
    public Optional<QuotaState> findById(QuotaStateId quotaId) {
        return quotaStateRepository.findById(quotaId);
    }

    public Collection<QuotaState> findAll() {
        return quotaStateRepository.findAll();
    }
}
