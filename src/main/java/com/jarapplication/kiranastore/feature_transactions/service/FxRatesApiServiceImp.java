package com.jarapplication.kiranastore.feature_transactions.service;

import static com.jarapplication.kiranastore.feature_transactions.constants.Constants.FXRATES_URL;
import static com.jarapplication.kiranastore.feature_transactions.constants.LogConstants.FXRATES_INTERNAL_ERROR;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FxRatesApiServiceImp implements FxRatesApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches the exchange rate
     *
     * @return
     */
    @Override
    public Object fetchData() {
        try {
            return restTemplate.getForObject(FXRATES_URL, String.class);
        } catch (Exception e) {
            throw new RuntimeException(FXRATES_INTERNAL_ERROR);
        }
    }
}
