package com.jarapplication.kiranastore.feature_users.entity;

import com.jarapplication.kiranastore.AOP.annotation.Capitalize;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data  // ← Lombok annotation 1️⃣
@Document(collection = "users")  // ← MongoDB annotation 2️⃣
public class User {

    @Id private String id; // ← Entity ID annotation 3️⃣
    @Capitalize private String username; // ← Custom AOP annotation 4️⃣
    private String password;
    private List<String> roles;
}

/*
Without @Data, you'd write:
public class User {
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean equals(Object o) {  }
    public int hashCode() {  }
    public String toString() {  }
}

With @Data:
@Data
public class User {
    All above methods auto-generated at compile time
}
Why it's needed:
Eliminates boilerplate (getter/setter/equals/hashCode/toString)
Auto-generated code = less room for errors
Cleaner, more readable class definition


*/