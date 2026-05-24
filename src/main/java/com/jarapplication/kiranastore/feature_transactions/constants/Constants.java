package com.jarapplication.kiranastore.feature_transactions.constants;

/**
 * CONSTANTS CLASS: Centralized String Literals for Transaction Feature
 *
 * WHAT IT DOES:
 * ├─ Stores hardcoded string values used across the transaction module
 * ├─ Single source of truth for repeated string literals
 * └─ Prevents typos & inconsistencies when the same string appears in multiple files
 *
 * WHY IT'S NEEDED:
 * ├─ Without constants: Strings scattered across codebase
 * │   ├─ "Authorization" typed in 5 places → 1 typo = 1 silent bug
 * │   ├─ URL changes require find-and-replace across entire project
 * │   └─ No IDE auto-complete for raw strings
 * │
 * ├─ With constants:
 * │   ├─ Change URL in ONE place → reflected everywhere
 * │   ├─ IDE auto-complete: Constants.FX... → full suggestion
 * │   ├─ Compile-time safety: Misspelling constant name → red error
 * │   └─ Refactoring: Rename constant → IDE renames all usages
 *
 * USE CASE:
 * ├─ AUTHORIZATION → Used in TransactionController to extract JWT from request headers
 * ├─ SUCCESS       → Used in ConversionServiceImp to check if FxRates API call succeeded
 * ├─ RATES         → Used in ConversionServiceImp to parse exchange rate data from API JSON
 * ├─ FXRATES_URL   → External API endpoint for fetching live currency exchange rates
 * └─ REFUND_SUCCESSFUL → Response message returned to client after successful refund
 *
 * DESIGN PATTERN: CONSTANT POOL
 * ├─ public: Accessible from other packages (controller, service, etc.)
 * ├─ static: No need to create Constants object → Constants.AUTHORIZATION
 * ├─ final: Immutable → value cannot be changed after compilation
 * └─ Combined: public static final = compile-time constant (inlined by JVM)
 *
 * BEST PRACTICES:
 * ├─ ✓ Group related constants in domain-specific classes (TransactionConstants, UserConstants)
 * ├─ ✓ Use UPPER_SNAKE_CASE naming convention
 * ├─ ✓ Keep constants close to where they're used (feature_transactions package)
 * ├─ ✗ DON'T put ALL app constants in one file (God Object anti-pattern)
 * └─ ✗ DON'T use constants for values that change per environment (use application.properties)
 *    └─ e.g., FXRATES_URL should ideally be in application.properties for dev/staging/prod flexibility
 */
public class Constants {
    // ← Used in TransactionController to read JWT token from HTTP request header
    //    HTTP headers follow convention: "Authorization: Bearer eyJhbG..."
    public static final String AUTHORIZATION = "Authorization";

    // ← Used to verify FxRates API response status (JSON field: {"success": true, ...})
    public static final String SUCCESS = "success";

    // ← JSON key in FxRates API response containing exchange rate map: {"rates": {"INR": 83.5, "USD": 1.0}}
    public static final String RATES = "rates";

    // ← External API URL for fetching latest currency exchange rates
    //    Called by FxRatesApiServiceImp.fetchData() via RestTemplate
    //    Returns JSON: { "success": true, "rates": { "INR": 83.5, "EUR": 0.92, ... } }
    //    NOTE: Ideally this should be in application.properties for environment-specific configuration
    public static final String FXRATES_URL = "https://api.fxratesapi.com/latest";

    // ← Success message returned to client after a refund is processed
    //    Used in TransactionServiceImpl.makeRefund() as the return value
    public static final String REFUND_SUCCESSFUL = "Refund successful";
}
