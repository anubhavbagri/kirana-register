package com.jarapplication.kiranastore.feature_transactions.service;

import com.jarapplication.kiranastore.feature_transactions.enums.CurrencyCode;
import org.json.JSONException;

/**
 * CONVERSION SERVICE INTERFACE: Contract for Currency Rate Calculation
 *
 * WHAT IT DOES:
 * ├─ Defines the contract for calculating currency conversion rates
 * ├─ Implemented by ConversionServiceImp (FxRates API + Redis cache)
 * └─ Returns the exchange rate factor to convert FROM a currency TO INR
 *
 * WHY AN INTERFACE?
 * ├─ Same benefits as BillingService interface (see BillingService.java)
 * ├─ Allows swapping FxRates API with another provider without changing callers
 * ├─ Testability: Mock this interface to avoid real API calls in tests
 * └─ Fallback strategy: Could have OfflineConversionService with hardcoded rates
 *
 * USAGE:
 * ├─ BillingServiceImp calls: conversionService.calculate(CurrencyCode.USD)
 * ├─ Returns: exchange rate factor (e.g., 0.012 for INR→USD)
 * └─ Used as: billAmount = totalINR × conversionRate
 */
public interface ConversionService {
    /**
     * Calculates the conversion rate from a given currency to INR.
     *
     * @param currencyCode ← Target currency (e.g., USD, EUR, GBP)
     * @return Exchange rate factor for converting INR to the target currency
     * @throws JSONException if API response parsing fails
     */
    double calculate(CurrencyCode currencyCode) throws JSONException;
}
