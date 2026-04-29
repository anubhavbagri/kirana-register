package com.jarapplication.kiranastore.feature_transactions.entity;

import com.jarapplication.kiranastore.feature_transactions.enums.CurrencyCode;
import com.jarapplication.kiranastore.feature_transactions.model.BillItem;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "bills")
public class BillEntity {

    @Id private String billId;
    private String userId;
    private Double totalAmount;
    private CurrencyCode currencyCode;
    private Date billDate = new Date();
    private List<BillItem> billItems;
}
