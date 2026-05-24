package com.jarapplication.kiranastore.feature_users.dao_users;

import com.jarapplication.kiranastore.AOP.annotation.CapitalizeMethod;
import com.jarapplication.kiranastore.feature_users.entity.User;
import com.jarapplication.kiranastore.feature_users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * USER DAO: Data Access Object for User Operations (MongoDB)
 *
 * WHAT IT DOES:
 * ├─ Wraps UserRepository with AOP-enhanced operations
 * ├─ @CapitalizeMethod on save() → CapitalizeAspect uppercases @Capitalize fields
 * ├─ @CapitalizeMethod on findByUsername() → uppercases username before query
 * └─ Bridges UserServiceImp ↔ UserRepository
 *
 * WHY @CapitalizeMethod ON BOTH save() AND findByUsername()?
 * ├─ save(): Uppercase username BEFORE saving → DB stores "JOHN" (not "john")
 * ├─ findByUsername(): Uppercase search term → searches for "JOHN" (not "john")
 * └─ Together: Ensures consistent case-insensitive matching
 *    └─ Client sends "john" → DAO searches for "JOHN" → finds stored "JOHN"
 *
 * NOTE: @CapitalizeMethod on findByUsername() works because:
 * ├─ AOP intercepts the method call
 * ├─ The username parameter is NOT in a DTO (it's a raw String)
 * ├─ However, CapitalizeAspect reads joinPoint.getArgs()[0]
 * │   └─ If arg[0] is a String (not a DTO), the aspect may not find @Capitalize fields
 * │   └─ The capitalize logic specifically checks for @Capitalize on DECLARED FIELDS
 * │   └─ So this may actually be a no-op for String parameters
 * └─ Double-check: CapitalizeAspect iterates entity.getClass().getDeclaredFields()
 *    └─ String.class fields: value, hash, etc. → no @Capitalize annotation
 *    └─ Conclusion: @CapitalizeMethod on findByUsername may be INERT for String params
 *
 * @Component: Spring bean (data access layer)
 */
@Component // ← Spring bean
public class UserDAO {

    UserRepository userRepository; // ← MongoDB repository for User documents

    @Autowired
    UserDAO(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Saves a User entity to MongoDB "users" collection.
     *
     * @CapitalizeMethod: AOP intercepts → uppercases @Capitalize fields (User.username)
     * FLOW: Input User(username="john") → AOP → User(username="JOHN") → MongoDB save
     *
     * @param user ← User entity with potentially lowercase username
     * @return Saved User entity with uppercase username and generated ID
     */
    @CapitalizeMethod // ← AOP: Capitalizes User.username before save
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Finds a User by username in MongoDB.
     *
     * @CapitalizeMethod: Intended to uppercase the search term for case-insensitive matching.
     *                    Note: May be inert for raw String params (see class javadoc above).
     *
     * MongoDB query (auto-generated): db.users.findOne({ username: username })
     *
     * @param username ← Username to search for
     * @return User entity if found, null if not found
     */
    @CapitalizeMethod // ← AOP: Intended to uppercase search term
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
