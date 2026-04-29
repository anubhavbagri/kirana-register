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

@Service
public class UserServiceImp implements UserService {

    private final UserDAO userDao;

    @Autowired
    public UserServiceImp(UserDAO userDao) {
        this.userDao = userDao;
    }

    /**
     * Returns the list of Roles of the specific user
     *
     * @param username
     * @return
     */
    @Override
    public List<String> getUserRolesByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException(USER_NAME_IS_NULL_OR_EMPTY);
        }
        return userDao.findByUsername(username).getRoles();
    }

    /**
     * Returns the userId of the user
     *
     * @param username
     * @return
     */
    @Override
    public String getUserIdByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException(USER_NAME_IS_NULL_OR_EMPTY);
        }
        return userDao.findByUsername(username).getId();
    }

    /**
     * Saves the user into the database
     *
     * @param userRequest
     * @return
     */
    @Override
    @CapitalizeMethod
    public UserRequest save(UserRequest userRequest) {
        if (userRequest == null) {
            throw new IllegalArgumentException(USER_REQUEST_IS_NULL);
        }
        User user = UserDtoUtil.userDTO(userRequest);
        User userExists = userDao.findByUsername(user.getUsername());
        if (userExists != null) {
            throw new UserNameExistsException(USER_ALREADY_EXISTS);
        }
        user.setPassword(new BCryptPasswordEncoder().encode(userRequest.getPassword()));
        return UserDtoUtil.userEntityDTO(userDao.save(user));
    }
}
