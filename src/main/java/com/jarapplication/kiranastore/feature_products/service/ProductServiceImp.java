package com.jarapplication.kiranastore.feature_products.service;

import static com.jarapplication.kiranastore.feature_products.constants.LogConstants.*;

import com.jarapplication.kiranastore.feature_products.dao.ProductDao;
import com.jarapplication.kiranastore.feature_products.entities.ProductEntity;
import com.jarapplication.kiranastore.feature_products.models.Product;
import com.jarapplication.kiranastore.feature_products.utils.ProductDtoUtil;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImp implements ProductService {

    private final ProductDao productDao;

    @Autowired
    public ProductServiceImp(ProductDao productDao) {

        this.productDao = productDao;
    }

    /**
     * Retrieve products by type with pagination
     *
     * @param category
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Product> findByType(String category, int page, int size) {
        if (category == null) {
            throw new IllegalArgumentException(CATEGORY_IS_NULL);
        }
        Page<ProductEntity> productEntities = productDao.findByType(category, page, size);
        return productEntities.map(ProductDtoUtil::convertToDTO);
    }

    /**
     * Retrive products by name
     *
     * @param name
     * @return
     */
    @Override
    public Optional<Product> findByName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(NAME_IS_NULL_OR_EMPTY);
        }
        Optional<ProductEntity> product = productDao.findProductByName(name);
        return product.map(ProductDtoUtil::convertToDTO);
    }

    /**
     * Retrieve all products with pagination
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Product> getAllProducts(int page, int size) {
        Page<ProductEntity> productEntities = productDao.findAll(page, size);
        return productEntities.map(ProductDtoUtil::convertToDTO);
    }

    /**
     * Adding a new product
     *
     * @param product
     * @return
     */
    @Override
    public Product save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException(PRODUCT_IS_NULL);
        }
        ProductEntity productEntity = productDao.save(ProductDtoUtil.convertToEntity(product));
        return ProductDtoUtil.convertToDTO(productEntity);
    }
}
