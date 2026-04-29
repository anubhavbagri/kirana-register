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

@Service
public class ConversionServiceImp implements ConversionService {

    private final CacheService cacheService;
    private final FxRatesApiServiceImp fxRatesApiService;

    @Autowired
    public ConversionServiceImp(FxRatesApiServiceImp fxRatesApiService, CacheService cacheService) {
        this.fxRatesApiService = fxRatesApiService;
        this.cacheService = cacheService;
    }

    /**
     * calculates the conversion rate
     *
     * @param currencyCode
     * @return
     * @throws JSONException
     */
    @Override
    public double calculate(CurrencyCode currencyCode) throws JSONException {
        if (currencyCode == null) {
            throw new JSONException(CURRENCYCODE_IS_NULL);
        }

        String result =
                cacheService.getValueFromRedis(MessageFormat.format("{0}_INR", currencyCode));
        if (result != null) {
            return Double.parseDouble(result);
        }
        String response = (String) fxRatesApiService.fetchData();
        JSONObject jsonResponse = new JSONObject(response);

        if (jsonResponse.getBoolean(SUCCESS)) {
            JSONObject rates = jsonResponse.getJSONObject(RATES);

            double baseToINR = rates.getDouble(CurrencyCode.INR.toString());
            double baseToCurrency = rates.optDouble(currencyCode.name(), -1);
            if (baseToCurrency == -1) {
                throw new IllegalArgumentException(INVALID_CURRENCYCODE + currencyCode.name());
            }
            double value = baseToCurrency / baseToINR;
            cacheService.setValueToRedis(
                    currencyCode + "_INR", String.valueOf(value), DateUtil.getEndOfMinute());
            return baseToCurrency / baseToINR;
        } else {
            throw new RuntimeException(API_CALL_UNSUCCESSFUL);
        }
    }
}
