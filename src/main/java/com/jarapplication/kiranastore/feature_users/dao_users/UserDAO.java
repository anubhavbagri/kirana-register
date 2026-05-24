package com.jarapplication.kiranastore.feature_users.dao_users;

import com.jarapplication.kiranastore.AOP.annotation.CapitalizeMethod;
import com.jarapplication.kiranastore.feature_users.entity.User;
import com.jarapplication.kiranastore.feature_users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component // Marks this class as a Spring-managed bean. Broader than @Service or @Repository.
public class UserDAO {

    UserRepository userRepository;

    @Autowired // Explicitly tells Spring: “Use this constructor for dependency injection”
    UserDAO(UserRepository userRepository) {
        this.userRepository = userRepository; // ← Spring injected
    }

    /**
     * @param user
     * @return
     * @CapitalizeMethod -> Custom AOP annotation that intercepts this method and:
     * 1. Before execution: Capitalizes @Capitalize fields in the user object
     * 2. After execution: Returns the saved user
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
