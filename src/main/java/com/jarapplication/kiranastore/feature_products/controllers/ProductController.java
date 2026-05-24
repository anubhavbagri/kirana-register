package com.jarapplication.kiranastore.feature_products.controllers;

import com.jarapplication.kiranastore.AOP.annotation.RateLimiter;
import com.jarapplication.kiranastore.feature_products.models.Product;
import com.jarapplication.kiranastore.feature_products.service.ProductServiceImp;
import com.jarapplication.kiranastore.response.ApiResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * PRODUCT CONTROLLER: REST API for Product Catalog Management
 *
 * WHAT IT DOES:
 * ├─ Exposes HTTP endpoints for product operations:
 * │   ├─ GET /v1/api/products → List all products (paginated)
 * │   ├─ GET /v1/api/products/type?category=X → Filter by category (paginated)
 * │   └─ POST /v1/api/products/add → Add new product (ADMIN only)
 * ├─ All endpoints require authentication (JwtFilter in SecurityConfig)
 * └─ Returns responses in standardized ApiResponse wrapper
 *
 * WHY IT'S NEEDED:
 * ├─ Product catalog management for the Kirana Store
 * ├─ Browse products by type (e.g., "grocery", "dairy", "snacks")
 * ├─ Admin can add new products to the catalog
 * └─ Pagination prevents loading entire catalog at once (performance)
 *
 * SECURITY:
 * ├─ GET endpoints: Any authenticated user can browse products
 * ├─ POST /add: Only ADMIN role can add products
 * │   └─ @PreAuthorize("hasRole('ADMIN')") → Method-level security
 * │   └─ If non-ADMIN calls this → 403 Access Denied (handled by SecurityConfig)
 * └─ @RateLimiter(limit=5): Rate limits GET all products to 5 requests/minute
 *    └─ Prevents abuse of catalog listing (could be expensive DB query)
 *
 * AOP ANNOTATIONS IN USE:
 * ├─ @RateLimiter(limit=5): Custom AOP annotation → RateLimiterAspect intercepts
 * │   └─ Token bucket: 5 tokens per minute, greedy refill
 * │   └─ If tokens exhausted → throws RateLimitExceededException
 * │   └─ See RateLimiterAspect.java for detailed explanation
 * │
 * └─ @PreAuthorize("hasRole('ADMIN')"): Spring Security method-level authorization
 *    └─ Reads roles from SecurityContext (populated by JwtFilter)
 *    └─ Requires @EnableMethodSecurity in SecurityConfig
 *
 * PAGINATION:
 * ├─ page: 0-based page index (default: 0 = first page)
 * ├─ size: Number of items per page (default: 10)
 * ├─ WHY: Products catalog could have thousands of items
 * │   └─ Loading all at once → slow response + high memory usage
 * ├─ Spring Data Page<T>: Contains content + metadata (totalElements, totalPages)
 * └─ .getContent(): Extracts the List<Product> from the Page wrapper
 *
 * @RestController: @Controller + @ResponseBody (JSON responses)
 * @RequestMapping("/v1/api/products"): Base URL for all product endpoints
 */
@RestController
@RequestMapping("/v1/api/products")
public class ProductController {

    private final ProductServiceImp productService;

    @Autowired
    public ProductController(ProductServiceImp productService) {
        this.productService = productService;
    }

    /**
     * GET PRODUCTS BY TYPE: Filtered + Paginated Product Listing
     *
     * HTTP: GET /v1/api/products/type?category=grocery&page=0&size=10
     *
     * FLOW:
     * ├─ 1. Client sends category filter + pagination params
     * ├─ 2. Service queries MongoDB for matching products
     * ├─ 3. Returns paginated results as List<Product>
     * └─ 4. Wrapped in ApiResponse for consistent API format
     *
     * @param category ← Product type to filter by (e.g., "grocery", "dairy")
     * @param page     ← Page number (0-based, default: 0)
     * @param size     ← Items per page (default: 10)
     * @return ApiResponse containing List<Product> for the requested page
     */
    @GetMapping("/type")
    public ResponseEntity<ApiResponse> getProductsByType(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Product> result = productService.findByType(category, page, size).getContent();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(result);
        apiResponse.setStatus(HttpStatus.OK.name());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * ADD PRODUCT: Admin-Only Product Creation
     *
     * HTTP: POST /v1/api/products/add
     *
     * SECURITY:
     * ├─ @PreAuthorize("hasRole('ADMIN')"): Only users with ADMIN role can access
     * ├─ JWT must contain role = "ADMIN"
     * ├─ Non-ADMIN → 403 Forbidden (SecurityConfig accessDeniedHandler)
     * └─ This is METHOD-LEVEL SECURITY (finer than URL-based security)
     *
     * AOP FLOW (when @Capitalize is on Product.name):
     * ├─ If the service method has @CapitalizeMethod:
     * │   ├─ CapitalizeAspect intercepts
     * │   ├─ Finds @Capitalize on Product.name field
     * │   ├─ Converts name to UPPERCASE before saving
     * │   └─ Ensures consistent product names in DB
     *
     * @param product ← JSON body: { "name": "Rice", "type": "grocery", "price": 50.0 }
     * @return ApiResponse containing the saved Product (with any transformations applied)
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')") // ← Spring Security: Only ADMIN role can execute this
    public ResponseEntity<ApiResponse> addProduct(@RequestBody Product product) {
        Product result = productService.save(product);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(result);
        apiResponse.setStatus(HttpStatus.OK.name());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * GET ALL PRODUCTS: Rate-Limited Paginated Product Listing
     *
     * HTTP: GET /v1/api/products?page=0&size=10
     *
     * RATE LIMITING:
     * ├─ @RateLimiter(limit=5): Max 5 requests per minute to this endpoint
     * ├─ Uses token bucket algorithm (Bucket4j library)
     * ├─ 6th request within a minute → RateLimitExceededException
     * │   └─ Caught by ExceptionController → returns { "status": "429", "errorMessage": "Too many requests" }
     * └─ Tokens refill every minute (greedy refill = all at once)
     *
     * WHY RATE LIMIT THIS ENDPOINT?
     * ├─ Full catalog query could be expensive (scans entire products collection)
     * ├─ Prevents DoS attacks (malicious client spamming GET requests)
     * └─ Protects MongoDB from excessive read load
     *
     * @param page ← Page number (0-based, default: 0)
     * @param size ← Items per page (default: 10)
     * @return ApiResponse containing List<Product> for the requested page
     */
    @GetMapping
    @RateLimiter(limit = 5) // ← Custom AOP annotation: 5 requests/minute max
    public ResponseEntity<ApiResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Product> result = productService.getAllProducts(page, size).getContent();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(result);
        apiResponse.setStatus(HttpStatus.OK.name());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
