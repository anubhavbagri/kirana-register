package com.jarapplication.kiranastore.feature_transactions.service;

import com.jarapplication.kiranastore.feature_transactions.dao.BillDao;
import com.jarapplication.kiranastore.feature_transactions.entity.BillEntity;
import com.jarapplication.kiranastore.feature_transactions.helper.CalculateBill;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;
import com.jarapplication.kiranastore.feature_transactions.util.BillDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BILLING SERVICE IMPLEMENTATION: Bill Generation + Currency Conversion Orchestrator
 *
 * WHAT IT DOES:
 * ├─ Orchestrates the full bill generation workflow:
 * │   1. Calculate total from product prices (CalculateBill)
 * │   2. Get currency conversion rate (ConversionServiceImp)
 * │   3. Convert amount to requested currency
 * │   4. Save bill to MongoDB (BillDao)
 * │   5. Return TransactionDto for transaction creation
 * └─ Implements BillingService interface (see BillingService.java for contract)
 *
 * WHY IT'S NEEDED:
 * ├─ Orchestration: Coordinates multiple sub-services into a coherent workflow
 * ├─ Separation: TransactionService doesn't need to know billing details
 * ├─ Single Responsibility: Only handles billing (not transaction persistence)
 * └─ Extensibility: Add tax calculation, discounts, or promotions here
 *
 * DEPENDENCIES (Constructor Injection):
 * ├─ CalculateBill: Computes total from product prices × quantities
 * ├─ ConversionServiceImp: Gets exchange rate from FxRates API (with Redis caching)
 * └─ BillDao: Persists BillEntity to MongoDB "bills" collection
 *
 * WORKFLOW DETAIL:
 * ├─ Step 1: calculateBillhelper.calculateBill(purchaseRequest)
 * │   └─ Looks up each item price from MongoDB products → sum(price × qty)
 * │   └─ Returns: totalAmount in INR (e.g., ₹1000)
 * │
 * ├─ Step 2: conversionService.calculate(currencyCode)
 * │   └─ First checks Redis cache for cached rate
 * │   └─ If cache miss: calls FxRates API → caches result → returns rate
 * │   └─ Returns: conversionRate (e.g., 0.012 for INR→USD)
 * │
 * ├─ Step 3: billAmount = totalAmount × conversionRate
 * │   └─ e.g., ₹1000 × 0.012 = 12.0 USD
 * │
 * ├─ Step 4: billDao.save(BillDtoUtil.billEntityDTO(...))
 * │   └─ Creates BillEntity → saves to MongoDB → returns entity with generated billId
 * │
 * └─ Step 5: BillDtoUtil.toTransactionDto(billEntity, totalAmount)
 *    └─ Converts to TransactionDto → includes both amounts (INR + converted)
 *
 * @Service: Registers as Spring bean + semantically marks as business logic
 */
@Service // ← Spring bean + "I contain business logic" semantic marker
public class BillingServiceImp implements BillingService {

    private final CalculateBill calculateBillhelper;    // ← Price calculation engine
    private final ConversionServiceImp conversionService; // ← Currency conversion (FxRates API + Redis cache)
    private final BillDao billDao;                        // ← MongoDB persistence for bills

    @Autowired // ← Spring injects all three dependencies
    public BillingServiceImp(
            CalculateBill calculateBill, ConversionServiceImp conversionService, BillDao billDao) {
        this.calculateBillhelper = calculateBill;
        this.conversionService = conversionService;
        this.billDao = billDao;
    }

    /**
     * Generates a bill with both INR and user-requested currency amounts.
     *
     * EXAMPLE:
     * ├─ Input: { userId: "123", currencyCode: USD, billItems: [{Rice, 2}, {Flour, 1}] }
     * ├─ Step 1: totalAmount = (50×2) + (40×1) = ₹140 (INR)
     * ├─ Step 2: conversionRate = 0.012 (INR→USD)
     * ├─ Step 3: billAmount = 140 × 0.012 = 1.68 USD
     * ├─ Step 4: Save to MongoDB → billId = "6657a1b2..."
     * └─ Output: TransactionDto { billId, amount=1.68, amountInINR=140, ... }
     *
     * @param purchaseRequest ← Items, quantities, and preferred currency
     * @return TransactionDto with calculated amounts and generated billId
     * @throws IllegalArgumentException if purchaseRequest is null
     */
    @Override
    public TransactionDto generateBills(PurchaseRequest purchaseRequest) {
        // Guard clause: fail fast if request is null
        if (purchaseRequest == null) {
            throw new IllegalArgumentException("Purchase request cannot be null");
        }

        // Step 1: Calculate total bill in INR (base currency)
        double totalAmount = calculateBillhelper.calculateBill(purchaseRequest);

        // Step 2: Get conversion rate from INR to requested currency
        // Returns: how many units of target currency per 1 INR
        double conversionRate = conversionService.calculate(purchaseRequest.getCurrencyCode());

        // Step 3: Convert total to requested currency
        double billAmount = totalAmount * conversionRate;

        // Step 4: Build BillEntity DTO and persist to MongoDB
        // BillDtoUtil.billEntityDTO → creates BillEntity with items, amount, currency, userId
        // billDao.save → inserts into MongoDB "bills" collection → returns entity with generated billId
        BillEntity billEntity =
                billDao.save(BillDtoUtil.billEntityDTO(purchaseRequest, billAmount));

        // Step 5: Convert to TransactionDto (carries both INR and converted amounts)
        // totalAmount → stored as amountInINR (for PostgreSQL transaction record)
        // billEntity.totalAmount → stored as amount (in requested currency)
        return BillDtoUtil.toTransactionDto(billEntity, totalAmount);
    }
}
