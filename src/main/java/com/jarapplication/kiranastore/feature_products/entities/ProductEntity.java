package com.jarapplication.kiranastore.feature_products.entities;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * PRODUCT ENTITY: MongoDB Document for Product Catalog
 *
 * WHAT IT DOES:
 * ├─ Maps to a MongoDB document in the "products" collection
 * ├─ Represents a single product in the Kirana Store catalog
 * └─ Used for: product listing, price lookup during bill calculation
 *
 * WHY MONGODB (not PostgreSQL)?
 * ├─ Product catalog is read-heavy (browse/search products)
 * ├─ Flexible schema: Easy to add new fields (weight, brand, image) without migrations
 * ├─ No complex relationships: Products are independent documents
 * └─ Same DB as BillEntity → bill items can reference product data efficiently
 *
 * ANNOTATIONS:
 * ├─ @Data (Lombok): Auto-generates getters, setters, equals, hashCode, toString
 * ├─ @Document(collection = "products"):
 * │   └─ Spring Data MongoDB: Maps to "products" collection
 * ├─ @Id: MongoDB document primary key (_id field)
 * ├─ @Field("name"): Explicitly maps Java field to MongoDB document field name
 * │   └─ Not strictly needed if Java field name matches MongoDB field name
 * │   └─ Useful for clarity and if field names differ (e.g., Java camelCase vs MongoDB snake_case)
 * ├─ @Indexed(unique = true): Creates a MongoDB unique index on "name" field
 * │   ├─ Prevents duplicate product names (enforced at DB level)
 * │   ├─ Faster lookups: findProductEntityByName() uses this index
 * │   └─ Without index: MongoDB does collection scan (O(n) vs O(log n))
 * └─ @NotNull: Jakarta Validation → name cannot be null
 *    └─ Enforced when @Valid is used on controller/service parameters
 *
 * EXAMPLE MONGODB DOCUMENT:
 * {
 *   "_id": "6657a1b2c3d4e5f6",
 *   "name": "RICE",        ← @Capitalize AOP may uppercase this
 *   "type": "grocery",
 *   "price": 50.0,
 *   "date": "2024-05-24"
 * }
 */
@Data // ← Lombok: getter/setter/equals/hashCode/toString
@Document(collection = "products") // ← MongoDB: Maps to "products" collection
public class ProductEntity {

    @Id // ← MongoDB document primary key (auto-generated ObjectId string)
    private String id;

    @Field("name") // ← Explicitly maps to "name" field in MongoDB document
    @Indexed(unique = true) // ← MongoDB unique index → prevents duplicate product names
    // Index also speeds up: findProductEntityByName() queries
    @NotNull // ← Validation: name cannot be null (enforced with @Valid)
    private String name;

    @Field("type") // ← Product category/type (e.g., "grocery", "dairy", "snacks")
    // Used for filtering: GET /v1/api/products/type?category=grocery
    private String type;

    @Field("price") // ← Product price in INR (base currency)
    // Used in CalculateBill: product.getPrice() * item.getQuantity()
    private double price;

    @Field("date") // ← Date when product was added to catalog
    // Stored as String (consider using Date type for consistency)
    private String date;
}
