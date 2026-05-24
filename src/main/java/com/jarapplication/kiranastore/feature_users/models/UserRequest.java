package com.jarapplication.kiranastore.feature_users.models;

import com.jarapplication.kiranastore.AOP.annotation.Capitalize;
import java.util.List;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * USER REQUEST DTO: Client-Facing Data Transfer Object for User Operations
 *
 * WHAT IT DOES:
 * ├─ Represents the JSON body for POST /login and POST /register endpoints
 * ├─ Carries: username, password, and roles from client → controller → service
 * └─ @Capitalize on username → AOP uppercases before processing
 *
 * WHY SEPARATE FROM User ENTITY?
 * ├─ User entity: Has @Id, @Document → database concerns
 * ├─ UserRequest: Has @Capitalize → business logic concerns
 * ├─ API isolation: DB schema changes don't break API contract
 * └─ Field control: UserRequest doesn't expose internal _id
 *
 * @Capitalize ON username:
 * ├─ Marks this field for AOP uppercase transformation
 * ├─ When UserServiceImp.save() (has @CapitalizeMethod) receives this DTO:
 * │   ├─ CapitalizeAspect intercepts
 * │   ├─ Scans declared fields → finds @Capitalize on username
 * │   ├─ Mutates: "john" → "JOHN"
 * │   └─ Method proceeds with capitalized username
 * └─ Ensures case-insensitive username storage
 *
 * @Component ON DTO (UNUSUAL):
 * ├─ Makes UserRequest a Spring-managed bean
 * ├─ Not typically needed for DTOs (usually created with `new` or @RequestBody)
 * ├─ @RequestBody creates a NEW instance per request (doesn't use the bean)
 * ├─ Consider: Removing @Component (DTOs as beans is an anti-pattern)
 * └─ In this codebase: Not harmful but unnecessary
 *
 * JSON EXAMPLE:
 * Login:    { "username": "john", "password": "pass123" }
 * Register: { "username": "john", "password": "pass123", "roles": ["USER"] }
 *
 * @Data (Lombok): Generates getters, setters, equals, hashCode, toString
 */
@Data      // ← Lombok: getter/setter/equals/hashCode/toString
@Component // ← Spring bean (unusual for DTO, see notes above)
public class UserRequest {

    @Capitalize // ← AOP: CapitalizeAspect uppercases when @CapitalizeMethod is on caller
    private String username;

    // Plaintext password from client (hashed with BCrypt in UserServiceImp.save())
    // NEVER logged or stored in plaintext
    private String password;

    // User roles for authorization (e.g., ["USER"], ["ADMIN", "USER"])
    // Only relevant for registration (login doesn't use this field)
    private List<String> roles;
}