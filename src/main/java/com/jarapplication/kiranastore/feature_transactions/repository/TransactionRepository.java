package com.jarapplication.kiranastore.feature_transactions.repository;

import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * TRANSACTION REPOSITORY: Spring Data JPA Interface for PostgreSQL Transaction Records
 *
 * WHAT IT DOES:
 * ├─ Provides CRUD + custom query operations for TransactionEntity in PostgreSQL
 * ├─ Spring Data JPA auto-generates implementation at runtime
 * ├─ Supports both derived queries (method naming convention) and custom JPQL
 * └─ Used by TransactionDao and ReportDao for data access
 *
 * WHY IT'S NEEDED:
 * ├─ Zero SQL boilerplate for common operations
 * ├─ Type-safe queries: Compile-time checks on entity fields
 * ├─ Transaction support: Works seamlessly with @Transactional
 * └─ Custom JPQL: For complex queries Spring Data can't auto-generate
 *
 * INHERITED METHODS (from JpaRepository → CrudRepository):
 * ├─ save(entity)       → INSERT or UPDATE row in "transactions" table
 * ├─ findById(id)       → SELECT * FROM transactions WHERE transaction_id = ?
 * ├─ findAll()          → SELECT * FROM transactions
 * ├─ deleteById(id)     → DELETE FROM transactions WHERE transaction_id = ?
 * ├─ count()            → SELECT COUNT(*) FROM transactions
 * └─ existsById(id)     → SELECT EXISTS(... WHERE transaction_id = ?)
 *
 * QUERY METHODS EXPLAINED:
 * ├─ findTransactionEntityByBillId (derived query):
 * │   ├─ Spring Data parses method name → generates SQL automatically
 * │   ├─ "findTransactionEntityBy" → SELECT * FROM transactions WHERE
 * │   ├─ "BillId" → bill_id = :billId
 * │   ├─ Generated SQL: SELECT * FROM transactions WHERE bill_id = ?
 * │   └─ Used by: TransactionDao.findByBillId() → refund validation
 * │
 * └─ findTransactionsByDateRange (custom JPQL):
 *    ├─ @Query: Custom JPQL (Java Persistence Query Language, not raw SQL)
 *    ├─ JPQL uses entity field names (t.date), not column names (date)
 *    ├─ :startDate, :endDate → bound via @Param annotations
 *    ├─ Generated SQL: SELECT * FROM transactions WHERE date BETWEEN ? AND ?
 *    └─ Used by: ReportDao → weekly/monthly/yearly report generation
 *
 * WHY JPQL (not native SQL)?
 * ├─ Database-agnostic: Works if you switch PostgreSQL → MySQL
 * ├─ Uses entity field names: t.date (not column name)
 * ├─ Type-safe: Hibernate validates JPQL at startup
 * └─ Native SQL alternative: @Query(value="...", nativeQuery=true)
 *
 * @Repository: Marks as data access layer + enables JPA exception translation
 */
@Repository // ← Spring bean + JPA exception translation (SQL exceptions → DataAccessException)
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
    // JpaRepository<TransactionEntity, String>:
    //   ├─ TransactionEntity: The JPA entity class
    //   └─ String: The type of @Id field (transactionId is UUID String)

    /**
     * DERIVED QUERY: Finds all transactions linked to a specific bill.
     *
     * Spring Data derives SQL from method name:
     * ├─ "find" → SELECT
     * ├─ "TransactionEntityBy" → FROM transactions WHERE
     * ├─ "BillId" → bill_id = :billId
     * └─ Returns List: Multiple transactions can exist per bill (PURCHASE + REFUND)
     *
     * USED FOR: Refund validation in TransactionServiceImpl.makeRefund()
     * ├─ Find all transactions for a bill
     * ├─ Check ownership (userId matches)
     * └─ Check not already refunded (no REFUND type exists)
     *
     * @param billId ← MongoDB bill ID to search for
     * @return List of matching TransactionEntity records (usually 1-2 records)
     */
    List<TransactionEntity> findTransactionEntityByBillId(String billId);

    /**
     * CUSTOM JPQL QUERY: Retrieves transactions within a date range.
     *
     * JPQL: "SELECT t FROM TransactionEntity t WHERE t.date BETWEEN :startDate AND :endDate"
     * ├─ t.date → maps to "date" column in PostgreSQL
     * ├─ BETWEEN → inclusive range (startDate <= date <= endDate)
     * └─ Results ordered by: default (insertion order)
     *
     * USED FOR: Report generation in ReportDao
     * ├─ getTransactionsForWeek() → weekly report
     * ├─ getTransactionsForMonth() → monthly report
     * └─ getTransactionsForYear() → yearly report
     *
     * WHY @Param ANNOTATION?
     * ├─ Binds method parameter to JPQL named parameter (:startDate, :endDate)
     * ├─ Without @Param: Spring might not match parameter names (depends on compiler flags)
     * └─ Best practice: Always use @Param for explicit binding
     *
     * @param startDate ← Start of date range (inclusive)
     * @param endDate   ← End of date range (inclusive)
     * @return List of transactions within the date range
     */
    @Query("SELECT t FROM TransactionEntity t WHERE t.date BETWEEN :startDate AND :endDate")
    List<TransactionEntity> findTransactionsByDateRange(
            @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
