package com.jarapplication.kiranastore.feature_transactions.util;

import com.jarapplication.kiranastore.feature_transactions.entity.BillEntity;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;
import java.util.Date;

/**
 * BILL DTO UTIL: Static Mapper Between Bill-Related Objects
 *
 * WHAT IT DOES:
 * ├─ Converts PurchaseRequest → BillEntity (for MongoDB persistence)
 * ├─ Converts BillEntity → TransactionDto (for service-to-service communication)
 * └─ Pure static utility (no instance needed, no Spring bean)
 *
 * WHY IT'S NEEDED (Mapper/Converter Pattern):
 * ├─ Separation: Entity and DTO have different concerns
 * │   ├─ PurchaseRequest: Client input (what the API receives)
 * │   ├─ BillEntity: Database document (what MongoDB stores)
 * │   └─ TransactionDto: Internal transport (what services exchange)
 * ├─ Single responsibility: Mapping logic isolated from business logic
 * ├─ Reusability: Same mapping used wherever conversion is needed
 * └─ Testability: Static methods → easy to unit test without Spring context
 *
 * DATA FLOW:
 * ├─ billEntityDTO():     PurchaseRequest + billAmount → BillEntity
 * │   └─ Used in BillingServiceImp.generateBills() before MongoDB save
 * │
 * └─ toTransactionDto():  BillEntity + amountInINR → TransactionDto
 *    └─ Used in BillingServiceImp.generateBills() after MongoDB save
 *    └─ TransactionDto then goes to TransactionServiceImpl for PostgreSQL save
 *
 * WHY NOT USE A MAPPING LIBRARY (MapStruct, ModelMapper)?
 * ├─ Simple mappings: Only a few fields → manual mapping is clear and efficient
 * ├─ No runtime overhead: Static methods are faster than reflection-based mappers
 * └─ Full control: Custom logic (e.g., billDate = new Date()) is explicit
 */
public class BillDtoUtil {

    /**
     * Converts PurchaseRequest → BillEntity for MongoDB persistence.
     *
     * MAPPING:
     * ├─ PurchaseRequest.userId      → BillEntity.userId
     * ├─ PurchaseRequest.billItems   → BillEntity.billItems
     * ├─ PurchaseRequest.currencyCode → BillEntity.currencyCode
     * ├─ billAmount (param)          → BillEntity.totalAmount
     * └─ new Date()                  → BillEntity.billDate (current timestamp)
     *
     * NOTE: BillEntity.billId is NOT set here → MongoDB auto-generates it on save()
     *
     * @param purchaseRequest ← Client's purchase data
     * @param billAmount      ← Calculated bill total in requested currency
     * @return BillEntity ready to be saved to MongoDB
     */
    public static BillEntity billEntityDTO(PurchaseRequest purchaseRequest, double billAmount) {
        BillEntity billEntity = new BillEntity();
        billEntity.setBillDate(new Date());                           // ← Timestamp of bill creation
        billEntity.setUserId(purchaseRequest.getUserId());            // ← Who is buying
        billEntity.setBillItems(purchaseRequest.getBillItems());      // ← What they're buying
        billEntity.setCurrencyCode(purchaseRequest.getCurrencyCode()); // ← In which currency
        billEntity.setTotalAmount(billAmount);                         // ← How much (in requested currency)
        return billEntity;
    }

    /**
     * Converts BillEntity → TransactionDto for inter-service communication.
     *
     * MAPPING:
     * ├─ BillEntity.userId      → TransactionDto.userId
     * ├─ BillEntity.billId      → TransactionDto.billId (MongoDB-generated)
     * ├─ BillEntity.currencyCode → TransactionDto.currencyCode
     * ├─ BillEntity.totalAmount → TransactionDto.amount (in requested currency)
     * ├─ BillEntity.billItems   → TransactionDto.billItems
     * └─ amountInINR (param)    → TransactionDto.amountInINR (base currency)
     *
     * NOTE: TransactionDto carries BOTH amounts:
     *   - amount: In customer's currency (for response)
     *   - amountInINR: In INR (for PostgreSQL transaction record)
     *
     * @param billEntity ← Saved bill from MongoDB (has auto-generated billId)
     * @param amountInINR ← Total bill amount in INR (before conversion)
     * @return TransactionDto for creating TransactionEntity and PurchaseResponse
     */
    public static TransactionDto toTransactionDto(BillEntity billEntity, double amountInINR) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setUserId(billEntity.getUserId());
        transactionDto.setBillId(billEntity.getBillId());             // ← MongoDB-generated ID
        transactionDto.setCurrencyCode(billEntity.getCurrencyCode());
        transactionDto.setAmount(billEntity.getTotalAmount());        // ← Amount in requested currency
        transactionDto.setBillItems(billEntity.getBillItems());
        transactionDto.setAmountInINR(amountInINR);                   // ← Amount in INR (base)
        return transactionDto;
    }
}
