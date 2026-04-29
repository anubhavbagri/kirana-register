package com.jarapplication.kiranastore.feature_transactions.service;

import com.jarapplication.kiranastore.feature_transactions.enums.CurrencyCode;
import org.json.JSONException;

public interface ConversionService {
    /**
     * calculates the conversion rate
     *
     * @param currencyCode
     * @return
     * @throws JSONException
     */
    double calculate(CurrencyCode currencyCode) throws JSONException;
}
