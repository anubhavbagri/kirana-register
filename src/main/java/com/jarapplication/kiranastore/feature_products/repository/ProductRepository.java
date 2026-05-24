package com.jarapplication.kiranastore.feature_products.repository;

import com.jarapplication.kiranastore.feature_products.entities.ProductEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * PRODUCT REPOSITORY: Spring Data MongoDB Interface for Product Catalog
 *
 * WHAT IT DOES:
 * ├─ Provides CRUD + custom query operations for ProductEntity in MongoDB
 * ├─ Spring Data auto-generates implementation at runtime
 * └─ Supports derived queries with Pageable for pagination
 *
 * WHY IT'S NEEDED:
 * ├─ Zero boilerplate: Extend MongoRepository → get all CRUD methods free
 * ├─ Derived queries: Method naming convention → auto-generated MongoDB queries
 * └─ Pagination: Pageable parameter → automatic SKIP/LIMIT in MongoDB
 *
 * QUERY METHODS EXPLAINED:
 * ├─ findByType(String type, Pageable pageable):
 * │   ├─ Derived query: Spring Data parses method name
 * │   ├─ "findBy" → db.products.find({
 * │   ├─ "Type" → type: type })
 * │   ├─ Pageable → .skip(page*size).limit(size)
 * │   └─ Returns: Page<ProductEntity> with matching products
 * │
 * ├─ findAll(Pageable pageable):
 * │   ├─ Overrides MongoRepository's findAll to support pagination
 * │   ├─ MongoDB: db.products.find({}).skip(page*size).limit(size)
 * │   └─ Returns: Page<ProductEntity> of all products
 * │
 * └─ findProductEntityByName(String name):
 *    ├─ Derived query: Finds exactly one product by name
 *    ├─ Uses @Indexed(unique=true) index → O(log n) lookup
 *    ├─ Returns Optional → handles "not found" without null
 *    └─ MongoDB: db.products.findOne({ name: name })
 *
 * NOTE: No @Repository annotation needed → Spring Data auto-detects MongoRepository interfaces
 */
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    // MongoRepository<ProductEntity, String>:
    //   ├─ ProductEntity: The document class
    //   └─ String: Type of @Id field

    // Derived query: Find products by type with pagination
    // MongoDB: db.products.find({ type: ? }).skip(offset).limit(size)
    Page<ProductEntity> findByType(String type, Pageable pageable);

    // Paginated findAll: All products with SKIP/LIMIT
    // MongoDB: db.products.find({}).skip(offset).limit(size)
    Page<ProductEntity> findAll(Pageable pageable);

    // Exact name lookup: Uses unique index for fast O(log n) search
    // Returns Optional: empty if product doesn't exist
    // Used by: CalculateBill → look up product price during bill calculation
    Optional<ProductEntity> findProductEntityByName(String name);
}
