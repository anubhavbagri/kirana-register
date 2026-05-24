package com.jarapplication.kiranastore.feature_transactions.service;

import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseResponse;
import jakarta.transaction.Transactional;

/**
 * TRANSACTION SERVICE INTERFACE: Contract for Core Financial Operations
 *
 * WHAT IT DOES:
 * ├─ Defines the contract for purchase and refund operations
 * ├─ Implemented by TransactionServiceImpl (the actual logic)
 * └─ Used by TransactionController to delegate business logic
 *
 * WHY AN INTERFACE?
 * ├─ Same benefits as other service interfaces (see BillingService.java)
 * ├─ Dependency Inversion: Controller depends on interface, not implementation
 * ├─ @Transactional on interface method: Declares transactional intent in the contract
 * └─ Testability: Mock TransactionService → test controller without DB
 *
 * @Transactional ON INTERFACE METHOD:
 * ├─ Placed on makePurchase() to declare that this operation MUST be transactional
 * ├─ Implementation (TransactionServiceImpl) also has @Transactional
 * ├─ When both have @Transactional: Implementation's annotation takes precedence
 * ├─ Convention: Prefer @Transactional on implementation (more common in Spring projects)
 * └─ Having it on interface documents the contract: "this operation needs atomicity"
 *
 * See TransactionServiceImpl.java for comprehensive @Transactional deep-dive:
 * ├─ How proxy pattern enables transaction management
 * ├─ Rollback behavior for checked vs unchecked exceptions
 * ├─ Propagation modes (REQUIRED, REQUIRES_NEW, etc.)
 * ├─ Self-invocation gotcha (same-class calls bypass proxy)
 * └─ Performance considerations for long-running transactions
 */
public interface TransactionService {

    /**
     * Processes a refund for a given bill.
     *
     * CONTRACT:
     * ├─ Validates bill exists, user owns it, and not already refunded
     * ├─ Creates a REFUND transaction in PostgreSQL
     * └─ Returns success message or throws exception on failure
     *
     * @param billId ← MongoDB bill ID to refund
     * @param userId ← User requesting the refund (from JWT)
     * @return Success message (e.g., "Refund successful")
     */
    String makeRefund(String billId, String userId);

    /**
     * Processes a purchase transaction.
     *
     * CONTRACT:
     * ├─ Generates bill → converts currency → saves to both DBs
     * └─ All operations are atomic (@Transactional)
     *
     * @param purchaseRequest ← Items, quantities, and currency preference
     * @return PurchaseResponse with billId, amount, and transaction details
     */
    @Transactional // ← Declares: this method requires a database transaction
    PurchaseResponse makePurchase(PurchaseRequest purchaseRequest);
}
