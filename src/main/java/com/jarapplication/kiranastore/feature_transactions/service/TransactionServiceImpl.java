package com.jarapplication.kiranastore.feature_transactions.service;

import static com.jarapplication.kiranastore.feature_transactions.constants.Constants.REFUND_SUCCESSFUL;
import static com.jarapplication.kiranastore.feature_transactions.constants.LogConstants.*;
import static com.jarapplication.kiranastore.feature_transactions.util.TransactionDtoUtil.TransactionEntityDTO;
import static com.jarapplication.kiranastore.feature_transactions.util.TransactionDtoUtil.transactionResponseDto;

import com.jarapplication.kiranastore.feature_transactions.dao.TransactionDao;
import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import com.jarapplication.kiranastore.feature_transactions.enums.TransactionType;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseResponse;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;
import com.jarapplication.kiranastore.feature_transactions.util.TransactionDtoUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final BillingServiceImp billingService;
    private final TransactionDao transactionDao;

    @Autowired
    public TransactionServiceImpl(BillingServiceImp billingService, TransactionDao transactionDao) {
        this.billingService = billingService;
        this.transactionDao = transactionDao;
    }

    /**
     * checks for validity and makes refund
     *
     * @param billId
     * @param userId
     * @return
     */
    @Transactional
    @Override
    public String makeRefund(String billId, String userId) {
        if (billId == null || userId == null) {
            throw new IllegalArgumentException(BILL_ID_AND_USERID_IS_NULL);
        }
        List<TransactionEntity> transactions = transactionDao.findByBillId(billId);
        if (transactions.isEmpty()) {
            throw new RuntimeException(TRANSACTION_NOT_FOUND);
        }

        for (TransactionEntity transactionEntity : transactions) {
            if (!transactionEntity.getUserId().equals(userId)) {
                throw new RuntimeException(TRANSACTION_REFUND_FAILED);
            }
            if (transactionEntity.getTransactionType().equals(TransactionType.REFUND)) {
                throw new RuntimeException(TRANSACTION_ALREADY_REFUNDED);
            }
        }
        double amount = transactions.getFirst().getAmount();
        transactionDao.save(TransactionDtoUtil.toTransactionEntity(billId, userId, amount));
        return REFUND_SUCCESSFUL;
    }

    /**
     * Makes purchase
     *
     * @param purchaseRequest
     * @return
     */
    @Transactional
    @Override
    public PurchaseResponse makePurchase(PurchaseRequest purchaseRequest) {
        if (purchaseRequest == null) {
            throw new IllegalArgumentException(PURCHASE_REQUEST_IS_NULL);
        }
        TransactionDto transactionDto = billingService.generateBills(purchaseRequest);
        transactionDao.save(TransactionEntityDTO(transactionDto));
        return transactionResponseDto(transactionDto);
    }
}
