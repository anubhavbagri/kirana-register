package com.jarapplication.kiranastore.feature_transactions.model;

import com.jarapplication.kiranastore.feature_transactions.enums.CurrencyCode;
import com.jarapplication.kiranastore.feature_transactions.enums.TransactionType;
import java.util.List;
import lombok.Data;

/**
 * TRANSACTION DTO: Internal Data Transfer Object Between Service Layers
 *
 * WHAT IT DOES:
 * ├─ Carries transaction data between BillingService and TransactionService
 * ├─ Combines bill details (from MongoDB) with transaction metadata
 * └─ Acts as an intermediate representation before splitting into Entity + Response
 *
 * WHY IT'S NEEDED (DTO Pattern):
 * ├─ Decoupling: BillingService doesn't need to know about TransactionEntity or PurchaseResponse
 * ├─ Data aggregation: Combines fields from multiple sources:
 * │   ├─ userId      → from PurchaseRequest (originally from JWT)
 * │   ├─ billId      → from BillEntity (MongoDB-generated after save)
 * │   ├─ currencyCode → from PurchaseRequest
 * │   ├─ amount      → calculated bill amount in requested currency
 * │   ├─ amountInINR → original amount in INR (base currency)
 * │   └─ billItems   → from PurchaseRequest (items purchased)
 * │
 * └─ Transformation hub: TransactionDtoUtil converts this DTO into:
 *    ├─ TransactionEntity → for PostgreSQL persistence
 *    └─ PurchaseResponse → for API response to client
 *
 * DATA FLOW:
 * ├─ BillingServiceImp.generateBills(purchaseRequest)
 * │   ├─ Calculates bill → converts currency → saves to MongoDB
 * │   └─ Returns: TransactionDto (this class)
 * │
 * ├─ TransactionServiceImpl.makePurchase(purchaseRequest)
 * │   ├─ Receives: TransactionDto from BillingService
 * │   ├─ Converts: TransactionDtoUtil.TransactionEntityDTO(dto) → TransactionEntity
 * │   ├─ Saves: transactionDao.save(entity) → PostgreSQL
 * │   └─ Converts: TransactionDtoUtil.transactionResponseDto(dto) → PurchaseResponse
 * │
 * └─ TransactionController.purchase()
 *    └─ Returns: PurchaseResponse wrapped in ApiResponse
 *
 * WHY BOTH amount AND amountInINR?
 * ├─ amount: Bill total in the customer's requested currency (e.g., 12.0 USD)
 * ├─ amountInINR: Bill total in INR (e.g., ₹1000)
 * ├─ PostgreSQL stores amountInINR (consistent base currency for reporting)
 * └─ Client receives amount in their currency (for display)
 *
 * @Data (Lombok): Auto-generates getters, setters, equals, hashCode, toString
 */
@Data // ← Lombok: getter/setter/equals/hashCode/toString auto-generated
public class TransactionDto {
    // User ID (from JWT via PurchaseRequest)
    private String userId;

    // Bill ID (from MongoDB BillEntity after save)
    // Links this transaction to the full bill details in MongoDB
    private String billId;

    // Currency the customer requested (e.g., USD, EUR)
    private CurrencyCode currencyCode;

    // Total amount in the requested currency (after conversion)
    // e.g., if INR total = ₹1000 and rate = 0.012 → amount = 12.0 USD
    private double amount;

    // Transaction type (PURCHASE or REFUND)
    // Set by TransactionDtoUtil based on operation
    private TransactionType transactionType;

    // Total amount in INR (base currency, before conversion)
    // This is what gets stored in PostgreSQL TransactionEntity.amount
    // Used for consistent reporting regardless of customer's currency choice
    private double amountInINR;

    // List of items purchased (passed through from PurchaseRequest)
    private List<BillItem> billItems;
}
