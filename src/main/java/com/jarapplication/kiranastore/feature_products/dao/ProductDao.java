package com.jarapplication.kiranastore.feature_products.dao;

import com.jarapplication.kiranastore.feature_products.entities.ProductEntity;
import com.jarapplication.kiranastore.feature_products.repository.ProductRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * PRODUCT DAO: Data Access Object for Product Catalog (MongoDB)
 *
 * WHAT IT DOES:
 * ├─ Wraps ProductRepository with pagination and query abstraction
 * ├─ Constructs Pageable objects from raw page/size parameters
 * └─ Provides CRUD operations for ProductEntity documents
 *
 * WHY IT'S NEEDED:
 * ├─ Same DAO pattern as BillDao and TransactionDao
 * ├─ Encapsulates pagination logic: PageRequest.of(page, size) → Pageable
 * │   └─ Service layer passes raw ints, DAO creates Spring Data Pageable
 * ├─ Single responsibility: DAO handles data access mechanics
 * └─ Extensibility: Add sorting, filtering, or caching logic here
 *
 * PAGINATION (Spring Data Page<T>):
 * ├─ PageRequest.of(page, size): Creates a Pageable with offset + limit
 * │   ├─ page=0, size=10 → SKIP 0, LIMIT 10 (first 10 items)
 * │   ├─ page=1, size=10 → SKIP 10, LIMIT 10 (items 11-20)
 * │   └─ page=2, size=10 → SKIP 20, LIMIT 10 (items 21-30)
 * ├─ Page<T> response contains:
 * │   ├─ content: List<T> of items for this page
 * │   ├─ totalElements: Total count across all pages
 * │   ├─ totalPages: How many pages exist
 * │   └─ number: Current page number
 * └─ MongoDB SKIP/LIMIT: Efficiently handled by MongoDB driver
 *
 * @Component: Spring bean (data access layer)
 */
@Component
public class ProductDao {

    private final ProductRepository productRepository; // ← Spring Data MongoDB repository

    @Autowired
    public ProductDao(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieves a paginated list of products filtered by type/category.
     *
     * MongoDB query (auto-generated): db.products.find({ type: category }).skip(page*size).limit(size)
     *
     * @param category ← Product type to filter (e.g., "grocery", "dairy")
     * @param page     ← 0-based page index
     * @param size     ← Number of items per page
     * @return Page<ProductEntity> with matching products for the requested page
     */
    public Page<ProductEntity> findByType(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // ← Convert raw ints to Spring Pageable
        return productRepository.findByType(category, pageable);
    }

    /**
     * Retrieves a paginated list of ALL products.
     *
     * MongoDB query: db.products.find({}).skip(page*size).limit(size)
     *
     * @param page ← 0-based page index
     * @param size ← Number of items per page
     * @return Page<ProductEntity> with all products for the requested page
     */
    public Page<ProductEntity> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    /**
     * Saves a product to MongoDB "products" collection.
     *
     * ├─ If id is null (new product): MongoDB generates ObjectId → INSERT
     * └─ If id is set (existing): MongoDB performs UPSERT (update or insert)
     *
     * @param productEntity ← Product document to persist
     * @return ProductEntity with auto-generated id (if new)
     */
    public ProductEntity save(ProductEntity productEntity) {
        return productRepository.save(productEntity);
    }

    /**
     * Finds a product by exact name match.
     *
     * USED BY: CalculateBill.calculateBill() → looks up product price during bill calculation
     *
     * MongoDB query (auto-generated): db.products.findOne({ name: productName })
     *
     * Returns Optional to handle "product not found" gracefully:
     * ├─ Optional.isPresent() → product exists, get price
     * └─ Optional.isEmpty() → product not in catalog → throws RuntimeException
     *
     * @param productName ← Exact product name to search for
     * @return Optional<ProductEntity> (empty if not found)
     */
    public Optional<ProductEntity> findProductByName(String productName) {
        return productRepository.findProductEntityByName(productName);
    }
}
