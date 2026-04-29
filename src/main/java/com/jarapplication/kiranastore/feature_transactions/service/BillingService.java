package com.jarapplication.kiranastore.feature_transactions.service;

import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;

public interface BillingService {
    /**
     * Generates bills based on the list of products and quantities provided and calculates the
     * price in user requested currency
     *
     * @param purchaseRequest
     * @return
     */
    TransactionDto generateBills(PurchaseRequest purchaseRequest);
}
