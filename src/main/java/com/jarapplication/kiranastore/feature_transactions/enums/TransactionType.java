package com.jarapplication.kiranastore.feature_transactions.enums;

/**
 * TRANSACTION TYPE ENUM: Classifies Financial Operations
 *
 * WHAT IT DOES:
 * ├─ Defines the two types of financial transactions in the Kirana Store
 * └─ Used in TransactionEntity to categorize each transaction record
 *
 * WHY IT'S NEEDED:
 * ├─ Type safety: Prevents invalid transaction types (e.g., "DELET" typo)
 * ├─ Business logic: TransactionServiceImpl.makeRefund() checks for existing REFUND type
 * │   └─ if (transactionType.equals(TransactionType.REFUND)) → "already refunded"
 * ├─ Reporting: ReportService can filter/aggregate by PURCHASE vs REFUND
 * └─ Database: Stored as STRING in PostgreSQL via @Enumerated(EnumType.STRING)
 *    └─ DB column shows "PURCHASE" or "REFUND" (human-readable)
 *
 * USE CASE:
 * ├─ PURCHASE → Created when user buys items (TransactionServiceImpl.makePurchase)
 * │   └─ TransactionDtoUtil.TransactionEntityDTO() sets type = PURCHASE
 * └─ REFUND  → Created when user refunds a bill (TransactionServiceImpl.makeRefund)
 *     └─ TransactionDtoUtil.toTransactionEntity() sets type = REFUND
 *
 * REFUND VALIDATION FLOW:
 * ├─ User requests refund for billId "abc"
 * ├─ Service finds all transactions with billId "abc"
 * ├─ Loops through transactions:
 * │   ├─ Found PURCHASE? → OK, this bill exists
 * │   └─ Found REFUND? → REJECT (already refunded, prevents double refund)
 * └─ If only PURCHASE found → create new REFUND transaction → success
 */
public enum TransactionType {
    PURCHASE, // ← Money received: Customer bought items from the store
    REFUND    // ← Money returned: Customer returned items / reversed purchase
}
