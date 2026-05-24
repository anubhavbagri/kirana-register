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

/**
 * PRODUCT SERVICE IMPLEMENTATION: Business Logic for Product Catalog
 *
 * WHAT IT DOES:
 * ├─ Validates inputs (null checks, empty string checks)
 * ├─ Delegates data access to ProductDao
 * ├─ Converts between Product DTO ↔ ProductEntity using ProductDtoUtil
 * └─ Returns Product DTOs (not entities) to maintain layer separation
 *
 * WHY IT'S NEEDED:
 * ├─ Validation layer: Guards against null/empty inputs before DB queries
 * ├─ DTO conversion: Entity → DTO before returning to controller
 * │   └─ Hides database internals (like _id, @Document) from API layer
 * ├─ Page<Entity>.map(): Transforms paginated entities into paginated DTOs
 * │   └─ Preserves pagination metadata while converting content type
 * └─ Single responsibility: Only product business logic
 *
 * DEFENSIVE PROGRAMMING PATTERN:
 * ├─ Every method starts with input validation (fail fast)
 * ├─ Throws IllegalArgumentException for invalid inputs
 * │   └─ Caught by ExceptionController → returns error ApiResponse
 * └─ Example: findByType(null, 0, 10) → throws "category is null"
 *
 * Page<T>.map() PATTERN:
 * ├─ Page<ProductEntity> → Page<Product>
 * ├─ productEntities.map(ProductDtoUtil::convertToDTO)
 * │   ├─ Iterates over each entity in the page
 * │   ├─ Converts each to Product DTO
 * │   └─ Returns new Page with Product type (preserves metadata: totalPages, totalElements)
 * └─ Method reference: ProductDtoUtil::convertToDTO is equivalent to
 *    entity -> ProductDtoUtil.convertToDTO(entity)
 *
 * @Service: Spring bean with business logic semantic
 */
@Service // ← Spring bean
public class ProductServiceImp implements ProductService {

    private final ProductDao productDao;

    @Autowired
    public ProductServiceImp(ProductDao productDao) {

        this.productDao = productDao;
    }

    /**
     * Retrieves products by type/category with pagination.
     *
     * FLOW: category + page/size → validate → DAO query → Page<Entity> → .map() → Page<Product>
     *
     * @param category ← Product type to filter (e.g., "grocery")
     * @param page     ← 0-based page index
     * @param size     ← Items per page
     * @return Page<Product> with matching products
     * @throws IllegalArgumentException if category is null
     */
    @Override
    public Page<Product> findByType(String category, int page, int size) {
        if (category == null) {
            throw new IllegalArgumentException(CATEGORY_IS_NULL);
        }
        Page<ProductEntity> productEntities = productDao.findByType(category, page, size);
        // .map() transforms each ProductEntity to Product DTO, preserving pagination metadata
        return productEntities.map(ProductDtoUtil::convertToDTO);
    }

    /**
     * Retrieves a product by exact name match.
     *
     * USED BY:
     * ├─ CalculateBill.calculateBill() → looks up product price
     * └─ Returns Optional to handle "product not found" gracefully
     *
     * @param name ← Exact product name (e.g., "RICE")
     * @return Optional<Product> (empty if not in catalog)
     * @throws IllegalArgumentException if name is null or empty
     */
    @Override
    public Optional<Product> findByName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(NAME_IS_NULL_OR_EMPTY);
        }
        Optional<ProductEntity> product = productDao.findProductByName(name);
        // .map() transforms Optional<Entity> → Optional<Product> (empty stays empty)
        return product.map(ProductDtoUtil::convertToDTO);
    }

    /**
     * Retrieves all products with pagination.
     *
     * @param page ← 0-based page index
     * @param size ← Items per page
     * @return Page<Product> with all products for the requested page
     */
    @Override
    public Page<Product> getAllProducts(int page, int size) {
        Page<ProductEntity> productEntities = productDao.findAll(page, size);
        return productEntities.map(ProductDtoUtil::convertToDTO);
    }

    /**
     * Saves a new product to the catalog.
     *
     * FLOW:
     * ├─ 1. Validate product is not null
     * ├─ 2. Convert Product DTO → ProductEntity
     * ├─ 3. Save to MongoDB via ProductDao
     * ├─ 4. Convert saved entity back to Product DTO
     * └─ 5. Return DTO to controller
     *
     * NOTE: If @CapitalizeMethod were on this method, the @Capitalize field
     *       (Product.name) would be auto-uppercased by CapitalizeAspect before save.
     *
     * @param product ← Product data from admin request body
     * @return Saved Product DTO (with MongoDB-generated id mapped back)
     * @throws IllegalArgumentException if product is null
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
