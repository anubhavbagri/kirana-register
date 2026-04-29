package com.jarapplication.kiranastore.feature_transactions.util;

import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import com.jarapplication.kiranastore.feature_transactions.enums.TransactionType;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseResponse;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;
import java.util.Date;

public class TransactionDtoUtil {
    /**
     * DTO for transaction Entity
     *
     * @param transactionDto
     * @return
     */
    public static TransactionEntity TransactionEntityDTO(TransactionDto transactionDto) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setTransactionType(TransactionType.PURCHASE);
        transactionEntity.setBillId(transactionDto.getBillId());
        transactionEntity.setUserId(transactionDto.getUserId());
        transactionEntity.setAmount(transactionDto.getAmountInINR());
        return transactionEntity;
    }

    public static TransactionEntity toTransactionEntity(
            String billId, String userId, double refundAmount) {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setBillId(billId);
        transactionEntity.setUserId(userId);
        transactionEntity.setTransactionType(TransactionType.REFUND);
        transactionEntity.setAmount(refundAmount);
        transactionEntity.setDate(new Date());
        return transactionEntity;
    }

    public static PurchaseResponse transactionResponseDto(TransactionDto transactionDto) {
        PurchaseResponse purchaseResponseDto = new PurchaseResponse();
        purchaseResponseDto.setBillId(transactionDto.getBillId());
        purchaseResponseDto.setAmount(transactionDto.getAmount());
        purchaseResponseDto.setBillItems(transactionDto.getBillItems());
        purchaseResponseDto.setTransactionType(transactionDto.getTransactionType());
        return purchaseResponseDto;
    }
}
