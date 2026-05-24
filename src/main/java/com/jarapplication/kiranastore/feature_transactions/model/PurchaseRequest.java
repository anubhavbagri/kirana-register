package com.jarapplication.kiranastore.feature_transactions.model;

import static com.jarapplication.kiranastore.feature_transactions.constants.LogConstants.USERID_IS_NULL;

import com.jarapplication.kiranastore.feature_transactions.enums.CurrencyCode;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * PURCHASE REQUEST MODEL: Client Input DTO for Making a Purchase
 *
 * WHAT IT DOES:
 * ├─ Captures all data needed to process a purchase from the client
 * ├─ Deserialized from JSON request body by Jackson (Spring auto-converts JSON → Java object)
 * └─ Passed through: Controller → Service → BillingService → CalculateBill
 *
 * WHY IT'S NEEDED:
 * ├─ Encapsulation: Groups related purchase data into one object
 * ├─ Validation: @NotEmpty on userId ensures required field is present
 * ├─ Defaults: currencyCode defaults to INR if client doesn't specify
 * └─ Transport: Carries data from HTTP layer through to business logic
 *
 * CLIENT JSON EXAMPLE:
 * ├─ POST /v1/api/purchase
 * │   Headers: { "Authorization": "Bearer eyJhbG..." }
 * │   Body: {
 * │       "currencyCode": "USD",
 * │       "billItems": [
 * │           { "itemName": "Rice", "quantity": 2 },
 * │           { "itemName": "Flour", "quantity": 1 }
 * │       ]
 * │   }
 * │
 * ├─ NOTE: userId is NOT sent by client!
 * │   └─ Controller extracts userId from JWT token and sets it:
 * │       request.setUserId(jwtUtil.extractUserId(jwt))
 * │   └─ Security: Prevents user impersonation (can't fake userId)
 *
 * ANNOTATIONS:
 * ├─ @Data (Lombok): Auto-generates getters, setters, equals, hashCode, toString
 * ├─ @Builder (Lombok): Generates Builder pattern for object construction
 * │   └─ Usage: PurchaseRequest.builder().userId("123").currencyCode(CurrencyCode.USD).build()
 * │   └─ Useful in tests and service layers for creating instances fluently
 * ├─ @NotEmpty: Jakarta Validation → userId must not be null or empty string
 * │   └─ Triggers when @Valid is used on controller parameter
 * │   └─ Message comes from LogConstants.USERID_IS_NULL
 * └─ @Builder.Default: Tells Lombok's Builder to use this default value
 *    └─ Without it: builder().build() would set currencyCode = null
 *    └─ With it: builder().build() sets currencyCode = CurrencyCode.INR
 */
@Data    // ← Lombok: Auto-generates getter/setter/equals/hashCode/toString
@Builder // ← Lombok: Generates PurchaseRequest.builder().field(value).build() pattern
public class PurchaseRequest {
    @NotEmpty(message = USERID_IS_NULL) // ← Validation: userId must not be null/empty
    // Set by Controller (from JWT), NOT by client
    // Security: Server-derived value prevents userId spoofing
    private String userId;

    @Builder.Default // ← Tells @Builder to use INR as default when building
    // Default currency is INR (Indian Rupee) since this is a Kirana Store
    // Client can override: { "currencyCode": "USD" } to get bill in USD
    private CurrencyCode currencyCode = CurrencyCode.INR;

    // List of items being purchased with quantities
    // Each BillItem has: itemName (String) + quantity (int)
    // Used by CalculateBill to compute total: sum(product.price * item.quantity)
    private List<BillItem> billItems;
}
