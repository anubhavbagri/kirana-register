package com.jarapplication.kiranastore.feature_transactions.dao;

import com.jarapplication.kiranastore.feature_transactions.entity.BillEntity;
import com.jarapplication.kiranastore.feature_transactions.repository.BillRepository;
import org.springframework.stereotype.Component;

@Component
public class BillDao {

    private final BillRepository billRepository;

    public BillDao(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /**
     * Saves the bill
     *
     * @param billEntity
     * @return
     */
    public BillEntity save(BillEntity billEntity) {
        return billRepository.save(billEntity);
    }
}
