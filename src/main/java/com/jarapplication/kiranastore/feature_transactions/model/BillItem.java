package com.jarapplication.kiranastore.feature_transactions.model;

import lombok.Data;

/**
 * BILL ITEM MODEL: Represents a Single Item in a Purchase Bill
 *
 * WHAT IT DOES:
 * ├─ Represents one line item in a purchase: "2x Rice", "3x Flour", etc.
 * ├─ Embedded inside PurchaseRequest (from client) and BillEntity (in MongoDB)
 * └─ Used for bill calculation: itemName → look up price → multiply by quantity
 *
 * WHY IT'S NEEDED:
 * ├─ Structure: Client sends a LIST of BillItems in PurchaseRequest body
 * │   └─ JSON: { "billItems": [ {"itemName": "Rice", "quantity": 2}, {"itemName": "Flour", "quantity": 1} ] }
 * ├─ Calculation: CalculateBill.calculateBill() iterates over BillItems
 * │   └─ For each item: productService.findByName(itemName).price * quantity → subtotal
 * ├─ Persistence: Stored as embedded sub-documents in MongoDB BillEntity
 * │   └─ MongoDB document: { billItems: [ { itemName: "Rice", quantity: 2 }, ... ] }
 * └─ Reusability: Same BillItem class used in request, entity, and response DTOs
 *
 * DATA FLOW:
 * ├─ Client → PurchaseRequest.billItems (input: what to buy)
 * ├─ CalculateBill → reads billItems → looks up product prices → calculates total
 * ├─ BillEntity.billItems → persisted to MongoDB (stored as embedded documents)
 * └─ PurchaseResponse.billItems → returned to client (confirms what was purchased)
 *
 * @Data (Lombok): Auto-generates getters, setters, equals, hashCode, toString
 *
 * NOTE: This is a simple DTO/POJO with no validation annotations.
 *       itemName is not validated here (validation happens in CalculateBill when
 *       productService.findByName() throws RuntimeException if product not found).
 */
@Data // ← Lombok: getters/setters/equals/hashCode/toString auto-generated
public class BillItem {

    // Name of the product being purchased (e.g., "Rice", "Flour", "Sugar")
    // Must match a product name in MongoDB "products" collection exactly
    // CalculateBill looks up: productService.findByName(itemName)
    private String itemName;

    // Quantity of this item being purchased (e.g., 2 bags of Rice)
    // Used in calculation: product.price * quantity = line item total
    private int quantity;
}
