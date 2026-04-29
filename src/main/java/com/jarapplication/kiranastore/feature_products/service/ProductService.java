package com.jarapplication.kiranastore.feature_products.service;

import com.jarapplication.kiranastore.feature_products.models.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductService {
    Page<Product> findByType(String category, int page, int size);

    Optional<Product> findByName(String name);

    Page<Product> getAllProducts(int page, int size);

    Product save(Product product);
}
