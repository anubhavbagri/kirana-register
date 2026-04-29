package com.jarapplication.kiranastore.feature_transactions.entity;

import com.jarapplication.kiranastore.feature_transactions.enums.TransactionType;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

@Data
@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private String transactionId;

    @Column(name = "amount")
    private double amount;

    @Column(name = "bill_id")
    private String billId;

    @Column(name = "user_id")
    private String userId;

    @Enumerated(EnumType.STRING) // Store enum as String
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @CreatedDate
    @Column(name = "date", nullable = false)
    private Date date = new Date();
}
