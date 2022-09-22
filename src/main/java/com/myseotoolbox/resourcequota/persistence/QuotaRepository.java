package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.quota4j.model.QuotaId;
import com.myseotoolbox.quota4j.model.QuotaState;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuotaRepository extends MongoRepository<QuotaState, QuotaId> {
}
