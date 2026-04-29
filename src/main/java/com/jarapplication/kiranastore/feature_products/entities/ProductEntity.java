package com.jarapplication.kiranastore.feature_products.entities;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "products")
public class ProductEntity {

    @Id private String id;

    @Field("name")
    @Indexed(unique = true)
    @NotNull
    private String name;

    @Field("type")
    private String type;

    @Field("price")
    private double price;

    @Field("date")
    private String date;
}
