package com.jarapplication.kiranastore.feature_transactions.constants;

public class LogConstants {
    public static final String USERID_IS_NULL = "userid is null";
    public static final String BILLID_IS_NULL_OR_EMPTY = "billid is null or empty";
    public static final String CURRENCYCODE_IS_NULL = "currency code is null";
    public static final String INVALID_CURRENCYCODE = "invalid Currency code";
    public static final String API_CALL_UNSUCCESSFUL = "API call unsuccessful";
    public static final String FXRATES_INTERNAL_ERROR =
            "Error fetching data from FxRatesApiServiceImp";
    public static final String BILL_ID_AND_USERID_IS_NULL = "billId and userId cannot be null";
    public static final String TRANSACTION_NOT_FOUND = "transaction not found";
    public static final String TRANSACTION_REFUND_FAILED = "Transaction refund failed";
    public static final String TRANSACTION_ALREADY_REFUNDED = "Transaction already refunded";
    public static final String PURCHASE_REQUEST_IS_NULL = "purchase request is null";
}
