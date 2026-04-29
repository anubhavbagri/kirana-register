package com.jarapplication.kiranastore.feature_transactions.service;

import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseResponse;
import jakarta.transaction.Transactional;

public interface TransactionService {

    String makeRefund(String billId, String userId);

    @Transactional
    PurchaseResponse makePurchase(PurchaseRequest purchaseRequest);
}
