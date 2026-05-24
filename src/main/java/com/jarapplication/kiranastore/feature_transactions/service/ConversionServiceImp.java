package com.jarapplication.kiranastore.feature_transactions.service;

import static com.jarapplication.kiranastore.feature_transactions.constants.Constants.RATES;
import static com.jarapplication.kiranastore.feature_transactions.constants.Constants.SUCCESS;
import static com.jarapplication.kiranastore.feature_transactions.constants.LogConstants.*;

import com.jarapplication.kiranastore.cache.CacheService;
import com.jarapplication.kiranastore.feature_transactions.enums.CurrencyCode;
import com.jarapplication.kiranastore.feature_transactions.util.DateUtil;
import java.text.MessageFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CONVERSION SERVICE IMPLEMENTATION: Currency Exchange Rate Calculator with Redis Caching
 *
 * WHAT IT DOES:
 * ├─ Calculates exchange rates between any currency and INR
 * ├─ Uses Redis caching to avoid redundant FxRates API calls
 * ├─ Falls back to live API call on cache miss
 * └─ Caches results with TTL (time-to-live) until end of current minute
 *
 * WHY IT'S NEEDED:
 * ├─ Every purchase needs currency conversion (if not INR)
 * ├─ FxRates API has rate limits → cache to reduce API calls
 * ├─ Performance: Redis lookup (~1ms) vs API call (~200ms)
 * └─ Cost: Many API providers charge per request → caching saves money
 *
 * CACHING STRATEGY:
 * ├─ Cache key format: "{CURRENCY}_INR" (e.g., "USD_INR", "EUR_INR")
 * ├─ Cache value: Exchange rate as String (e.g., "0.012")
 * ├─ TTL: Until end of current minute (rates refresh every minute)
 * │   └─ DateUtil.getEndOfMinute() calculates remaining milliseconds
 * │   └─ Example: If it's 10:30:45 → TTL = 15 seconds (until 10:31:00)
 * ├─ Cache hit: Return cached rate → skip API call (fast path)
 * └─ Cache miss: Call API → parse response → cache result → return rate
 *
 * EXCHANGE RATE CALCULATION:
 * ├─ FxRates API returns rates relative to a base currency (e.g., EUR)
 * │   Response: { "rates": { "INR": 89.5, "USD": 1.07, "GBP": 0.86, ... } }
 * ├─ To convert Currency→INR: rate = baseToCurrency / baseToINR
 * │   Example: USD→INR = 1.07 / 89.5 = 0.01195... (1 INR ≈ 0.012 USD)
 * ├─ This factor is used by BillingServiceImp:
 * │   billAmount = totalINR × conversionRate
 * │   e.g., ₹1000 × 0.012 = 12.0 USD
 * └─ NOTE: The calculation gives "how much of target currency per 1 INR"
 *
 * ERROR HANDLING:
 * ├─ null currencyCode → JSONException("currency code is null")
 * ├─ Invalid currency (not in API rates) → IllegalArgumentException("invalid Currency code")
 * ├─ API returns success=false → RuntimeException("API call unsuccessful")
 * └─ All exceptions propagate to ExceptionController for client-friendly responses
 *
 * DEPENDENCIES:
 * ├─ CacheService: Redis operations (get/set with TTL)
 * └─ FxRatesApiServiceImp: HTTP call to FxRates API
 */
@Service // ← Spring bean with business logic semantic
public class ConversionServiceImp implements ConversionService {

    private final CacheService cacheService;              // ← Redis cache operations
    private final FxRatesApiServiceImp fxRatesApiService;  // ← External API caller

    @Autowired
    public ConversionServiceImp(FxRatesApiServiceImp fxRatesApiService, CacheService cacheService) {
        this.fxRatesApiService = fxRatesApiService;
        this.cacheService = cacheService;
    }

    /**
     * Calculates the exchange rate from INR to the target currency.
     *
     * ALGORITHM:
     * ├─ 1. Check Redis cache for key "{currency}_INR"
     * ├─ 2. If cached → return cached value (fast path, ~1ms)
     * ├─ 3. If not cached → call FxRates API (slow path, ~200ms)
     * ├─ 4. Parse API JSON response
     * ├─ 5. Calculate rate: baseToCurrency / baseToINR
     * ├─ 6. Cache result in Redis with TTL until end of minute
     * └─ 7. Return calculated rate
     *
     * @param currencyCode ← Target currency (e.g., CurrencyCode.USD)
     * @return Exchange rate factor (multiply INR amount by this to get target currency)
     * @throws JSONException if currencyCode is null or API parsing fails
     */
    @Override
    public double calculate(CurrencyCode currencyCode) throws JSONException {
        // Guard: Fail fast if no currency specified
        if (currencyCode == null) {
            throw new JSONException(CURRENCYCODE_IS_NULL);
        }

        // Step 1: Check Redis cache first (fast path)
        // Key format: "USD_INR", "EUR_INR", etc.
        String result =
                cacheService.getValueFromRedis(MessageFormat.format("{0}_INR", currencyCode));
        if (result != null) {
            // Cache HIT → return cached rate without calling external API
            return Double.parseDouble(result);
        }

        // Step 2: Cache MISS → call FxRates API for live rates
        String response = (String) fxRatesApiService.fetchData();
        JSONObject jsonResponse = new JSONObject(response);

        // Step 3: Parse and calculate exchange rate
        if (jsonResponse.getBoolean(SUCCESS)) { // ← Check API returned success=true
            JSONObject rates = jsonResponse.getJSONObject(RATES); // ← Extract rates map

            // Get rate for INR relative to API's base currency
            double baseToINR = rates.getDouble(CurrencyCode.INR.toString());

            // Get rate for target currency relative to API's base currency
            // optDouble returns -1 if currency not found (instead of throwing)
            double baseToCurrency = rates.optDouble(currencyCode.name(), -1);
            if (baseToCurrency == -1) {
                // Currency code not supported by FxRates API
                throw new IllegalArgumentException(INVALID_CURRENCYCODE + currencyCode.name());
            }

            // Calculate: how many units of target currency per 1 INR
            double value = baseToCurrency / baseToINR;

            // Step 4: Cache the result in Redis with TTL until end of current minute
            // This ensures rates are refreshed at least every minute
            cacheService.setValueToRedis(
                    currencyCode + "_INR", String.valueOf(value), DateUtil.getEndOfMinute());

            return baseToCurrency / baseToINR;
        } else {
            // FxRates API returned success=false (server error, rate limit, etc.)
            throw new RuntimeException(API_CALL_UNSUCCESSFUL);
        }
    }
}
