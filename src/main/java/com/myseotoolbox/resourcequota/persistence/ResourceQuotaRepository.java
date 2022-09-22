package com.myseotoolbox.resourcequota.persistence;

import com.myseotoolbox.quota4j.model.ResourceQuota;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResourceQuotaRepository extends MongoRepository<ResourceQuota, String> {
}
