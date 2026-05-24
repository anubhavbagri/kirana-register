package com.jarapplication.kiranastore.feature_products.models;

import com.jarapplication.kiranastore.AOP.annotation.Capitalize;
import lombok.Data;

/**
 * PRODUCT MODEL: DTO for Product Data Transfer (Client ↔ Controller ↔ Service)
 *
 * WHAT IT DOES:
 * ├─ Client-facing representation of a product (excludes internal fields like _id)
 * ├─ Used as: API request body (POST /add) and API response body (GET endpoints)
 * └─ Converted to/from ProductEntity by ProductDtoUtil
 *
 * WHY SEPARATE FROM ProductEntity?
 * ├─ Entity has: @Id, @Document, @Indexed → database concerns
 * ├─ Model has: @Capitalize → business logic concerns
 * ├─ Entity may have internal fields (id, date) not needed in API
 * └─ Decoupling: DB schema changes don't break API contract
 *
 * AOP ANNOTATION: @Capitalize
 * ├─ Placed on 'name' field → signals "this field should be UPPERCASED"
 * ├─ Works WITH @CapitalizeMethod on the service method that processes this DTO
 * ├─ FLOW:
 * │   1. Client sends: { "name": "Rice", "type": "grocery", "price": 50.0 }
 * │   2. Controller passes Product to service method marked @CapitalizeMethod
 * │   3. CapitalizeAspect intercepts the method call
 * │   4. Scans Product fields → finds @Capitalize on 'name'
 * │   5. Mutates: name = "Rice" → "RICE"
 * │   6. Service method runs with uppercase name
 * │   7. Product saved to MongoDB with name = "RICE"
 * │
 * ├─ WHY CAPITALIZE PRODUCT NAMES?
 * │   ├─ Consistency: "Rice", "rice", "RICE" all stored as "RICE"
 * │   ├─ Search accuracy: findByName("RICE") always matches
 * │   └─ Prevents duplicates: Can't have "Rice" AND "rice" as separate products
 * │
 * └─ See AOP/annotation/Capitalize.java for full annotation documentation
 *    See AOP/CapitalizeAspect.java for the interceptor implementation
 *
 * @Data (Lombok): Auto-generates getters, setters, equals, hashCode, toString
 */
@Data // ← Lombok: getter/setter/equals/hashCode/toString
public class Product {

    @Capitalize // ← AOP field marker: CapitalizeAspect will uppercase this field
    // when the service method processing this DTO has @CapitalizeMethod annotation
    private String name;

    // Product category/type (e.g., "grocery", "dairy", "snacks")
    // Not @Capitalize → stored as-is (preserves original casing)
    private String type;

    // Product price in INR (base currency)
    // Set by admin when adding product, used by CalculateBill for total calculation
    private double price;
}
