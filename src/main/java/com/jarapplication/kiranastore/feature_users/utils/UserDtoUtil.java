package com.jarapplication.kiranastore.feature_users.utils;

import com.jarapplication.kiranastore.feature_users.entity.User;
import com.jarapplication.kiranastore.feature_users.models.UserRequest;

/**
 * USER DTO UTIL: Static Mapper Between UserRequest DTO and User Entity
 *
 * WHAT IT DOES:
 * ├─ Converts UserRequest → User entity (DTO to Entity, for MongoDB persistence)
 * ├─ Converts User entity → UserRequest (Entity to DTO, for API responses)
 * └─ Same mapper pattern as BillDtoUtil and ProductDtoUtil
 *
 * WHY IT'S NEEDED:
 * ├─ Layer separation: User entity has DB annotations (@Document, @Id, @Capitalize)
 * │   └─ UserRequest is the client-facing DTO (@Capitalize for AOP, @Component for Spring)
 * ├─ Security: UserRequest can omit sensitive fields (e.g., internal _id)
 * ├─ Flexibility: API shape can differ from DB shape
 * └─ Consistency: Same converter pattern across all features
 *
 * DATA FLOW:
 * ├─ Registration: UserRequest (from JSON) → userDTO() → User entity → MongoDB save
 * └─ Response: User entity (from MongoDB) → userEntityDTO() → UserRequest → JSON response
 *
 * NOTE: Password is copied as-is in both directions
 *       └─ On registration: Hashed AFTER conversion (in UserServiceImp.save())
 *       └─ On response: Returns hashed password (consider omitting for security)
 */
public class UserDtoUtil {

    /**
     * Converts UserRequest DTO → User entity for MongoDB persistence.
     *
     * MAPPING:
     * ├─ UserRequest.username → User.username
     * ├─ UserRequest.password → User.password (plaintext at this point)
     * ├─ UserRequest.roles    → User.roles
     * └─ User.id              → NOT SET (MongoDB auto-generates)
     *
     * @param userRequest ← Client registration data
     * @return User entity ready for password hashing + MongoDB save
     */
    public static User userDTO(UserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(userRequest.getPassword());
        user.setRoles(userRequest.getRoles());
        return user;
    }

    /**
     * Converts User entity → UserRequest DTO for API response.
     *
     * MAPPING:
     * ├─ User.username → UserRequest.username (UPPERCASE after AOP)
     * ├─ User.password → UserRequest.password (BCrypt hash)
     * ├─ User.roles    → UserRequest.roles
     * └─ User.id       → NOT MAPPED (internal DB field)
     *
     * SECURITY NOTE: Returns BCrypt hashed password in response
     *       └─ Consider: omitting password from response DTO
     *       └─ userRequest.setPassword(null) would hide it
     *
     * @param user ← User entity from MongoDB
     * @return UserRequest DTO for API response
     */
    public static UserRequest userEntityDTO(User user) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(user.getUsername());
        userRequest.setPassword(user.getPassword());
        userRequest.setRoles(user.getRoles());
        return userRequest;
    }
}
