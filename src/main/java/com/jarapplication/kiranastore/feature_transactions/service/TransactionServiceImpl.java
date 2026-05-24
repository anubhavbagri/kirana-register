package com.jarapplication.kiranastore.feature_transactions.service;

import static com.jarapplication.kiranastore.feature_transactions.constants.Constants.REFUND_SUCCESSFUL;
import static com.jarapplication.kiranastore.feature_transactions.constants.LogConstants.*;
import static com.jarapplication.kiranastore.feature_transactions.util.TransactionDtoUtil.TransactionEntityDTO;
import static com.jarapplication.kiranastore.feature_transactions.util.TransactionDtoUtil.transactionResponseDto;

import com.jarapplication.kiranastore.feature_transactions.dao.TransactionDao;
import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import com.jarapplication.kiranastore.feature_transactions.enums.TransactionType;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseResponse;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;
import com.jarapplication.kiranastore.feature_transactions.util.TransactionDtoUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TRANSACTION SERVICE LAYER: Business Logic for Financial Operations
 *
 * WHAT IT DOES:
 * ├─ Orchestrates complex business logic for purchases & refunds
 * ├─ Validates data before database operations
 * ├─ Ensures atomic operations (all-or-nothing)
 * └─ Communicates with DAO layer for persistence
 *
 * ARCHITECTURE PATTERN: SERVICE LAYER
 * ├─ Controller → Validates HTTP input + routing
 * ├─ Service (YOU ARE HERE) → Business logic + transactions
 * │   └─ makeRefund(): Complex logic (validation + DB save)
 * │   └─ makePurchase(): Complex logic (bill generation + save)
 * ├─ DAO → Data access operations
 * ├─ Entity → JPA @Entity (PostgreSQL mapping)
 * └─ Repository → Spring Data JPA (CRUD basics)
 *
 * WHY SERVICE LAYER:
 * ├─ Separation of concerns: Controllers don't know DB details
 * ├─ Reusability: Can call same service from different controllers
 * ├─ Testability: Mock service for unit tests (no DB needed)
 * ├─ Transaction management: @Transactional in ONE place
 * └─ Complex logic: Business rules live here (not in DB or controller)
 *
 * @Service ANNOTATION:
 * ├─ Spring stereotype = creates @Component bean
 * ├─ Name: @Service (not @Component) makes intent clear
 * ├─ Registration: Automatically found via @ComponentScan
 * ├─ Scope: Singleton (one bean for entire app)
 * └─ Lifecycle: Created on startup, destroyed on shutdown
 *
 * @Transactional ANNOTATION (CRITICAL):
 * ├─ Purpose: Manages database transactions (ACID guarantee)
 * ├─ Scope: Method-level (each @Transactional method = one transaction)
 * ├─ Behavior:
 * │   ├─ START transaction before method
 * │   ├─ Execute all DB operations
 * │   ├─ If exception: ROLLBACK (undo all changes)
 * │   └─ If success: COMMIT (persist all changes)
 * │
 * ├─ EXAMPLE (makeRefund):
 * │   1. Find transactions by billId
 * │   2. Validate ownership (userId matches)
 * │   3. Validate not already refunded
 * │   4. Save new refund transaction
 * │   5. If any step fails → ALL changes rollback
 * │   6. Database sees: No partial refunds (all or nothing)
 * │
 * ├─ WHY NEEDED:
 * │   ├─ Without @Transactional:
 * │   │   1. Save refund transaction (OK)
 * │   │   2. Update user balance (ERROR)
 * │   │   3. Database has: refund saved + user balance NOT updated (inconsistent!)
 * │   │
 * │   ├─ With @Transactional:
 * │   │   1. Save refund transaction (in transaction)
 * │   │   2. Update user balance (in transaction)
 * │   │   3. If error in step 2: Step 1 also rollback (consistent!)
 * │
 * ├─ HOW IT WORKS (PROXY PATTERN):
 * │   ├─ Spring creates proxy class around TransactionServiceImpl
 * │   ├─ @Transactional method call goes to PROXY first
 * │   ├─ Proxy opens database transaction
 * │   ├─ Proxy calls ACTUAL method
 * │   ├─ If exception: ROLLBACK
 * │   ├─ If success: COMMIT
 * │   └─ Proxy returns result to caller
 * │
 * ├─ IMPORTANT: Only works if called from OUTSIDE class
 * │   ├─ ✓ Controller calls service.makeRefund() → @Transactional works
 * │   ├─ ✓ Another service calls this.makeRefund() → @Transactional works
 * │   └─ ✗ this.makeRefund() from within same method → @Transactional bypassed!
 * │      └─ Reason: Proxy only intercepts external calls
 * │      └─ Solution: Create separate method or use dependency injection
 * │
 * └─ PROPAGATION MODES (when to rollback):
 *    ├─ REQUIRED (default): Use existing transaction or create new
 *    ├─ REQUIRES_NEW: Always create new transaction
 *    ├─ SUPPORTS: Use existing or run without transaction
 *    └─ Your code uses: REQUIRED (default behavior)
 *
 * DATABASE ISOLATION:
 * ├─ Transaction ensures no dirty reads (sees committed data only)
 * ├─ Prevents lost updates (concurrent modifications)
 * ├─ Serializable: Most strict isolation (slowest)
 * ├─ Read committed: Typical default (balance between safety + performance)
 * └─ Your DB (PostgreSQL): Default = READ_COMMITTED
 *
 * CONSTRUCTOR INJECTION:
 * ├─ @Autowired on constructor (Spring 5+ supports implicit)
 * ├─ billingService: Generates bills from purchase items
 * ├─ transactionDao: Saves transaction entities to DB
 * ├─ Why constructor injection?
 * │   ├─ Dependencies immutable (final fields)
 * │   ├─ Testable: Easy to inject mocks
 * │   ├─ Clear dependencies: See requirements in constructor
 * │   └─ No null pointer exceptions (fail fast if null)
 * │
 * ├─ Alternative: Field injection (NOT recommended)
 * │   @Autowired
 * │   private BillingServiceImp billingService;  // Can be null if autowiring fails
 * │
 * └─ Constructor injection: (RECOMMENDED)
 *    public TransactionServiceImpl(BillingServiceImp billingService, ...) {
 *        this.billingService = billingService;  // Forced to set
 *    }
 *
 * VALIDATION PATTERN (defensive programming):
 * ├─ Fail fast: Check inputs FIRST before any DB operations
 * ├─ Example (makeRefund):
 * │   1. if (billId == null || userId == null) → throw immediately
 * │   2. Find transactions → if empty → throw immediately
 * │   3. Validate ownership → if mismatch → throw immediately
 * │   4. Only THEN: Save new transaction
 * │
 * └─ WHY: If DB operation fails after state checks, expensive query already happened
 *
 * EXCEPTION HANDLING (@Transactional behavior):
 * ├─ Checked exceptions: DEFAULT = no rollback (only logs)
 * ├─ Unchecked (RuntimeException): DEFAULT = rollback
 * ├─ Your code uses: RuntimeException (triggers automatic rollback)
 * ├─ Example flow:
 * │   makeRefund() throws RuntimeException(\"Bill not found\")
 * │   @Transactional catches → ROLLBACK
 * │   Exception propagates to ExceptionController
 * │   Client gets: HTTP 200 + error response
 * │
 * └─ Best practice: Throw RuntimeException for business errors
 *    └─ Not IllegalStateException (too generic)
 *    └─ Not IOException (checked exception, confusing)
 *
 * DATABASE CONSISTENCY GUARANTEES:
 * ├─ A = Atomicity (all or nothing)
 * │   └─ @Transactional enforces this
 * ├─ C = Consistency (valid state transitions)
 * │   └─ Your validation (checks ownership, prevents double-refunds)
 * ├─ I = Isolation (concurrent requests don't interfere)
 * │   └─ Database engine handles (PostgreSQL)
 * └─ D = Durability (saved data persists after crash)
 *    └─ Database engine handles (PostgreSQL writes to disk)
 *
 * PERFORMANCE CONSIDERATIONS:
 * ├─ @Transactional locks database rows (prevents concurrent modifications)
 * ├─ Long transactions = longer locks = other clients wait
 * ├─ Keep transaction methods SHORT (only what needs ACID)
 * ├─ Example (BAD):
 * │   @Transactional
 * │   public void slowProcess() {
 * │       List<Item> items = httpClient.fetchFromExternalAPI();  // 10 seconds!
 * │       saveItems(items);  // 1 second
 * │   }
 * │   └─ Total: 11 seconds locked transaction (wastes DB resources)
 * │
 * └─ Example (GOOD):
 *    public void slowProcess() {
 *        List<Item> items = httpClient.fetchFromExternalAPI();  // 10 seconds, no lock
 *        saveItems(items);  // Moved to separate @Transactional method (1 second, locked)
 *    }
 *    └─ Total: 1 second transaction (efficient)
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final BillingServiceImp billingService;
    private final TransactionDao transactionDao;

    @Autowired
    public TransactionServiceImpl(BillingServiceImp billingService, TransactionDao transactionDao) {
        this.billingService = billingService;
        this.transactionDao = transactionDao;
    }

    /**
     * checks for validity and makes refund
     *
     * @param billId
     * @param userId
     * @return
     */
    @Transactional
    @Override
    public String makeRefund(String billId, String userId) {
        if (billId == null || userId == null) {
            throw new IllegalArgumentException(BILL_ID_AND_USERID_IS_NULL);
        }
        List<TransactionEntity> transactions = transactionDao.findByBillId(billId);
        if (transactions.isEmpty()) {
            throw new RuntimeException(TRANSACTION_NOT_FOUND);
        }

        for (TransactionEntity transactionEntity : transactions) {
            if (!transactionEntity.getUserId().equals(userId)) {
                throw new RuntimeException(TRANSACTION_REFUND_FAILED);
            }
            if (transactionEntity.getTransactionType().equals(TransactionType.REFUND)) {
                throw new RuntimeException(TRANSACTION_ALREADY_REFUNDED);
            }
        }
        double amount = transactions.getFirst().getAmount();
        transactionDao.save(TransactionDtoUtil.toTransactionEntity(billId, userId, amount));
        return REFUND_SUCCESSFUL;
    }

    /**
     * Makes purchase
     *
     * @param purchaseRequest
     * @return
     */
    @Transactional
    @Override
    public PurchaseResponse makePurchase(PurchaseRequest purchaseRequest) {
        if (purchaseRequest == null) {
            throw new IllegalArgumentException(PURCHASE_REQUEST_IS_NULL);
        }
        TransactionDto transactionDto = billingService.generateBills(purchaseRequest);
        transactionDao.save(TransactionEntityDTO(transactionDto));
        return transactionResponseDto(transactionDto);
    }
}
