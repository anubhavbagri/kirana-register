package com.jarapplication.kiranastore.exception;

/**
 * CUSTOM EXCEPTION: UserNameExistsException
 *
 * WHAT IT DOES:
 * ├─ Thrown when attempting to register user with existing username
 * ├─ Caught by ExceptionController.java → Returns standardized error response
 * └─ Communicates business logic error to caller (controller/service)
 *
 * WHY CUSTOM EXCEPTION:
 * ├─ Generic exceptions lose context (IllegalArgumentException is vague)
 * ├─ Custom exception name is self-documenting code
 * ├─ ExceptionController can handle it specifically
 * │   └─ Different custom exceptions → different error codes/messages
 * ├─ Traceable: Stack trace shows exact error point
 * └─ Example flow:
 *    UserServiceImp.save(userRequest):
 *        ├─ Check if username exists in DB
 *        ├─ If YES → throw new UserNameExistsException("Username already taken")
 *        └─ Throws exception UP the call stack
 *            ↑
 *        UserController.register(userRequest):
 *            ├─ Calls UserServiceImp.save()
 *            ├─ Exception propagates up (no try-catch)
 *            └─ DispatcherServlet catches it
 *                ↑
 *        ExceptionController.UserNameExistsException(exception):
 *            ├─ Catches the thrown exception
 *            ├─ Creates ApiResponse with error details
 *            └─ Returns HTTP 200 + error JSON to client
 *
 * EXTENDS RuntimeException (Unchecked Exception):
 * ├─ RuntimeException: Compiler doesn't force try-catch
 * ├─ Checked Exception Example:
 * │   └─ public void process() throws IOException {}  ← Force try-catch
 * │      └─ Leads to try-catch everywhere (boilerplate)
 * │
 * ├─ Unchecked Exception (your approach):
 * │   └─ public void process() throws UserNameExistsException {}  ← Optional
 * │      ├─ Can throw without declaring (clean code)
 * │      └─ But still MUST catch in ExceptionController
 *
 * BEST PRACTICES FOR CUSTOM EXCEPTIONS:
 * ├─ ✓ Extend RuntimeException or Exception
 * ├─ ✓ Name clearly: UserNameExistsException (not "Error1" or "BadThing")
 * ├─ ✓ Add descriptive message
 * ├─ ✓ Handle in global exception handler
 * ├─ ✓ Use specific exceptions for different errors
 * └─ ✗ DON'T: Create exception hierarchy too deep
 *
 * USAGE IN CODE:
 * ├─ In service layer:
 * │   if (userRepository.findByUsername(username) != null) {
 * │       throw new UserNameExistsException("Username already registered");
 * │   }
 * │
 * └─ Message caught and returned to client:
 *    {
 *        "success": false,
 *        "status": "error",
 *        "errorMessage": "Username already registered"
 *    }
 */
public class UserNameExistsException extends RuntimeException {
    public UserNameExistsException(String message) {
        super(message);
    }
}
