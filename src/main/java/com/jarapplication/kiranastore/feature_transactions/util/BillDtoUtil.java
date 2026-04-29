package com.jarapplication.kiranastore.feature_transactions.util;

import com.jarapplication.kiranastore.feature_transactions.entity.BillEntity;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;
import java.util.Date;

public class BillDtoUtil {

    /**
     * Transforms into BillEntity
     *
     * @param purchaseRequest
     * @param billAmount
     * @return
     */
    public static BillEntity billEntityDTO(PurchaseRequest purchaseRequest, double billAmount) {
        BillEntity billEntity = new BillEntity();
        billEntity.setBillDate(new Date());
        billEntity.setUserId(purchaseRequest.getUserId());
        billEntity.setBillItems(purchaseRequest.getBillItems());
        billEntity.setCurrencyCode(purchaseRequest.getCurrencyCode());
        billEntity.setTotalAmount(billAmount);
        return billEntity;
    }

    /**
     * Transforms into transactionDto
     *
     * @param billEntity
     * @param amountInINR
     * @return
     */
    public static TransactionDto toTransactionDto(BillEntity billEntity, double amountInINR) {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setUserId(billEntity.getUserId());
        transactionDto.setBillId(billEntity.getBillId());
        transactionDto.setCurrencyCode(billEntity.getCurrencyCode());
        transactionDto.setAmount(billEntity.getTotalAmount());
        transactionDto.setBillItems(billEntity.getBillItems());
        transactionDto.setAmountInINR(amountInINR);
        return transactionDto;
    }
}
