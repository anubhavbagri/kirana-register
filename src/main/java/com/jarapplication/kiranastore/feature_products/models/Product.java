package com.jarapplication.kiranastore.feature_products.models;

import com.jarapplication.kiranastore.AOP.annotation.Capitalize;
import lombok.Data;

@Data
public class Product {

    @Capitalize private String name;
    private String type;
    private double price;
}
