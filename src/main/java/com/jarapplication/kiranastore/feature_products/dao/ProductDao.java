package com.jarapplication.kiranastore.feature_products.dao;

import com.jarapplication.kiranastore.feature_products.entities.ProductEntity;
import com.jarapplication.kiranastore.feature_products.repository.ProductRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ProductDao {

    private final ProductRepository productRepository;

    @Autowired
    public ProductDao(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieves Page of products of the given type
     *
     * @param category
     * @param page
     * @param size
     * @return
     */
    public Page<ProductEntity> findByType(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByType(category, pageable);
    }

    /**
     * Retrieves Page of products
     *
     * @param page
     * @param size
     * @return
     */
    public Page<ProductEntity> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    /**
     * Adds a product
     *
     * @param productEntity
     * @return
     */
    public ProductEntity save(ProductEntity productEntity) {
        return productRepository.save(productEntity);
    }

    /**
     * Retrieves product by name
     *
     * @param productName
     * @return
     */
    public Optional<ProductEntity> findProductByName(String productName) {
        return productRepository.findProductEntityByName(productName);
    }
}
