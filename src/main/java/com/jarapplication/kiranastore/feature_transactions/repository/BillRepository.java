package com.jarapplication.kiranastore.feature_transactions.repository;

import com.jarapplication.kiranastore.feature_transactions.entity.BillEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * BILL REPOSITORY: Spring Data MongoDB Interface for Bill Persistence
 *
 * WHAT IT DOES:
 * ├─ Provides CRUD operations for BillEntity documents in MongoDB "bills" collection
 * ├─ Spring Data MongoDB auto-generates the implementation at runtime (no code needed!)
 * └─ Currently uses only save() (inherited from MongoRepository)
 *
 * WHY IT'S NEEDED:
 * ├─ Zero boilerplate: Extend MongoRepository → get save(), findById(), findAll(), delete(), count()
 * ├─ No implementation class needed: Spring Data creates proxy at startup
 * └─ Type-safe: Generics define <BillEntity, String> → entity type + ID type
 *
 * HOW SPRING DATA MONGODB WORKS:
 * ├─ You define: interface extends MongoRepository<EntityClass, IdType>
 * ├─ Spring auto-creates: Runtime proxy implementing all CRUD methods
 * ├─ Behind the scenes: Uses MongoTemplate to execute MongoDB queries
 * └─ No SQL, no JPQL: Works with MongoDB's document model natively
 *
 * INHERITED METHODS (from MongoRepository → CrudRepository → Repository):
 * ├─ save(entity)       → Insert or upsert document
 * ├─ findById(id)       → Find document by _id field
 * ├─ findAll()          → Return all documents in collection
 * ├─ deleteById(id)     → Remove document by _id
 * ├─ count()            → Count total documents
 * ├─ existsById(id)     → Check if document exists
 * └─ saveAll(entities)  → Batch insert/upsert
 *
 * COMPARISON WITH TransactionRepository:
 * ├─ BillRepository extends MongoRepository (MongoDB)
 * │   └─ Documents in "bills" collection
 * │   └─ No SQL queries needed
 * │
 * └─ TransactionRepository extends JpaRepository (PostgreSQL)
 *    └─ Rows in "transactions" table
 *    └─ Uses JPQL/SQL queries
 *
 * @Repository ANNOTATION:
 * ├─ Spring stereotype annotation for data access layer
 * ├─ Functionally: Same as @Component (registers as Spring bean)
 * ├─ Additionally: Enables MongoDB exception translation
 * │   └─ MongoDB-specific exceptions → Spring DataAccessException hierarchy
 * │       └─ Makes exception handling database-agnostic
 * └─ Note: @Repository is optional for Spring Data interfaces (auto-detected)
 *    └─ Added here for clarity and convention
 */
@Repository // ← Marks as data access component + enables MongoDB exception translation
public interface BillRepository extends MongoRepository<BillEntity, String> {}
// MongoRepository<BillEntity, String>:
//   ├─ BillEntity: The document class to persist
//   └─ String: The type of the @Id field (billId is String)
//
// No methods declared → uses inherited CRUD from MongoRepository
// To add custom queries, declare methods here:
//   e.g., List<BillEntity> findByUserId(String userId);
//   Spring Data auto-generates: db.bills.find({ userId: ? })
