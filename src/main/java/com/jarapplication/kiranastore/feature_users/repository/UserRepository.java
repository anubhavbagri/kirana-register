package com.jarapplication.kiranastore.feature_users.repository;

import com.jarapplication.kiranastore.feature_users.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);
}
