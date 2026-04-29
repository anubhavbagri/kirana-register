package com.jarapplication.kiranastore.feature_users.dao_users;

import com.jarapplication.kiranastore.AOP.annotation.CapitalizeMethod;
import com.jarapplication.kiranastore.feature_users.entity.User;
import com.jarapplication.kiranastore.feature_users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDAO {

    UserRepository userRepository;

    @Autowired
    UserDAO(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param user
     * @return
     */
    @CapitalizeMethod
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * @param username
     * @return
     */
    @CapitalizeMethod
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
