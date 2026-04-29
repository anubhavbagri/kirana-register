package com.jarapplication.kiranastore.feature_transactions.repository;

import com.jarapplication.kiranastore.feature_transactions.entity.BillEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends MongoRepository<BillEntity, String> {}
