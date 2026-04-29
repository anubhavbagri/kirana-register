package com.jarapplication.kiranastore.feature_transactions.model;

import com.jarapplication.kiranastore.feature_transactions.enums.CurrencyCode;
import com.jarapplication.kiranastore.feature_transactions.enums.TransactionType;
import java.util.List;
import lombok.Data;

@Data
public class TransactionDto {
    private String userId;
    private String billId;
    private CurrencyCode currencyCode;
    private double amount;
    private TransactionType transactionType;
    private double amountInINR;
    private List<BillItem> billItems;
}
