package com.jarapplication.kiranastore.feature_users.models;

import com.jarapplication.kiranastore.AOP.annotation.Capitalize;
import java.util.List;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data // ← Lombok annotation eliminates boilerplate(getter/setter/equals/hashCode/toString methods auto-generated at compile Time)
@Component // Generic utility, creates generic bean
public class UserRequest {

    @Capitalize private String username; //Custom AOP annotation
    private String password;
    private List<String> roles;
}
