package com.jarapplication.kiranastore.feature_transactions.entity;

import com.jarapplication.kiranastore.feature_transactions.enums.TransactionType;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

/**
 * TRANSACTION ENTITY: JPA Entity for Financial Transaction Records (PostgreSQL)
 *
 * WHAT IT DOES:
 * ├─ Maps to the "transactions" table in PostgreSQL database
 * ├─ Records every financial event: PURCHASE or REFUND
 * ├─ Provides ACID-compliant storage for financial data
 * └─ Links to BillEntity (MongoDB) via billId for full purchase details
 *
 * WHY POSTGRESQL (not MongoDB)?
 * ├─ Financial records REQUIRE strong ACID guarantees:
 * │   ├─ Atomicity: All-or-nothing saves (via @Transactional)
 * │   ├─ Consistency: Foreign key relationships, constraints
 * │   ├─ Isolation: Concurrent transactions don't interfere
 * │   └─ Durability: Data survives crashes (WAL logging)
 * ├─ Flat structure: No nested documents needed (simple columns)
 * ├─ Reporting: SQL aggregations (SUM, GROUP BY) for financial reports
 * └─ Compliance: Relational DBs are industry standard for financial data
 *
 * DUAL-DATABASE LINK:
 * ├─ TransactionEntity.billId → BillEntity.billId
 * │   ├─ TransactionEntity (PostgreSQL): Financial record (amount, type, date)
 * │   └─ BillEntity (MongoDB): Purchase details (items, quantities, currency)
 * └─ To get full purchase info: Query TransactionEntity → use billId → query BillEntity
 *
 * ANNOTATIONS:
 * ├─ @Data (Lombok): Auto-generates getters, setters, equals, hashCode, toString
 * ├─ @Entity: JPA annotation marking this as a persistent entity (database table)
 * │   └─ Hibernate scans for @Entity classes → creates/validates tables at startup
 * └─ @Table(name = "transactions"): Explicitly maps to "transactions" table
 *    └─ Without this: Hibernate would use class name "TransactionEntity" as table name
 *
 * JPA vs SPRING DATA MONGODB:
 * ├─ BillEntity uses: @Document (MongoDB) + @Id (Spring Data)
 * ├─ TransactionEntity uses: @Entity + @Table (JPA) + @Id + @Column (Jakarta Persistence)
 * └─ Key difference: JPA requires explicit column mapping; MongoDB auto-maps fields
 */
@Data   // ← Lombok: Auto-generates getter/setter/equals/hashCode/toString
@Entity // ← JPA: Marks this class as a persistent entity (mapped to a DB table)
@Table(name = "transactions") // ← JPA: Explicit table name mapping in PostgreSQL
public class TransactionEntity {

    @Id // ← JPA @Id: Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.UUID) // ← Auto-generates UUID string for each new record
    // GenerationType.UUID: JPA generates a random UUID (e.g., "550e8400-e29b-41d4-a716-446655440000")
    // Other strategies: AUTO (DB decides), IDENTITY (auto-increment), SEQUENCE (DB sequence)
    // WHY UUID? → Globally unique, no sequence contention in distributed systems
    @Column(name = "transaction_id", updatable = false, nullable = false)
    // updatable = false → ID cannot be changed after INSERT (immutable primary key)
    // nullable = false → DB enforces NOT NULL constraint
    private String transactionId;

    @Column(name = "amount") // ← Maps to "amount" column in PostgreSQL
    // Stores the transaction amount in INR (base currency)
    // For PURCHASE: positive amount (money received)
    // For REFUND: positive amount (money returned) → same as original purchase
    private double amount;

    @Column(name = "bill_id") // ← Foreign reference to MongoDB BillEntity.billId
    // Links this transaction to the bill details stored in MongoDB
    // Note: This is NOT a JPA @ManyToOne relationship because bills are in MongoDB (different DB)
    // Instead, it's a logical reference (application-level join, not DB-level foreign key)
    private String billId;

    @Column(name = "user_id") // ← The user who made this transaction
    // Used for: ownership validation during refunds, user transaction history
    private String userId;

    @Enumerated(EnumType.STRING) // ← Stores enum as STRING in DB (not ordinal integer)
    // EnumType.STRING → DB stores "PURCHASE" or "REFUND" (human-readable)
    // EnumType.ORDINAL → DB stores 0 or 1 (fragile: adding new enum values shifts ordinals!)
    // ALWAYS use STRING for production code → safe against enum reordering
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @CreatedDate // ← Spring Data annotation: auto-populates with current timestamp on creation
    // Note: For @CreatedDate to work with JPA, you'd typically need @EnableJpaAuditing
    // Here it's also initialized with new Date() as a fallback
    @Column(name = "date", nullable = false)
    private Date date = new Date(); // ← Defaults to current time if @CreatedDate doesn't activate
}
