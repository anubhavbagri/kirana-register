package com.jarapplication.kiranastore.exception;

/** username doesn't exist Exception */
public class UserNameExistsException extends RuntimeException {
    public UserNameExistsException(String message) {
        super(message);
    }
}
