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

/**
 * CUSTOM USER DETAILS SERVICE: Bridge Between Spring Security & MongoDB User Store
 *
 * WHAT IT DOES:
 * ├─ Implements Spring Security's UserDetailsService interface
 * ├─ Loads user data from MongoDB when Spring Security needs to authenticate
 * ├─ Converts custom User entity → Spring Security's UserDetails interface
 * └─ Used by JwtFilter to validate JWT tokens against stored user data
 *
 * WHY IT'S NEEDED:
 * ├─ Spring Security requires UserDetailsService to authenticate users
 * │   └─ AuthenticationManager calls: userDetailsService.loadUserByUsername(username)
 * ├─ Your User entity doesn't implement UserDetails → needs adapter
 * ├─ Adapter pattern: Converts domain model (User) → framework interface (UserDetails)
 * └─ Without it: Spring Security can't verify user credentials
 *
 * WHERE IT'S USED:
 * ├─ JwtFilter.doFilterInternal():
 * │   └─ userDetailsService.loadUserByUsername(username) → validates user exists
 * │   └─ Called on EVERY authenticated request (per-request validation)
 * │
 * └─ AuthenticationManager.authenticate() (during login):
 *    └─ Spring auto-discovers this service → loads user → compares BCrypt password
 *
 * ADAPTER PATTERN:
 * ├─ Your domain model:
 * │   User { id, username, password, roles: List<String> }  ← MongoDB document
 * │
 * ├─ Spring Security expects:
 * │   UserDetails { getUsername(), getPassword(), getAuthorities() }  ← Framework interface
 * │
 * └─ This service bridges the gap:
 *    User → UserDetails via User.withUsername(username).password(pwd).roles(roles).build()
 *
 * @Service: Registers as Spring bean → auto-discovered by AuthenticationManager
 */
@Service // ← Spring Security auto-detects UserDetailsService implementations
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // ← Direct repo access (no DAO layer)

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username from MongoDB and converts to Spring Security UserDetails.
     *
     * CALLED BY:
     * ├─ Spring AuthenticationManager (during login) → compares passwords
     * └─ JwtFilter (per request) → validates user still exists
     *
     * CONVERSION:
     * ├─ MongoDB User entity → Spring Security UserDetails
     * ├─ User.withUsername(username): Sets the principal name
     * ├─ .password(password): Sets the BCrypt hash for comparison
     * ├─ .roles(roleNames.toArray(...)): Converts List<String> → GrantedAuthority[]
     * │   └─ Spring auto-prefixes with "ROLE_" (e.g., "ADMIN" → "ROLE_ADMIN")
     * └─ .build(): Creates immutable UserDetails instance
     *
     * @param username ← Username to look up in MongoDB
     * @return UserDetails with username, password hash, and roles
     * @throws UsernameNotFoundException if username is null or user doesn't exist
     */
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

        // Adapter: Convert MongoDB User entity → Spring Security UserDetails
        // Spring Security uses this for:
        // ├─ Password comparison (BCrypt match during login)
        // ├─ Role-based authorization (@PreAuthorize checks)
        // └─ SecurityContext population (who is authenticated)
        return org.springframework.security.core.userdetails.User.withUsername(userName)
                .password(password)
                .roles(roleNames.toArray(new String[0])) // ← List<String> → String[]
                .build();
    }
}
