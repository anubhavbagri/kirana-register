package com.jarapplication.kiranastore.feature_users.utils;

import com.jarapplication.kiranastore.feature_users.entity.User;
import com.jarapplication.kiranastore.feature_users.models.UserRequest;

public class UserDtoUtil {

    /**
     * DTO of user to userEntity
     *
     * @param userRequest
     * @return
     */
    public static User userDTO(UserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(userRequest.getPassword());
        user.setRoles(userRequest.getRoles());
        return user;
    }

    /**
     * DTO of userEntity to user
     *
     * @param user
     * @return
     */
    public static UserRequest userEntityDTO(User user) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(user.getUsername());
        userRequest.setPassword(user.getPassword());
        userRequest.setRoles(user.getRoles());
        return userRequest;
    }
}
