package com.jarapplication.kiranastore.feature_transactions.util;

import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import com.jarapplication.kiranastore.feature_transactions.enums.TransactionType;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseResponse;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;
import java.util.Date;

/**
 * TRANSACTION DTO UTIL: Static Mapper for Transaction-Related Object Conversions
 *
 * WHAT IT DOES:
 * ├─ Converts TransactionDto → TransactionEntity (for PostgreSQL persistence)
 * ├─ Creates TransactionEntity for refund operations (from raw fields)
 * └─ Converts TransactionDto → PurchaseResponse (for API client response)
 *
 * WHY IT'S NEEDED:
 * ├─ Same mapper pattern as BillDtoUtil (see BillDtoUtil.java for detailed explanation)
 * ├─ Keeps mapping logic out of service classes
 * ├─ Three distinct conversions for different use cases:
 * │   ├─ TransactionEntityDTO():   Purchase → PostgreSQL entity
 * │   ├─ toTransactionEntity():   Refund → PostgreSQL entity
 * │   └─ transactionResponseDto(): Either → API response
 *
 * DATA FLOW:
 * ├─ PURCHASE PATH:
 * │   TransactionDto → TransactionEntityDTO() → TransactionEntity → PostgreSQL save
 * │   TransactionDto → transactionResponseDto() → PurchaseResponse → API response
 * │
 * └─ REFUND PATH:
 *    Raw fields → toTransactionEntity() → TransactionEntity → PostgreSQL save
 *
 * NOTE ON NAMING CONVENTION:
 * ├─ TransactionEntityDTO() starts with uppercase (Java convention is lowercase for methods)
 * ├─ This is a static utility method → PascalCase is sometimes used for factory methods
 * └─ Conventional: toTransactionEntity() or createTransactionEntity() would be more standard
 */
public class TransactionDtoUtil {
    /**
     * Converts TransactionDto → TransactionEntity for PURCHASE transactions.
     *
     * MAPPING:
     * ├─ TransactionType.PURCHASE (hardcoded) → transactionType
     * ├─ TransactionDto.billId     → TransactionEntity.billId
     * ├─ TransactionDto.userId     → TransactionEntity.userId
     * └─ TransactionDto.amountInINR → TransactionEntity.amount
     *    └─ NOTE: Stores INR amount (not converted currency amount) in PostgreSQL
     *       This ensures consistent base currency for financial reporting
     *
     * @param transactionDto ← Contains bill details from BillingService
     * @return TransactionEntity ready for PostgreSQL save (transactionId auto-generated)
     */
    public static TransactionEntity TransactionEntityDTO(TransactionDto transactionDto) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setTransactionType(TransactionType.PURCHASE); // ← Always PURCHASE
        transactionEntity.setBillId(transactionDto.getBillId());        // ← Links to MongoDB bill
        transactionEntity.setUserId(transactionDto.getUserId());        // ← Who purchased
        transactionEntity.setAmount(transactionDto.getAmountInINR());   // ← Amount in INR (base)
        return transactionEntity;
    }

    /**
     * Creates a TransactionEntity for REFUND transactions from raw fields.
     *
     * WHY NOT USE TransactionDto?
     * ├─ Refund doesn't generate a new bill → no TransactionDto available
     * ├─ Refund data comes from: existing transaction (billId, userId, amount)
     * └─ Direct field construction is simpler than creating intermediate DTO
     *
     * MAPPING:
     * ├─ billId (param)           → TransactionEntity.billId
     * ├─ userId (param)           → TransactionEntity.userId
     * ├─ TransactionType.REFUND   → transactionType
     * ├─ refundAmount (param)     → TransactionEntity.amount
     * └─ new Date()               → TransactionEntity.date (refund timestamp)
     *
     * @param billId       ← The bill being refunded
     * @param userId       ← User requesting the refund
     * @param refundAmount ← Amount to refund (same as original purchase amount)
     * @return TransactionEntity ready for PostgreSQL save
     */
    public static TransactionEntity toTransactionEntity(
            String billId, String userId, double refundAmount) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setBillId(billId);
        transactionEntity.setUserId(userId);
        transactionEntity.setTransactionType(TransactionType.REFUND); // ← Always REFUND
        transactionEntity.setAmount(refundAmount);
        transactionEntity.setDate(new Date()); // ← Refund timestamp
        return transactionEntity;
    }

    /**
     * Converts TransactionDto → PurchaseResponse for API client response.
     *
     * MAPPING:
     * ├─ TransactionDto.billId          → PurchaseResponse.billId
     * ├─ TransactionDto.amount          → PurchaseResponse.amount (in requested currency)
     * ├─ TransactionDto.billItems       → PurchaseResponse.billItems
     * └─ TransactionDto.transactionType → PurchaseResponse.transactionType
     *
     * NOTE: PurchaseResponse.userName is NOT set here
     *       → Controller adds userName from JWT after receiving this response
     *       → TransactionDto doesn't carry userName (it's a controller-level concern)
     *
     * @param transactionDto ← Contains bill + transaction details
     * @return PurchaseResponse for API client (userName added by controller)
     */
    public static PurchaseResponse transactionResponseDto(TransactionDto transactionDto) {
        PurchaseResponse purchaseResponseDto = new PurchaseResponse();
        purchaseResponseDto.setBillId(transactionDto.getBillId());
        purchaseResponseDto.setAmount(transactionDto.getAmount());           // ← In requested currency
        purchaseResponseDto.setBillItems(transactionDto.getBillItems());
        purchaseResponseDto.setTransactionType(transactionDto.getTransactionType());
        return purchaseResponseDto;
    }
}
