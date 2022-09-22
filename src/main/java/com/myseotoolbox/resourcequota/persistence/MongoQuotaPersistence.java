package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.quota4j.model.QuotaId;
import com.myseotoolbox.quota4j.model.QuotaState;
import com.myseotoolbox.quota4j.persistence.QuotaPersistence;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
public class MongoQuotaPersistence implements QuotaPersistence {

    private final QuotaRepository quotaRepository;

    public MongoQuotaPersistence(QuotaRepository quotaRepository) {
        this.quotaRepository = quotaRepository;
    }

    @Override
    public QuotaState save(QuotaState quotaState) {
        return quotaRepository.save(quotaState);
    }

    @Override
    public Optional<QuotaState> findById(QuotaId quotaId) {
        return quotaRepository.findById(quotaId);
    }

    public Collection<QuotaState> findAll() {
        return quotaRepository.findAll();
    }
}
