package com.jarapplication.kiranastore.feature_transactions.service;

/**
 * FX RATES API SERVICE INTERFACE: Contract for External Exchange Rate Data Fetching
 *
 * WHAT IT DOES:
 * ├─ Defines contract for fetching live currency exchange rate data
 * ├─ Implemented by FxRatesApiServiceImp (uses RestTemplate to call FxRates API)
 * └─ Returns raw JSON response as Object (typically String)
 *
 * WHY AN INTERFACE?
 * ├─ Abstraction: Hides external API details from ConversionServiceImp
 * ├─ Testability: Mock this interface → test ConversionService without real HTTP calls
 * ├─ Swappable: Could implement with a different API provider (e.g., Open Exchange Rates)
 * └─ Offline mode: Could have StubFxRatesApiService returning hardcoded rates for dev/testing
 *
 * RETURN TYPE (Object):
 * ├─ Returns Object to keep interface generic
 * ├─ In practice: Returns String (JSON response body from FxRates API)
 * ├─ ConversionServiceImp casts: (String) fxRatesApiService.fetchData()
 * └─ Better design: Return String or a typed DTO → stronger type safety
 */
public interface FxRatesApiService {
    /**
     * Fetches live exchange rate data from an external API.
     *
     * @return Raw API response (JSON string containing exchange rates)
     */
    Object fetchData();
}
