package com.jarapplication.kiranastore.feature_transactions.service;

import static com.jarapplication.kiranastore.feature_transactions.constants.Constants.FXRATES_URL;
import static com.jarapplication.kiranastore.feature_transactions.constants.LogConstants.FXRATES_INTERNAL_ERROR;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * FX RATES API SERVICE IMPLEMENTATION: HTTP Client for FxRates External API
 *
 * WHAT IT DOES:
 * ├─ Makes HTTP GET request to FxRates API (https://api.fxratesapi.com/latest)
 * ├─ Returns raw JSON response containing live exchange rates
 * └─ Wraps RestTemplate for external HTTP communication
 *
 * WHY IT'S NEEDED:
 * ├─ External integration: Kirana Store needs live exchange rates for multi-currency billing
 * ├─ Separation: HTTP details isolated from business logic (ConversionServiceImp)
 * ├─ Error handling: Catches ALL HTTP exceptions → wraps in RuntimeException
 * └─ Single responsibility: Only handles the HTTP call, nothing else
 *
 * REST TEMPLATE:
 * ├─ Spring's synchronous HTTP client (alternatives: WebClient for async)
 * ├─ restTemplate.getForObject(url, type): Makes GET request → deserializes response
 * │   ├─ First arg: URL to call
 * │   ├─ Second arg: Response type (String.class = raw JSON string)
 * │   └─ Returns: Deserialized response object
 * ├─ Handles: Connection, serialization, HTTP status codes
 * └─ Throws: RestClientException for HTTP errors (4xx, 5xx, timeouts)
 *
 * API RESPONSE FORMAT:
 * {
 *   "success": true,
 *   "base": "EUR",
 *   "rates": {
 *     "INR": 89.5,
 *     "USD": 1.07,
 *     "GBP": 0.86,
 *     ...
 *   }
 * }
 *
 * ERROR HANDLING:
 * ├─ Catches ALL exceptions (network, timeout, DNS, HTTP errors)
 * ├─ Wraps in RuntimeException with descriptive message
 * └─ RuntimeException propagates to ExceptionController → client gets error response
 *
 * IMPROVEMENT OPPORTUNITIES:
 * ├─ Inject RestTemplate as @Bean (enable connection pooling, timeouts)
 * ├─ Add retry logic (resilience4j or Spring Retry)
 * ├─ Add circuit breaker (prevent cascading failures if API is down)
 * └─ Use WebClient for non-blocking async calls (better for high traffic)
 */
@Service // ← Spring bean for external API integration
public class FxRatesApiServiceImp implements FxRatesApiService {

    // RestTemplate created inline (consider @Bean injection for production)
    // @Bean injection would allow: connection pooling, timeouts, interceptors
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches latest exchange rates from FxRates API.
     *
     * FLOW:
     * ├─ HTTP GET → https://api.fxratesapi.com/latest
     * ├─ Receives: JSON string with exchange rates
     * ├─ Returns: Raw JSON string (parsed by ConversionServiceImp)
     * └─ On error: Throws RuntimeException (caught by ExceptionController)
     *
     * @return Raw JSON response string from FxRates API
     * @throws RuntimeException if HTTP call fails for any reason
     */
    @Override
    public Object fetchData() {
        try {
            // HTTP GET call → returns raw JSON response as String
            return restTemplate.getForObject(FXRATES_URL, String.class);
        } catch (Exception e) {
            // Catches: HttpClientErrorException, HttpServerErrorException,
            //          ResourceAccessException (timeout, DNS), etc.
            // Wraps in RuntimeException → propagates to ExceptionController
            throw new RuntimeException(FXRATES_INTERNAL_ERROR);
        }
    }
}
