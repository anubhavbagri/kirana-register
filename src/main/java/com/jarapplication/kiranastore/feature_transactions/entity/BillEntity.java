package com.jarapplication.kiranastore.feature_transactions.entity;

import com.jarapplication.kiranastore.feature_transactions.enums.CurrencyCode;
import com.jarapplication.kiranastore.feature_transactions.model.BillItem;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * BILL ENTITY: MongoDB Document for Bill/Invoice Records
 *
 * WHAT IT DOES:
 * ├─ Maps to a MongoDB document in the "bills" collection
 * ├─ Stores the complete bill: items purchased, total amount, currency, and user info
 * └─ Acts as the source of truth for "what was bought" (items + amounts)
 *
 * WHY MONGODB (not PostgreSQL)?
 * ├─ Bills contain nested lists (billItems) → natural fit for document databases
 * │   ├─ MongoDB: { billItems: [ {itemName: "Rice", qty: 2}, {itemName: "Flour", qty: 1} ] }
 * │   └─ PostgreSQL: Would need separate "bill_items" junction table + JOINs
 * ├─ Flexible schema: Different bills can have different numbers of items
 * └─ Read performance: Entire bill loaded in one document read (no JOINs)
 *
 * WHY SEPARATE FROM TransactionEntity?
 * ├─ BillEntity (MongoDB): WHAT was purchased (items, amounts, currency)
 * ├─ TransactionEntity (PostgreSQL): Financial record (amount, type, date)
 * ├─ Linked by: billId (BillEntity.billId = TransactionEntity.billId)
 * └─ Separation allows:
 *    ├─ MongoDB for flexible bill documents
 *    ├─ PostgreSQL for ACID-compliant financial transactions
 *    └─ Independent scaling of reads vs writes
 *
 * ANNOTATIONS:
 * ├─ @Data (Lombok):
 * │   └─ Auto-generates: getters, setters, equals(), hashCode(), toString()
 * │      └─ See AuthResponse.java for detailed Lombok explanation
 * │
 * └─ @Document(collection = "bills"):
 *    ├─ Spring Data MongoDB annotation (NOT JPA @Entity!)
 *    ├─ Maps this class to MongoDB collection named "bills"
 *    ├─ Each BillEntity instance = one document in the "bills" collection
 *    └─ MongoDB auto-creates the collection on first insert
 *
 * FIELDS EXPLAINED:
 * ├─ billId      → MongoDB auto-generated ObjectId (unique identifier for each bill)
 * ├─ userId      → Links bill to the user who made the purchase (from JWT)
 * ├─ totalAmount → Final bill amount in requested currency (after conversion)
 * ├─ currencyCode → Currency the user requested (INR, USD, EUR, etc.)
 * ├─ billDate    → Timestamp when bill was created (defaults to now)
 * └─ billItems   → List of purchased items with quantities (embedded documents)
 */
@Data // ← Lombok: Auto-generates getter/setter/equals/hashCode/toString
@Document(collection = "bills") // ← Spring Data MongoDB: Maps to "bills" collection in MongoDB
public class BillEntity {

    @Id // ← Spring Data @Id: Marks this field as the document's primary key
    // MongoDB auto-generates a unique ObjectId string when billId is null during save()
    private String billId;

    // The user who made this purchase (extracted from JWT in controller)
    private String userId;

    // Final bill amount AFTER currency conversion
    // e.g., if items cost ₹1000 and user wants USD → totalAmount = 12.0 (in USD)
    private Double totalAmount;

    // Currency the user requested for the bill (default: INR)
    // Used to convert from INR base prices to target currency
    private CurrencyCode currencyCode;

    // Timestamp of bill creation, defaults to current time via new Date()
    // Note: Using java.util.Date (legacy) → consider java.time.Instant for new code
    private Date billDate = new Date();

    // Embedded list of purchased items (stored as sub-documents in MongoDB)
    // Each BillItem has: itemName (String) + quantity (int)
    // MongoDB stores this as an array inside the bill document (no separate collection)
    private List<BillItem> billItems;
}
