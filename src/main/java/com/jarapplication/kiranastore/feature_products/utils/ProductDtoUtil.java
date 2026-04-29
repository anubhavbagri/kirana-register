package com.jarapplication.kiranastore.feature_products.utils;

import com.jarapplication.kiranastore.feature_products.entities.ProductEntity;
import com.jarapplication.kiranastore.feature_products.models.Product;

public class ProductDtoUtil {

    /**
     * transforms from ProductEntity to Product
     *
     * @param productEntity
     * @return
     */
    public static Product convertToDTO(ProductEntity productEntity) {
        Product product = new Product();
        product.setName(productEntity.getName());
        product.setType(productEntity.getType());
        product.setPrice(productEntity.getPrice());
        return product;
    }

    /**
     * Transforms from Product to ProductEntity
     *
     * @param product
     * @return
     */
    public static ProductEntity convertToEntity(Product product) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(product.getName());
        productEntity.setType(product.getType());
        productEntity.setPrice(product.getPrice());
        return productEntity;
    }
}
