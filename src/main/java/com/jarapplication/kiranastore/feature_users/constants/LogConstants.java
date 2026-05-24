package com.jarapplication.kiranastore.feature_users.constants;

/**
 * USER LOG CONSTANTS: Validation & Error Messages for User Feature
 *
 * WHAT IT DOES:
 * ├─ Stores error messages for user-related validation failures
 * └─ Used across UserServiceImp, AuthServiceImp, CustomUserDetailsService
 *
 * WHY IT'S NEEDED:
 * ├─ Same centralized constants pattern as other modules
 * ├─ Consistent error messages across all user operations
 * ├─ Searchable: Find all validation sites for a specific error
 * └─ Maintainable: Change message once → updated everywhere
 *
 * WHERE USED:
 * ├─ USER_NAME_OR_PASSWORD_IS_NULL → AuthServiceImp.authenticate() (login null check)
 * ├─ USER_NAME_IS_NULL_OR_EMPTY   → UserServiceImp (role/ID lookup null check)
 * │                                → CustomUserDetailsService.loadUserByUsername()
 * ├─ USER_DOES_NOT_EXIST          → CustomUserDetailsService (user not in DB)
 * ├─ USER_ALREADY_EXISTS          → UserServiceImp.save() (duplicate username check)
 * └─ USER_REQUEST_IS_NULL         → UserServiceImp.save() (null request body check)
 *
 * NOTE: Minor typo in messages: "Username is be null" → should be "Username is null"
 */
public class LogConstants {
    public static final String USER_NAME_OR_PASSWORD_IS_NULL = "Username or password is be null";
    public static final String USER_NAME_IS_NULL_OR_EMPTY = "Username is be null or empty";
    public static final String USER_DOES_NOT_EXIST = "Username doesn't exist";
    public static final String USER_ALREADY_EXISTS = "Username already exists";
    public static final String USER_REQUEST_IS_NULL = "Username request is null";
}
