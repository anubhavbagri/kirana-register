package com.jarapplication.kiranastore.feature_reports.dao;

import static com.jarapplication.kiranastore.feature_reports.util.DateUtil.*;

import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import com.jarapplication.kiranastore.feature_transactions.repository.TransactionRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REPORT DAO: Data Access Layer for Report Generation Queries
 *
 * WHAT IT DOES:
 * ├─ Queries PostgreSQL for transactions within specific date ranges
 * ├─ Supports weekly, monthly, and yearly report generation
 * ├─ Uses TransactionRepository's custom JPQL query (findTransactionsByDateRange)
 * └─ Logs report generation requests for monitoring
 *
 * WHY IT'S NEEDED:
 * ├─ Date range construction: Converts (week, month, year) → (startDate, endDate)
 * │   └─ Uses feature_reports/util/DateUtil for date boundary calculations
 * ├─ Separation: ReportService handles business logic, ReportDao handles data access
 * ├─ Reusability: Same date-range query pattern for weekly/monthly/yearly
 * └─ Logging: Tracks what reports are being generated (audit trail)
 *
 * ARCHITECTURE:
 * ├─ ReportKafkaListener (Kafka consumer, triggers reports)
 * │   └─ calls ReportService (business logic)
 * │       └─ calls ReportDao (THIS CLASS, data access)
 * │           └─ calls TransactionRepository.findTransactionsByDateRange() (JPQL query)
 * │               └─ PostgreSQL: SELECT * FROM transactions WHERE date BETWEEN ? AND ?
 *
 * CROSS-MODULE DEPENDENCY:
 * ├─ This class lives in feature_reports package
 * ├─ But queries TransactionRepository from feature_transactions package
 * └─ This is intentional: Reports are READ views over transaction data
 *    └─ No circular dependency: reports → transactions (one-way)
 *
 * @Slf4j (Lombok): Auto-generates a `log` field for logging
 *   └─ Equivalent to: private static final Logger log = LoggerFactory.getLogger(ReportDao.class);
 *
 * @Component: Registers as Spring bean (data access layer)
 */
@Slf4j      // ← Lombok: Auto-generates logger field
@Component  // ← Spring bean (DAO layer)
public class ReportDao {
    private final TransactionRepository transactionRepository;

    @Autowired
    public ReportDao(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieves all transactions for a specific week of a month.
     *
     * DATE RANGE:
     * ├─ Start: Sunday of the specified week at 00:00:00.000
     * ├─ End: Saturday of the specified week at 23:59:59.999
     * └─ getStartOfWeek/getEndOfWeek from DateUtil handle boundary calculation
     *
     * @param weekNumber ← Week number within the month (1-based: 1st week, 2nd week, etc.)
     * @param month      ← Month (1-12, but DateUtil adjusts for 0-based Calendar.MONTH)
     * @param year       ← Year (e.g., 2024)
     * @return List of transactions within the specified week
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
     * Retrieves all transactions for a specific month.
     *
     * DATE RANGE:
     * ├─ Start: 1st day of month at 00:00:00.000
     * └─ End: Last day of month at 23:59:59.999
     *
     * @param month ← Month (0-11 for Calendar, see DateUtil)
     * @param year  ← Year (e.g., 2024)
     * @return List of transactions within the specified month
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
     * Retrieves all transactions for an entire year.
     *
     * DATE RANGE:
     * ├─ Start: January 1st at 00:00:00.000
     * └─ End: December 31st at 23:59:59.999
     *
     * @param year ← Year (e.g., 2024)
     * @return List of transactions within the specified year
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
