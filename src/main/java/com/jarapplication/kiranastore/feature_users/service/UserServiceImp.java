package com.jarapplication.kiranastore.feature_users.service;

import static com.jarapplication.kiranastore.feature_users.constants.LogConstants.*;

import com.jarapplication.kiranastore.AOP.annotation.CapitalizeMethod;
import com.jarapplication.kiranastore.exception.UserNameExistsException;
import com.jarapplication.kiranastore.feature_users.dao_users.UserDAO;
import com.jarapplication.kiranastore.feature_users.entity.User;
import com.jarapplication.kiranastore.feature_users.models.UserRequest;
import com.jarapplication.kiranastore.feature_users.utils.UserDtoUtil;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * USER SERVICE IMPLEMENTATION: Business Logic for User Management
 *
 * WHAT IT DOES:
 * ├─ Manages user registration (save) and role/ID lookups
 * ├─ Validates inputs (null checks) before DB operations
 * ├─ Hashes passwords with BCrypt before persistence
 * ├─ Checks for duplicate usernames (throws UserNameExistsException)
 * └─ Uses @CapitalizeMethod AOP to normalize usernames to UPPERCASE
 *
 * WHY IT'S NEEDED:
 * ├─ User registration: Validates → checks duplicates → hashes password → saves
 * ├─ Role lookup: Used by AuthServiceImp to embed roles in JWT token
 * ├─ UserID lookup: Used by AuthServiceImp to embed userId in JWT token
 * └─ Password security: BCrypt hashing prevents plaintext password storage
 *
 * AOP INTEGRATION (@CapitalizeMethod on save()):
 * ├─ 1. Controller calls userService.save(userRequest)
 * ├─ 2. CapitalizeAspect intercepts (sees @CapitalizeMethod)
 * ├─ 3. Scans UserRequest fields → finds @Capitalize on username
 * ├─ 4. Mutates: username = "john" → "JOHN"
 * ├─ 5. save() method runs with uppercase username
 * ├─ 6. Database stores: { username: "JOHN", ... }
 * └─ 7. Benefit: Case-insensitive username matching
 *
 * EXCEPTION HANDLING:
 * ├─ null userRequest → IllegalArgumentException("Username request is null")
 * ├─ null/empty username → IllegalArgumentException("Username is be null or empty")
 * ├─ Duplicate username → UserNameExistsException("Username already exists")
 * └─ All caught by ExceptionController → consistent error JSON response
 *
 * PASSWORD HASHING:
 * ├─ BCryptPasswordEncoder.encode(password):
 * │   ├─ Input: "password123"
 * │   ├─ Output: "$2a$10$dXJ3SW6G7P50lGmMQgel..." (random salt + hash)
 * │   ├─ Each call produces different output (different salt)
 * │   └─ One-way: Cannot reverse hash → original password
 * └─ Note: Creating new BCryptPasswordEncoder() per call
 *    └─ Improvement: Inject PasswordEncoder bean from SecurityConfig
 *       └─ Reuses singleton, consistent with Spring configuration
 *
 * @Service: Spring bean with business logic semantic
 */
@Service // ← Business logic layer bean
public class UserServiceImp implements UserService {

    private final UserDAO userDao; // ← MongoDB data access (User documents)

    @Autowired
    public UserServiceImp(UserDAO userDao) {
        this.userDao = userDao;
    }

    /**
     * Retrieves the list of roles for a given username.
     *
     * USED BY: AuthServiceImp → embeds roles in JWT token claims
     * FLOW: username → UserDAO.findByUsername() → User entity → getRoles()
     *
     * @param username ← Username to look up (UPPERCASE in DB)
     * @return List of role strings (e.g., ["ADMIN", "USER"])
     * @throws IllegalArgumentException if username is null or empty
     */
    @Override
    public List<String> getUserRolesByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException(USER_NAME_IS_NULL_OR_EMPTY);
        }
        return userDao.findByUsername(username).getRoles();
    }

    /**
     * Retrieves the MongoDB user ID for a given username.
     *
     * USED BY: AuthServiceImp → embeds userId in JWT token claims
     * FLOW: username → UserDAO.findByUsername() → User entity → getId()
     *
     * @param username ← Username to look up
     * @return MongoDB document ID string
     * @throws IllegalArgumentException if username is null or empty
     */
    @Override
    public String getUserIdByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException(USER_NAME_IS_NULL_OR_EMPTY);
        }
        return userDao.findByUsername(username).getId();
    }

    /**
     * Registers a new user in the system.
     *
     * FLOW:
     * ├─ 0. AOP: @CapitalizeMethod → CapitalizeAspect uppercases @Capitalize fields
     * ├─ 1. Validate: userRequest is not null
     * ├─ 2. Convert: UserRequest → User entity (via UserDtoUtil)
     * ├─ 3. Check duplicate: findByUsername → if exists → throw UserNameExistsException
     * ├─ 4. Hash password: BCrypt encode → replaces plaintext password
     * ├─ 5. Save: UserDAO.save() → MongoDB "users" collection
     * └─ 6. Convert back: User entity → UserRequest (for API response)
     *
     * AOP ORDER:
     * ├─ 1. @CapitalizeMethod intercepted BEFORE method body runs
     * ├─ 2. username capitalized: "john" → "JOHN"
     * ├─ 3. Method body executes with capitalized username
     * └─ 4. DB receives: { username: "JOHN", password: "$2a$10$...", roles: ["USER"] }
     *
     * @param userRequest ← Registration data (username, password, roles)
     * @return UserRequest with saved data (uppercase username, hashed password)
     * @throws IllegalArgumentException if userRequest is null
     * @throws UserNameExistsException if username already exists in DB
     */
    @Override
    @CapitalizeMethod // ← AOP: CapitalizeAspect uppercases @Capitalize fields before execution
    public UserRequest save(UserRequest userRequest) {
        if (userRequest == null) {
            throw new IllegalArgumentException(USER_REQUEST_IS_NULL);
        }
        User user = UserDtoUtil.userDTO(userRequest); // ← DTO → Entity conversion
        User userExists = userDao.findByUsername(user.getUsername()); // ← Duplicate check
        if (userExists != null) {
            throw new UserNameExistsException(USER_ALREADY_EXISTS);
        }
        // Hash password before saving (NEVER store plaintext passwords)
        user.setPassword(new BCryptPasswordEncoder().encode(userRequest.getPassword()));
        return UserDtoUtil.userEntityDTO(userDao.save(user)); // ← Save → Entity → DTO
    }
}
