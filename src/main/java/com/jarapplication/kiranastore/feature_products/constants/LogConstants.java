package com.jarapplication.kiranastore.feature_products.constants;

/**
 * LOG CONSTANTS: Validation Messages for Product Feature
 *
 * WHAT IT DOES:
 * ├─ Stores error messages for product-related validation failures
 * └─ Used in ProductServiceImp for guard clause exceptions
 *
 * WHY IT'S NEEDED:
 * ├─ Same pattern as feature_transactions/constants/LogConstants
 * ├─ Centralized messages → consistent error responses
 * ├─ Searchable: grep "CATEGORY_IS_NULL" → find all validation sites
 * └─ Package-scoped: Only product-related messages (not mixed with transaction messages)
 *
 * WHERE USED:
 * ├─ CATEGORY_IS_NULL     → ProductServiceImp.findByType() (null category guard)
 * ├─ PRODUCT_IS_NULL      → ProductServiceImp.save() (null product guard)
 * └─ NAME_IS_NULL_OR_EMPTY → ProductServiceImp.findByName() (null/empty name guard)
 */
public class LogConstants {
    // ← Thrown when filtering products by category but category parameter is null
    public static final String CATEGORY_IS_NULL = "category is null";

    // ← Thrown when trying to save a null product object
    public static final String PRODUCT_IS_NULL = "product is null";

    // ← Thrown when searching for a product by name but name is null or empty string
    public static final String NAME_IS_NULL_OR_EMPTY = "Name is null or empty";
}
