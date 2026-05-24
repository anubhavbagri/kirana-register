package com.jarapplication.kiranastore.feature_products.utils;

import com.jarapplication.kiranastore.feature_products.entities.ProductEntity;
import com.jarapplication.kiranastore.feature_products.models.Product;

/**
 * PRODUCT DTO UTIL: Static Mapper Between Product DTO and Entity
 *
 * WHAT IT DOES:
 * ├─ Converts ProductEntity → Product (Entity to DTO, for API responses)
 * ├─ Converts Product → ProductEntity (DTO to Entity, for DB persistence)
 * └─ Pure static utility (no state, no Spring bean needed)
 *
 * WHY IT'S NEEDED (Same mapper pattern as BillDtoUtil):
 * ├─ Layer separation: Entity has DB annotations, DTO has API/AOP annotations
 * ├─ Data filtering: convertToDTO() excludes internal fields (id, date) from API response
 * │   └─ Client only sees: name, type, price (not MongoDB's internal _id)
 * ├─ Single responsibility: Mapping logic isolated from business logic
 * └─ Reusability: Used in Page.map() for batch conversions
 *
 * DATA FLOW:
 * ├─ API Response: ProductEntity (from MongoDB) → convertToDTO() → Product (to client)
 * └─ API Request: Product (from client JSON) → convertToEntity() → ProductEntity (to MongoDB)
 *
 * NOTE: convertToDTO() does NOT copy id or date fields.
 *       This is a design choice: API clients don't need internal DB identifiers.
 *       If clients need product IDs (for updates/deletes), add id to Product DTO.
 */
public class ProductDtoUtil {

    /**
     * Converts ProductEntity → Product DTO for API responses.
     *
     * MAPPING:
     * ├─ ProductEntity.name  → Product.name
     * ├─ ProductEntity.type  → Product.type
     * ├─ ProductEntity.price → Product.price
     * └─ ProductEntity.id    → NOT MAPPED (internal DB field)
     * └─ ProductEntity.date  → NOT MAPPED (internal audit field)
     *
     * USED IN: Page<ProductEntity>.map(ProductDtoUtil::convertToDTO)
     *          → Method reference used for batch page conversion
     *
     * @param productEntity ← MongoDB document entity
     * @return Product DTO for API response
     */
    public static Product convertToDTO(ProductEntity productEntity) {
        Product product = new Product();
        product.setName(productEntity.getName());
        product.setType(productEntity.getType());
        product.setPrice(productEntity.getPrice());
        return product;
    }

    /**
     * Converts Product DTO → ProductEntity for MongoDB persistence.
     *
     * MAPPING:
     * ├─ Product.name  → ProductEntity.name
     * ├─ Product.type  → ProductEntity.type
     * ├─ Product.price → ProductEntity.price
     * └─ ProductEntity.id → NOT SET (MongoDB auto-generates on save)
     * └─ ProductEntity.date → NOT SET (could be added for audit trail)
     *
     * @param product ← Product DTO from API request body
     * @return ProductEntity ready for MongoDB save
     */
    public static ProductEntity convertToEntity(Product product) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(product.getName());
        productEntity.setType(product.getType());
        productEntity.setPrice(product.getPrice());
        return productEntity;
    }
}
