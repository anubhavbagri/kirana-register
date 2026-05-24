package com.jarapplication.kiranastore.feature_products.service;

import com.jarapplication.kiranastore.feature_products.models.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;

/**
 * PRODUCT SERVICE INTERFACE: Contract for Product Catalog Operations
 *
 * WHAT IT DOES:
 * ├─ Defines the contract for product CRUD operations
 * ├─ Implemented by ProductServiceImp
 * └─ Used by ProductController and CalculateBill (cross-feature dependency)
 *
 * WHY AN INTERFACE?
 * ├─ Same benefits as other service interfaces (see BillingService.java)
 * ├─ Dependency Inversion: Controllers/services depend on abstraction
 * ├─ Testability: Mock in unit tests
 * └─ Multiple implementations: Could have CachedProductService, etc.
 *
 * CROSS-FEATURE USAGE:
 * ├─ ProductController → calls findByType(), getAllProducts(), save()
 * └─ CalculateBill (feature_transactions) → calls findByName()
 *    └─ This creates a cross-feature dependency: transactions → products
 *    └─ Acceptable: Products are a shared concern (catalog serves multiple features)
 */
public interface ProductService {
    /** Find products by category with pagination */
    Page<Product> findByType(String category, int page, int size);

    /** Find a single product by exact name (for price lookup in billing) */
    Optional<Product> findByName(String name);

    /** Get all products with pagination */
    Page<Product> getAllProducts(int page, int size);

    /** Save a new product to the catalog (ADMIN only) */
    Product save(Product product);
}
