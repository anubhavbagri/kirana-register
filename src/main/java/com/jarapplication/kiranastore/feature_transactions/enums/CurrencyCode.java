package com.jarapplication.kiranastore.feature_transactions.enums;

/**
 * CURRENCY CODE ENUM: ISO 4217 + Cryptocurrency Codes for Multi-Currency Support
 *
 * WHAT IT DOES:
 * ├─ Defines all supported currency codes for the Kirana Store
 * ├─ Used in PurchaseRequest to specify which currency the customer wants
 * ├─ Validated against FxRates API response (only currencies present in API rates are valid)
 * └─ Type-safe: Prevents invalid currency strings at compile time
 *
 * WHY AN ENUM (not String)?
 * ├─ Type safety: "USDD" would be a compile error → catches typos early
 * ├─ IDE support: Auto-complete shows all valid currencies
 * ├─ Validation: Jackson (JSON parser) auto-rejects invalid currency strings in request body
 * │   └─ Client sends: { "currencyCode": "INVALID" } → HttpMessageNotReadableException
 * │       └─ Caught by ExceptionController → returns "Invalid Request Body"
 * ├─ Documentation: Enum itself documents all supported currencies
 * └─ Comparison: == works for enums (instead of .equals() for Strings)
 *
 * USE CASE IN APPLICATION:
 * ├─ 1. Client sends: POST /v1/api/purchase { "currencyCode": "USD", "billItems": [...] }
 * ├─ 2. Jackson deserializes "USD" → CurrencyCode.USD enum value
 * ├─ 3. ConversionServiceImp.calculate(CurrencyCode.USD) is called
 * ├─ 4. FxRates API response has: { "rates": { "INR": 83.5, "USD": 1.0, ... } }
 * ├─ 5. Conversion: baseToCurrency / baseToINR = exchange rate
 * └─ 6. Bill amount = totalAmount * conversionRate
 *
 * INCLUDES:
 * ├─ Fiat currencies: INR, USD, EUR, GBP, JPY, etc. (ISO 4217 standard)
 * └─ Cryptocurrencies: BTC, ETH, BNB, SOL, XRP, etc. (as supported by FxRates API)
 *
 * NOTE: The FxRates API may not support ALL currencies listed here.
 *       ConversionServiceImp handles this with:
 *       double baseToCurrency = rates.optDouble(currencyCode.name(), -1);
 *       if (baseToCurrency == -1) throw new IllegalArgumentException("invalid Currency code");
 */
public enum CurrencyCode {
    ADA,  // Cardano (crypto)
    AED,  // United Arab Emirates Dirham
    AFN,  // Afghan Afghani
    ALL,  // Albanian Lek
    AMD,  // Armenian Dram
    ANG,  // Netherlands Antillean Guilder
    AOA,  // Angolan Kwanza
    ARB,  // Arbitrum (crypto)
    ARS,  // Argentine Peso
    AUD,  // Australian Dollar
    AWG,  // Aruban Florin
    AZN,  // Azerbaijani Manat
    BAM,  // Bosnia-Herzegovina Convertible Mark
    BBD,  // Barbadian Dollar
    BDT,  // Bangladeshi Taka
    BGN,  // Bulgarian Lev
    BHD,  // Bahraini Dinar
    BIF,  // Burundian Franc
    BMD,  // Bermudian Dollar
    BNB,  // Binance Coin (crypto)
    BND,  // Brunei Dollar
    BOB,  // Bolivian Boliviano
    BRL,  // Brazilian Real
    BSD,  // Bahamian Dollar
    BTC,  // Bitcoin (crypto)
    BTN,  // Bhutanese Ngultrum
    BWP,  // Botswana Pula
    BYN,  // Belarusian Ruble (new)
    BYR,  // Belarusian Ruble (old)
    BZD,  // Belize Dollar
    CAD,  // Canadian Dollar
    CDF,  // Congolese Franc
    CHF,  // Swiss Franc
    CLF,  // Chilean Unit of Account
    CLP,  // Chilean Peso
    CNY,  // Chinese Yuan
    COP,  // Colombian Peso
    CRC,  // Costa Rican Colón
    CUC,  // Cuban Convertible Peso
    CUP,  // Cuban Peso
    CVE,  // Cape Verdean Escudo
    CZK,  // Czech Koruna
    DAI,  // Dai Stablecoin (crypto)
    DJF,  // Djiboutian Franc
    DKK,  // Danish Krone
    DOP,  // Dominican Peso
    DOT,  // Polkadot (crypto)
    DZD,  // Algerian Dinar
    EGP,  // Egyptian Pound
    ERN,  // Eritrean Nakfa
    ETB,  // Ethiopian Birr
    ETH,  // Ethereum (crypto)
    EUR,  // Euro
    FJD,  // Fijian Dollar
    FKP,  // Falkland Islands Pound
    GBP,  // British Pound Sterling
    GEL,  // Georgian Lari
    GGP,  // Guernsey Pound
    GHS,  // Ghanaian Cedi
    GIP,  // Gibraltar Pound
    GMD,  // Gambian Dalasi
    GNF,  // Guinean Franc
    GTQ,  // Guatemalan Quetzal
    GYD,  // Guyanese Dollar
    HKD,  // Hong Kong Dollar
    HNL,  // Honduran Lempira
    HRK,  // Croatian Kuna
    HTG,  // Haitian Gourde
    HUF,  // Hungarian Forint
    IDR,  // Indonesian Rupiah
    ILS,  // Israeli New Shekel
    IMP,  // Isle of Man Pound
    INR,  // Indian Rupee ← BASE CURRENCY for Kirana Store product prices
    IQD,  // Iraqi Dinar
    IRR,  // Iranian Rial
    ISK,  // Icelandic Króna
    JEP,  // Jersey Pound
    JMD,  // Jamaican Dollar
    JOD,  // Jordanian Dinar
    JPY,  // Japanese Yen
    KES,  // Kenyan Shilling
    KGS,  // Kyrgyzstani Som
    KHR,  // Cambodian Riel
    KMF,  // Comorian Franc
    KPW,  // North Korean Won
    KRW,  // South Korean Won
    KWD,  // Kuwaiti Dinar
    KYD,  // Cayman Islands Dollar
    KZT,  // Kazakhstani Tenge
    LAK,  // Lao Kip
    LBP,  // Lebanese Pound
    LKR,  // Sri Lankan Rupee
    LRD,  // Liberian Dollar
    LSL,  // Lesotho Loti
    LTC,  // Litecoin (crypto)
    LTL,  // Lithuanian Litas (legacy, now EUR)
    LVL,  // Latvian Lats (legacy, now EUR)
    LYD,  // Libyan Dinar
    MAD,  // Moroccan Dirham
    MDL,  // Moldovan Leu
    MGA,  // Malagasy Ariary
    MKD,  // Macedonian Denar
    MMK,  // Myanmar Kyat
    MNT,  // Mongolian Tugrik
    MOP,  // Macanese Pataca
    MRO,  // Mauritanian Ouguiya (old)
    MUR,  // Mauritian Rupee
    MVR,  // Maldivian Rufiyaa
    MWK,  // Malawian Kwacha
    MXN,  // Mexican Peso
    MYR,  // Malaysian Ringgit
    MZN,  // Mozambican Metical
    NAD,  // Namibian Dollar
    NGN,  // Nigerian Naira
    NIO,  // Nicaraguan Córdoba
    NOK,  // Norwegian Krone
    NPR,  // Nepalese Rupee
    NZD,  // New Zealand Dollar
    OMR,  // Omani Rial
    OP,   // Optimism (crypto)
    PAB,  // Panamanian Balboa
    PEN,  // Peruvian Sol
    PGK,  // Papua New Guinean Kina
    PHP,  // Philippine Peso
    PKR,  // Pakistani Rupee
    PLN,  // Polish Zloty
    PYG,  // Paraguayan Guarani
    QAR,  // Qatari Rial
    RON,  // Romanian Leu
    RSD,  // Serbian Dinar
    RUB,  // Russian Ruble
    RWF,  // Rwandan Franc
    SAR,  // Saudi Riyal
    SBD,  // Solomon Islands Dollar
    SCR,  // Seychellois Rupee
    SDG,  // Sudanese Pound
    SEK,  // Swedish Krona
    SGD,  // Singapore Dollar
    SHP,  // Saint Helena Pound
    SLL,  // Sierra Leonean Leone
    SOL,  // Solana (crypto)
    SOS,  // Somali Shilling
    SRD,  // Surinamese Dollar
    STD,  // São Tomé and Príncipe Dobra (old)
    SVC,  // Salvadoran Colón
    SYP,  // Syrian Pound
    SZL,  // Swazi Lilangeni
    THB,  // Thai Baht
    TJS,  // Tajikistani Somoni
    TMT,  // Turkmenistani Manat
    TND,  // Tunisian Dinar
    TOP,  // Tongan Paʻanga
    TRY,  // Turkish Lira
    TTD,  // Trinidad and Tobago Dollar
    TWD,  // New Taiwan Dollar
    TZS,  // Tanzanian Shilling
    UAH,  // Ukrainian Hryvnia
    UGX,  // Ugandan Shilling
    USD,  // United States Dollar
    UYU,  // Uruguayan Peso
    UZS,  // Uzbekistani Som
    VEF,  // Venezuelan Bolívar (old)
    VND,  // Vietnamese Dong
    VUV,  // Vanuatu Vatu
    WST,  // Samoan Tala
    XAF,  // Central African CFA Franc
    XAG,  // Silver (troy ounce)
    XAU,  // Gold (troy ounce)
    XCD,  // East Caribbean Dollar
    XDR,  // Special Drawing Rights (IMF)
    XOF,  // West African CFA Franc
    XPD,  // Palladium (troy ounce)
    XPF,  // CFP Franc
    XPT,  // Platinum (troy ounce)
    XRP,  // Ripple (crypto)
    YER,  // Yemeni Rial
    ZAR,  // South African Rand
    ZMK,  // Zambian Kwacha (old)
    ZMW,  // Zambian Kwacha (new)
    ZWL;  // Zimbabwean Dollar
}
