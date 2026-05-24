package com.jarapplication.kiranastore.feature_transactions.service;

import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;

/**
 * BILLING SERVICE INTERFACE: Contract for Bill Generation
 *
 * WHAT IT DOES:
 * ├─ Defines the contract for bill generation operations
 * ├─ Implemented by BillingServiceImp (the actual logic)
 * └─ Used by TransactionServiceImpl to generate bills during purchase
 *
 * WHY AN INTERFACE?
 * ├─ Dependency Inversion Principle (SOLID "D"):
 * │   ├─ High-level modules (TransactionService) depend on abstractions (this interface)
 * │   ├─ Not on concrete implementations (BillingServiceImp)
 * │   └─ Allows swapping implementations without changing consumers
 * │
 * ├─ Testability: Mock this interface in unit tests
 * │   └─ Mockito.mock(BillingService.class) → test TransactionService in isolation
 * │
 * ├─ Multiple implementations: Could have different billing strategies
 * │   ├─ BillingServiceImp → current implementation (FxRates API conversion)
 * │   ├─ OfflineBillingService → fallback if API is down (fixed rates)
 * │   └─ Spring @Primary or @Qualifier selects which implementation to inject
 * │
 * └─ Documentation: Interface clearly defines the service contract
 *    └─ Consumers know WHAT it does without knowing HOW
 */
public interface BillingService {
    /**
     * Generates a bill for the purchase request.
     *
     * CONTRACT:
     * ├─ INPUT: PurchaseRequest with userId, currencyCode, and billItems
     * ├─ PROCESS:
     * │   ├─ Calculates total amount from product catalog prices × quantities
     * │   ├─ Converts total from INR to requested currency via FxRates API
     * │   └─ Saves bill document to MongoDB
     * ├─ OUTPUT: TransactionDto with billId, amounts (INR + converted), items
     * └─ THROWS: IllegalArgumentException if request is null
     *
     * @param purchaseRequest ← Items to purchase with currency preference
     * @return TransactionDto containing bill details for transaction creation
     */
    TransactionDto generateBills(PurchaseRequest purchaseRequest);
}
