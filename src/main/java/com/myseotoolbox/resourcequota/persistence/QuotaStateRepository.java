package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.quota4j.model.QuotaState;
import com.myseotoolbox.quota4j.model.QuotaStateId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuotaStateRepository extends MongoRepository<QuotaState, QuotaStateId> { }