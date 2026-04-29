package com.jarapplication.kiranastore.feature_users.service;

import com.jarapplication.kiranastore.feature_users.models.UserRequest;
import java.util.List;

public interface UserService {
    List<String> getUserRolesByUsername(String username);

    String getUserIdByUsername(String username);

    UserRequest save(UserRequest userRequest);
}
