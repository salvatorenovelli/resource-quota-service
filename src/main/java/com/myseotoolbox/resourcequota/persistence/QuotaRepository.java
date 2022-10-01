package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.quota4j.model.Quota;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuotaRepository extends MongoRepository<Quota, String> {
}
