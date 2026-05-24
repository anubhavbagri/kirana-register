package com.jarapplication.kiranastore.feature_transactions.dao;

import com.jarapplication.kiranastore.feature_transactions.entity.BillEntity;
import com.jarapplication.kiranastore.feature_transactions.repository.BillRepository;
import org.springframework.stereotype.Component;

/**
 * BILL DAO (Data Access Object): Abstraction Layer Between Service and Repository
 *
 * WHAT IT DOES:
 * ├─ Wraps BillRepository operations behind a simpler interface
 * ├─ Currently delegates directly to BillRepository.save()
 * └─ Acts as a "future-proofing" layer for custom query logic
 *
 * WHY IT'S NEEDED (DAO Pattern):
 * ├─ Abstraction: Service layer doesn't directly call repository
 * │   ├─ Service → DAO → Repository (3-tier)
 * │   └─ If repository changes (e.g., MongoDB → PostgreSQL), only DAO changes
 * │
 * ├─ Custom logic: DAOs can add pre/post-processing before DB operations
 * │   ├─ Example: Logging before save, auditing, transformation
 * │   ├─ Repository only provides basic CRUD
 * │   └─ DAO adds business-specific data operations
 * │
 * ├─ Testability: Mock DAO in service tests (simpler than mocking repository)
 * │
 * └─ Consistency: All data access goes through DAO → uniform error handling
 *
 * ARCHITECTURE POSITION:
 * ├─ BillingServiceImp (business logic)
 * │   └─ calls BillDao.save()
 * │       └─ calls BillRepository.save() (Spring Data MongoDB)
 * │           └─ inserts document into MongoDB "bills" collection
 *
 * @Component ANNOTATION:
 * ├─ Registers this class as a Spring-managed bean
 * ├─ Alternative to @Service, @Repository, @Controller
 * │   └─ @Component is generic; @Repository adds DB exception translation
 * │      └─ DAO could also use @Repository for MongoDB exception handling
 * ├─ Singleton scope (default): One instance shared across entire application
 * └─ Auto-detected by @ComponentScan during Spring Boot startup
 *
 * NOTE: BillEntity is stored in MongoDB (uses @Document), not PostgreSQL.
 *       BillRepository extends MongoRepository (not JpaRepository).
 *       This is because bills include nested lists (billItems) which map
 *       better to MongoDB's document model than relational tables.
 */
@Component
public class BillDao {

    private final BillRepository billRepository; // ← Spring Data MongoDB repository

    // Constructor injection: Spring auto-injects BillRepository bean
    // No @Autowired needed on single-constructor classes (Spring 4.3+)
    public BillDao(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * Persists a BillEntity document to MongoDB "bills" collection.
     *
     * WHAT HAPPENS INTERNALLY:
     * ├─ MongoRepository.save() checks if billId is null
     * │   ├─ If null (new): MongoDB generates a unique ObjectId → INSERT operation
     * │   └─ If set (existing): MongoDB performs an UPSERT (update or insert)
     * ├─ Returns the saved entity with the generated billId populated
     * └─ The returned billId is then used in TransactionEntity (PostgreSQL) to link bill → transaction
     *
     * WHY RETURN BillEntity (not void)?
     * ├─ Caller needs the auto-generated billId
     * └─ BillingServiceImp.generateBills() uses it to create TransactionDto
     *
     * @param billEntity ← The bill document to persist (billItems, amount, currency, userId)
     * @return BillEntity with auto-generated billId populated by MongoDB
     */
    public BillEntity save(BillEntity billEntity) {
        return billRepository.save(billEntity);
    }
}
