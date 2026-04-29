package com.jarapplication.kiranastore.feature_transactions.dao;

import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import com.jarapplication.kiranastore.feature_transactions.repository.TransactionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionDao {

    TransactionRepository transactionRepository;

    @Autowired
    public TransactionDao(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Saves the transaction
     *
     * @param transactionEntity
     * @return
     */
    public TransactionEntity save(TransactionEntity transactionEntity) {
        return transactionRepository.save(transactionEntity);
    }

    /**
     * Retrieves all the Transactions based on bill id
     *
     * @param billId
     * @return
     */
    public List<TransactionEntity> findByBillId(String billId) {
        return transactionRepository.findTransactionEntityByBillId(billId);
    }
}
