package com.jarapplication.kiranastore.feature_transactions.repository;

import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    List<TransactionEntity> findTransactionEntityByBillId(String billId);

    /**
     * Retrive transactions from database between startDate and EndDate
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.date BETWEEN :startDate AND :endDate")
    List<TransactionEntity> findTransactionsByDateRange(
            @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
