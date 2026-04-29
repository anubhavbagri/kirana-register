package com.jarapplication.kiranastore.auth.entity;

import jakarta.persistence.Id;
import java.util.Date;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class RefreshToken {

    @Id String id;

    @Indexed(unique = true)
    private String token;

    @Indexed private String userId;

    private Date timeout;

    private Date createdAt;
}
