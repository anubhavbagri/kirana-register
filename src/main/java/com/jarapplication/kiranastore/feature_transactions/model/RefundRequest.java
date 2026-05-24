package com.jarapplication.kiranastore.feature_transactions.model;

import static com.jarapplication.kiranastore.feature_transactions.constants.LogConstants.BILLID_IS_NULL_OR_EMPTY;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * REFUND REQUEST MODEL: Client Input DTO for Requesting a Refund
 *
 * WHAT IT DOES:
 * ├─ Captures the billId that the client wants to refund
 * ├─ Deserialized from JSON request body by Jackson
 * └─ Passed to TransactionServiceImpl.makeRefund(billId, userId)
 *
 * WHY IT'S NEEDED:
 * ├─ Encapsulation: Single-field wrapper for clean API contract
 * ├─ Validation: @NotEmpty ensures billId is provided
 * └─ Extensibility: Future fields (refund reason, partial amount) can be added
 *
 * CLIENT JSON EXAMPLE:
 * ├─ POST /v1/api/refund
 * │   Headers: { "Authorization": "Bearer eyJhbG..." }
 * │   Body: { "billId": "6657a1b2c3d4e5f6" }
 * │
 * ├─ NOTE: userId comes from JWT (not request body)
 * │   └─ Same security pattern as PurchaseRequest
 *
 * REFUND VALIDATION (in TransactionServiceImpl.makeRefund):
 * ├─ 1. billId must not be null → @NotEmpty validation
 * ├─ 2. Transactions must exist for this billId → "transaction not found"
 * ├─ 3. userId must match transaction owner → "refund failed" (prevents unauthorized refunds)
 * └─ 4. No existing REFUND type → "already refunded" (prevents double refunds)
 *
 * @Data (Lombok): Auto-generates getters, setters, equals, hashCode, toString
 */
@Data // ← Lombok: getter/setter/equals/hashCode/toString
public class RefundRequest {
    @NotEmpty(message = BILLID_IS_NULL_OR_EMPTY) // ← Validation: billId must not be null/empty
    // The MongoDB bill ID to refund (links to BillEntity document)
    // Client obtains this from the PurchaseResponse.billId they received during purchase
    private String billId;
}
