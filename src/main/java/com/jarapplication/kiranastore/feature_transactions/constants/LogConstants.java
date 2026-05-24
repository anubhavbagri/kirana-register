package com.jarapplication.kiranastore.feature_transactions.constants;

/**
 * LOG CONSTANTS: Centralized Error & Validation Messages for Transaction Feature
 *
 * WHAT IT DOES:
 * ├─ Stores all log/error/validation messages used across the transaction module
 * ├─ Provides consistent error messages for validation failures
 * └─ Used in service/DAO layers for throwing meaningful exceptions
 *
 * WHY IT'S NEEDED:
 * ├─ Consistency: Same error message for same error everywhere
 * ├─ Searchability: grep "TRANSACTION_NOT_FOUND" → find all error sites instantly
 * ├─ i18n ready: Swap constants with resource bundle for multi-language support
 * ├─ Testability: Assert error message in unit tests without hardcoding strings
 * └─ Debugging: Centralized messages make log analysis easier
 *
 * USE CASE IN EXCEPTION FLOW:
 * ├─ Validation fails in Service/DAO layer
 * │   └─ throw new IllegalArgumentException(LogConstants.USERID_IS_NULL)
 * ├─ Exception bubbles up to ExceptionController
 * │   └─ ExceptionController catches it → logs it → returns ApiResponse
 * └─ Client receives: { "success": false, "errorMessage": "userid is null" }
 *
 * WHERE EACH CONSTANT IS USED:
 * ├─ USERID_IS_NULL            → PurchaseRequest.java (@NotEmpty validation message)
 * ├─ BILLID_IS_NULL_OR_EMPTY   → RefundRequest.java (@NotEmpty validation message)
 * ├─ CURRENCYCODE_IS_NULL      → ConversionServiceImp.calculate() (null currency guard)
 * ├─ INVALID_CURRENCYCODE      → ConversionServiceImp.calculate() (currency not in API rates)
 * ├─ API_CALL_UNSUCCESSFUL     → ConversionServiceImp.calculate() (FxRates API returns success=false)
 * ├─ FXRATES_INTERNAL_ERROR    → FxRatesApiServiceImp.fetchData() (RestTemplate HTTP error)
 * ├─ BILL_ID_AND_USERID_IS_NULL → TransactionServiceImpl.makeRefund() (null input guard)
 * ├─ TRANSACTION_NOT_FOUND     → TransactionServiceImpl.makeRefund() (no transactions for billId)
 * ├─ TRANSACTION_REFUND_FAILED → TransactionServiceImpl.makeRefund() (userId doesn't match)
 * ├─ TRANSACTION_ALREADY_REFUNDED → TransactionServiceImpl.makeRefund() (duplicate refund guard)
 * └─ PURCHASE_REQUEST_IS_NULL  → TransactionServiceImpl.makePurchase() (null input guard)
 *
 * BEST PRACTICES:
 * ├─ ✓ Prefix with context: TRANSACTION_*, BILL_*, FXRATES_*
 * ├─ ✓ Use clear, descriptive messages (helps debugging in prod logs)
 * ├─ ✓ Keep messages user-friendly (they may reach the API client)
 * └─ ✗ DON'T include sensitive data in messages (no passwords, tokens, etc.)
 */
public class LogConstants {
    // ← Validation: userId field is missing in PurchaseRequest (used with @NotEmpty)
    public static final String USERID_IS_NULL = "userid is null";

    // ← Validation: billId field is missing in RefundRequest (used with @NotEmpty)
    public static final String BILLID_IS_NULL_OR_EMPTY = "billid is null or empty";

    // ← Guard: currencyCode parameter is null in ConversionServiceImp.calculate()
    public static final String CURRENCYCODE_IS_NULL = "currency code is null";

    // ← Business rule: The provided currency code is not supported by FxRates API
    //    e.g., user passes a fake currency code that doesn't exist in the rates JSON
    public static final String INVALID_CURRENCYCODE = "invalid Currency code";

    // ← External API: FxRates API returned "success": false in JSON response
    public static final String API_CALL_UNSUCCESSFUL = "API call unsuccessful";

    // ← Infrastructure: RestTemplate threw exception when calling FxRates API
    //    Could be: network timeout, DNS failure, 500 from external API, etc.
    public static final String FXRATES_INTERNAL_ERROR =
            "Error fetching data from FxRatesApiServiceImp";

    // ← Guard: Both billId AND userId are null when attempting a refund
    public static final String BILL_ID_AND_USERID_IS_NULL = "billId and userId cannot be null";

    // ← Business rule: No transactions found for the given billId in database
    public static final String TRANSACTION_NOT_FOUND = "transaction not found";

    // ← Security: The userId making the refund request doesn't own the transaction
    //    Prevents: User A refunding User B's purchases
    public static final String TRANSACTION_REFUND_FAILED = "Transaction refund failed";

    // ← Business rule: Transaction already has a REFUND entry (prevents double-refunding)
    public static final String TRANSACTION_ALREADY_REFUNDED = "Transaction already refunded";

    // ← Guard: PurchaseRequest object itself is null in makePurchase()
    public static final String PURCHASE_REQUEST_IS_NULL = "purchase request is null";
}
