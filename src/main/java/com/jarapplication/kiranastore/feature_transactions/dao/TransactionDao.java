package com.jarapplication.kiranastore.feature_transactions.dao;

import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import com.jarapplication.kiranastore.feature_transactions.repository.TransactionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TRANSACTION DAO: Data Access Object for Transaction Records (PostgreSQL)
 *
 * WHAT IT DOES:
 * ├─ Wraps TransactionRepository (JPA/PostgreSQL) behind a simpler interface
 * ├─ Provides save() and findByBillId() operations
 * └─ Bridges TransactionServiceImpl ↔ TransactionRepository
 *
 * WHY IT'S NEEDED:
 * ├─ Same DAO pattern as BillDao (see BillDao.java for full explanation)
 * ├─ Key difference: This DAO talks to PostgreSQL (JPA), not MongoDB
 * │   └─ TransactionRepository extends JpaRepository (relational DB)
 * │   └─ BillRepository extends MongoRepository (document DB)
 * │
 * ├─ Future extensibility:
 * │   ├─ Add custom queries beyond Spring Data auto-generation
 * │   ├─ Add retry logic for transient DB failures
 * │   ├─ Add audit logging for transaction persistence
 * │   └─ Add pagination support for transaction queries
 *
 * ARCHITECTURE POSITION:
 * ├─ TransactionServiceImpl (business logic)
 * │   ├─ calls TransactionDao.save() → persists purchase/refund to PostgreSQL
 * │   └─ calls TransactionDao.findByBillId() → retrieves transactions for refund validation
 * │       └─ TransactionRepository.findTransactionEntityByBillId() → JPA derived query
 * │           └─ SELECT * FROM transactions WHERE bill_id = ?
 *
 * DUAL-DATABASE ARCHITECTURE:
 * ├─ PostgreSQL (via JPA):
 * │   └─ TransactionEntity → "transactions" table
 * │      └─ Relational: transactionId, amount, billId, userId, transactionType, date
 * │      └─ WHY: Financial records need ACID guarantees (atomicity, consistency)
 * │
 * └─ MongoDB (via MongoRepository):
 *    └─ BillEntity → "bills" collection
 *       └─ Document: contains nested list of BillItems (flexible schema)
 *       └─ WHY: Bills have variable-length item lists → better as documents
 */
@Component
public class TransactionDao {

    TransactionRepository transactionRepository;

    @Autowired // ← Explicit @Autowired (Spring injects TransactionRepository JPA bean)
    public TransactionDao(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Persists a TransactionEntity to PostgreSQL "transactions" table.
     *
     * WHAT HAPPENS:
     * ├─ JpaRepository.save() detects if entity is new (transactionId == null)
     * │   ├─ New: INSERT INTO transactions (...) → UUID auto-generated (@GeneratedValue)
     * │   └─ Existing: UPDATE transactions SET ... WHERE transaction_id = ?
     * ├─ If within @Transactional: save is part of the transaction (commit or rollback together)
     * └─ Returns entity with auto-generated transactionId
     *
     * CALLED BY:
     * ├─ TransactionServiceImpl.makeRefund() → saves REFUND transaction
     * └─ TransactionServiceImpl.makePurchase() → saves PURCHASE transaction
     *
     * @param transactionEntity ← Transaction record (amount, billId, userId, type, date)
     * @return TransactionEntity with auto-generated UUID transactionId
     */
    public TransactionEntity save(TransactionEntity transactionEntity) {
        return transactionRepository.save(transactionEntity);
    }

    /**
     * Retrieves all transactions linked to a specific billId.
     *
     * USE CASE (Refund validation):
     * ├─ User requests refund for billId "abc-123"
     * ├─ This method finds ALL transactions for that bill
     * ├─ Service then checks:
     * │   ├─ Do transactions exist? (if not → "transaction not found")
     * │   ├─ Does userId match? (if not → "refund failed" → prevents unauthorized refunds)
     * │   └─ Is there already a REFUND type? (if yes → "already refunded" → prevents double refunds)
     * └─ Returns empty list if no transactions exist for the billId
     *
     * SQL GENERATED (Spring Data JPA derived query):
     * └─ SELECT * FROM transactions WHERE bill_id = :billId
     *
     * @param billId ← The MongoDB bill ID linking bill → transactions
     * @return List of TransactionEntity (could be 1 PURCHASE + 0-1 REFUND)
     */
    public List<TransactionEntity> findByBillId(String billId) {
        return transactionRepository.findTransactionEntityByBillId(billId);
    }
}
