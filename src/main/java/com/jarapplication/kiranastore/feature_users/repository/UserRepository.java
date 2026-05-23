package com.jarapplication.kiranastore.feature_users.repository;

import com.jarapplication.kiranastore.feature_users.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository // Marks this interface as a data access object (DAO) bean
public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);
}
