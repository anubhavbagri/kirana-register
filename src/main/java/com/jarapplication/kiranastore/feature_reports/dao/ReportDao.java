package com.jarapplication.kiranastore.feature_reports.dao;

import static com.jarapplication.kiranastore.feature_reports.util.DateUtil.*;

import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import com.jarapplication.kiranastore.feature_transactions.repository.TransactionRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportDao {
    private final TransactionRepository transactionRepository;

    @Autowired
    public ReportDao(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieve Transactions for a week
     *
     * @param weekNumber
     * @param month
     * @param year
     * @return
     */
    public List<TransactionEntity> getTransactionsForWeek(int weekNumber, int month, int year) {
        log.info(
                "Generating report for transactions from "
                        + getStartOfWeek(weekNumber, month, year)
                        + " to "
                        + getEndOfWeek(weekNumber, month, year));
        return transactionRepository.findTransactionsByDateRange(
                getStartOfWeek(weekNumber, month, year), getEndOfWeek(weekNumber, month, year));
    }

    /**
     * Retrieve Transactions for a month
     *
     * @param month
     * @param year
     * @return
     */
    public List<TransactionEntity> getTransactionsForMonth(int month, int year) {
        log.info(
                "Generating report for transactions from "
                        + getStartOfMonth(month, year)
                        + " to "
                        + getEndOfMonth(month, year));
        return transactionRepository.findTransactionsByDateRange(
                getStartOfMonth(month, year), getEndOfMonth(month, year));
    }

    /**
     * Retrieve transactions for a year
     *
     * @param year
     * @return
     */
    public List<TransactionEntity> getTransactionsForYear(int year) {
        log.info(
                "Generating report for transactions from "
                        + getStartOfYear(year)
                        + " to "
                        + getEndOfYear(year));
        return transactionRepository.findTransactionsByDateRange(
                getStartOfYear(year), getEndOfYear(year));
    }
}
