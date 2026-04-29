package com.jarapplication.kiranastore.feature_transactions.service;

import com.jarapplication.kiranastore.feature_transactions.dao.BillDao;
import com.jarapplication.kiranastore.feature_transactions.entity.BillEntity;
import com.jarapplication.kiranastore.feature_transactions.helper.CalculateBill;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.TransactionDto;
import com.jarapplication.kiranastore.feature_transactions.util.BillDtoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceImp implements BillingService {

    private final CalculateBill calculateBillhelper;
    private final ConversionServiceImp conversionService;
    private final BillDao billDao;

    @Autowired
    public BillingServiceImp(
            CalculateBill calculateBill, ConversionServiceImp conversionService, BillDao billDao) {
        this.calculateBillhelper = calculateBill;
        this.conversionService = conversionService;
        this.billDao = billDao;
    }

    /**
     * Generates Bills with standard currency and user requested currency
     *
     * @param purchaseRequest
     * @return
     */
    @Override
    public TransactionDto generateBills(PurchaseRequest purchaseRequest) {
        if (purchaseRequest == null) {
            throw new IllegalArgumentException("Purchase request cannot be null");
        }

        double totalAmount = calculateBillhelper.calculateBill(purchaseRequest);
        double conversionRate = conversionService.calculate(purchaseRequest.getCurrencyCode());
        double billAmount = totalAmount * conversionRate;
        BillEntity billEntity =
                billDao.save(BillDtoUtil.billEntityDTO(purchaseRequest, billAmount));
        return BillDtoUtil.toTransactionDto(billEntity, totalAmount);
    }
}
