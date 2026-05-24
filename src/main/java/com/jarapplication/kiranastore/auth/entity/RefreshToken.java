package com.jarapplication.kiranastore.auth.entity;

import jakarta.persistence.Id;
import java.util.Date;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data // ← Lombok annotation
@Document // ← MongoDB annotation: ← Uses class name as collection name
public class RefreshToken {

    @Id String id;

    @Indexed(unique = true) // Creates MongoDB index on token field with unique constraint.
    private String token;

    @Indexed private String userId; // creates mongoDB index for fast lookups by userId

    private Date timeout;

    private Date createdAt;
}

/*

// Result:
├─ Fast lookups by token (index)
├─ Prevents duplicate tokens (unique constraint)
├─ db.refreshtoken.findOne({ token: "..." }) is fast (O(log n) instead of O(n))
└─ Duplicate insert throws error

Each user's refresh token must be unique
├─ If duplicate tokens exist
├─ Can't tell which user it belongs to
└─ Security risk: token ambiguity

*/