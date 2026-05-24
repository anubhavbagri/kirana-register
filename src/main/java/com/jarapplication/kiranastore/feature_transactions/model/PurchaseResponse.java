package com.jarapplication.kiranastore.feature_transactions.model;

import com.jarapplication.kiranastore.feature_transactions.enums.TransactionType;
import java.util.List;
import lombok.Data;

/**
 * PURCHASE RESPONSE MODEL: Output DTO Returned to Client After Purchase
 *
 * WHAT IT DOES:
 * ├─ Carries purchase confirmation data back to the API client
 * ├─ Built by TransactionDtoUtil.transactionResponseDto() from TransactionDto
 * ├─ Controller adds userName (from JWT) before returning to client
 * └─ Wrapped inside ApiResponse.data field for consistent API format
 *
 * WHY SEPARATE FROM PurchaseRequest?
 * ├─ Request ≠ Response: Different fields needed for input vs output
 * │   ├─ Request has: currencyCode (input parameter)
 * │   ├─ Response has: billId (generated), amount (calculated), transactionType
 * ├─ Security: Response doesn't expose internal fields (userId is not returned)
 * └─ Clarity: Clear separation of input/output contracts
 *
 * CLIENT RESPONSE JSON EXAMPLE:
 * {
 *   "status": "OK",
 *   "data": {
 *     "userName": "JOHN",
 *     "billId": "6657a1b2c3d4e5f6",
 *     "amount": 150.75,
 *     "billItems": [ {"itemName": "Rice", "quantity": 2} ],
 *     "transactionType": "PURCHASE"
 *   }
 * }
 *
 * @Data (Lombok): Auto-generates getters, setters, equals, hashCode, toString
 */
@Data // ← Lombok: getter/setter/equals/hashCode/toString auto-generated
public class PurchaseResponse {
    // User's name (from JWT), added by Controller for display purposes
    // Not available from TransactionDto → Controller enriches this field
    private String userName;

    // MongoDB-generated bill ID linking to BillEntity document
    // Client can use this billId for future refund requests
    private String billId;

    // Total bill amount in the requested currency
    // e.g., if user requested USD and items total ₹1000 → amount = ~12.0 USD
    private double amount;

    // List of purchased items with quantities (mirrors what the client sent)
    // Included for confirmation: "here's what you bought"
    private List<BillItem> billItems;

    // Always PURCHASE for this response (distinguishes from refund)
    private TransactionType transactionType;
}
