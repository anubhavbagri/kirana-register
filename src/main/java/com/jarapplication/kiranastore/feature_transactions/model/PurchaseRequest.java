package com.jarapplication.kiranastore.feature_transactions.model;

import static com.jarapplication.kiranastore.feature_transactions.constants.LogConstants.USERID_IS_NULL;

import com.jarapplication.kiranastore.feature_transactions.enums.CurrencyCode;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseRequest {
    @NotEmpty(message = USERID_IS_NULL)
    private String userId;

    @Builder.Default private CurrencyCode currencyCode = CurrencyCode.INR;
    private List<BillItem> billItems;
}
