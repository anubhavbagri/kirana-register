package com.jarapplication.kiranastore.feature_transactions.model;

import static com.jarapplication.kiranastore.feature_transactions.constants.LogConstants.BILLID_IS_NULL_OR_EMPTY;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RefundRequest {
    @NotEmpty(message = BILLID_IS_NULL_OR_EMPTY)
    private String billId;
}
