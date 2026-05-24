package com.jarapplication.kiranastore.feature_transactions.helper;

import com.jarapplication.kiranastore.feature_products.service.ProductServiceImp;
import com.jarapplication.kiranastore.feature_transactions.model.BillItem;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CALCULATE BILL HELPER: Price Computation Engine
 *
 * WHAT IT DOES:
 * ├─ Takes a PurchaseRequest → calculates total bill amount in INR (base currency)
 * ├─ Looks up each item's price from the product catalog (MongoDB)
 * ├─ Multiplies: price × quantity for each item → sums all line totals
 * └─ Returns the grand total amount (before currency conversion)
 *
 * WHY IT'S NEEDED:
 * ├─ Separation of concerns: Bill calculation is a distinct responsibility
 * │   ├─ BillingServiceImp orchestrates the workflow
 * │   └─ CalculateBill handles the math (single responsibility)
 * ├─ Reusability: If another service needs bill calculation, it can inject this
 * ├─ Testability: Mock ProductServiceImp → test calculation logic in isolation
 * └─ Product catalog integration: Prices come from DB, NOT from client
 *    └─ Security: Client sends item names + quantities, but server looks up prices
 *       └─ Prevents price manipulation by malicious clients
 *
 * ARCHITECTURE POSITION:
 * ├─ BillingServiceImp.generateBills(request)
 * │   ├─ calls CalculateBill.calculateBill(request) → gets total in INR
 * │   ├─ calls ConversionServiceImp.calculate(currency) → gets exchange rate
 * │   ├─ totalInCurrency = totalINR × conversionRate
 * │   └─ saves BillEntity to MongoDB
 *
 * CALCULATION FLOW (Java Streams):
 * ├─ billItems.stream()           → Stream<BillItem>
 * ├─ .map(item -> ...)            → For each item: look up product → calculate subtotal
 * │   ├─ productService.findByName(item.getItemName()) → Optional<Product>
 * │   ├─ .map(product -> product.getPrice() * item.getQuantity()) → subtotal
 * │   └─ .orElseThrow(() -> new RuntimeException("Product not found: " + itemName))
 * ├─ .mapToDouble(Double::doubleValue) → DoubleStream
 * └─ .sum()                       → Grand total
 *
 * EXAMPLE:
 * ├─ Input: [{"itemName": "Rice", "quantity": 2}, {"itemName": "Flour", "quantity": 1}]
 * ├─ DB lookup: Rice = ₹50.0, Flour = ₹40.0
 * ├─ Calculation: (50.0 × 2) + (40.0 × 1) = ₹140.0
 * └─ Returns: 140.0
 *
 * ERROR HANDLING:
 * ├─ If billItems is null or empty → returns 0.0 (no items to calculate)
 * └─ If product not found in DB → throws RuntimeException("Product not found: <name>")
 *    └─ Caught by ExceptionController → client gets error response
 *
 * @Service ANNOTATION:
 * ├─ Registers as Spring-managed bean (singleton scope)
 * └─ Could also be @Component (functionally identical)
 *    └─ @Service used because this contains business logic (semantic clarity)
 */
@Service // ← Registers as Spring bean + signals "this is business logic"
public class CalculateBill {

    private final ProductServiceImp productService; // ← Product catalog service (MongoDB)

    @Autowired // ← Spring injects ProductServiceImp bean
    public CalculateBill(ProductServiceImp productService) {
        this.productService = productService;
    }

    /**
     * Calculates the total bill amount in INR based on the product catalog.
     *
     * SECURITY NOTE: Prices are looked up from the database, NOT from the client request.
     *                Client only provides item names + quantities. The server determines prices.
     *                This prevents price manipulation attacks.
     *
     * @param purchaseRequest ← Contains billItems list (itemName + quantity per item)
     * @return Total bill amount in INR (sum of all price × quantity)
     */
    public double calculateBill(PurchaseRequest purchaseRequest) {
        double totalAmount = 0.0;

        // Guard: If no items, return 0 (empty cart scenario)
        List<BillItem> billItems = purchaseRequest.getBillItems();
        if (billItems == null || billItems.isEmpty()) {
            return totalAmount;
        }

        // Java 8 Streams pipeline:
        // 1. Stream over each BillItem
        // 2. For each item: look up product by name from MongoDB → get price
        // 3. Multiply price × quantity → subtotal per item
        // 4. Sum all subtotals → grand total
        totalAmount +=
                billItems.stream()
                        .map(
                                item ->
                                        productService
                                                .findByName(item.getItemName()) // ← MongoDB query
                                                .map(
                                                        product ->
                                                                product.getPrice()
                                                                        * item.getQuantity()) // ← subtotal
                                                .orElseThrow(
                                                        () ->
                                                                new RuntimeException(
                                                                        "Product not found: "
                                                                                + item
                                                                                        .getItemName()))) // ← fail if not in catalog
                        .mapToDouble(Double::doubleValue) // ← convert Stream<Double> → DoubleStream
                        .sum(); // ← sum all subtotals

        return totalAmount;
    }
}
