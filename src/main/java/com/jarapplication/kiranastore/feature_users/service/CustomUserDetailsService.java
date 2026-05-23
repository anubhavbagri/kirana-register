package com.jarapplication.kiranastore.feature_users.service;

import static com.jarapplication.kiranastore.feature_users.constants.LogConstants.*;

import com.jarapplication.kiranastore.feature_users.entity.User;
import com.jarapplication.kiranastore.feature_users.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // Marks this class as Business Logic Layer: Creates bean + semantic clarity (“this does business logic”)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired // Explicitly tells Spring: “Use this constructor for dependency injection”
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null) {
            throw new UsernameNotFoundException(USER_NAME_IS_NULL_OR_EMPTY);
        }
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username));
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(USER_DOES_NOT_EXIST);
        }
        String userName = user.get().getUsername();
        List<String> roleNames = user.get().getRoles();
        String password = user.get().getPassword();
        /*
        // Your User entity (MongoDB document):
        User {
            id: "user-123"
            username: "admin"
            password: "$2a$10$..." (BCrypt hash)
            roles: ["ROLE_ADMIN", "ROLE_USER"]
        }
        // Converted to Spring Security's UserDetails
        // Why convert?
        ├─ Spring Security expects UserDetails interface
        ├─ Your User class is MongoDB entity (doesn't implement UserDetails)
        ├─ Adapter design pattern: convert domain model → framework interface
        └─ Now AuthenticationManager can use it
        */
        return org.springframework.security.core.userdetails.User.withUsername(userName)
                .password(password)
                .roles(roleNames.toArray(new String[0]))
                .build();
    }
}
