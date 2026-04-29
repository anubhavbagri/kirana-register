package com.jarapplication.kiranastore.feature_reports.service;

import com.jarapplication.kiranastore.feature_reports.dao.ReportDao;
import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import java.util.Calendar;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportDao reportDao;

    @Autowired
    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    /**
     * Retrieve Transactions for previous week
     *
     * @return
     */
    public List<TransactionEntity> getWeeklyReport() {
        Calendar calendar = Calendar.getInstance();
        int lastWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR) - 1;
        int lastMonth = calendar.get(Calendar.MONTH);
        int lastYear = calendar.get(Calendar.YEAR);
        return reportDao.getTransactionsForWeek(lastWeekNumber, lastMonth, lastYear);
    }

    /**
     * Retrieve Transactions for previous month
     *
     * @return
     */
    public List<TransactionEntity> getMonthlyReport() {
        Calendar calendar = Calendar.getInstance();
        int lastMonth = calendar.get(Calendar.MONTH) - 1;
        int lastYear = calendar.get(Calendar.YEAR);
        return reportDao.getTransactionsForMonth(lastMonth, lastYear);
    }

    /**
     * Retrieve Transactions for previous year
     *
     * @return
     */
    public List<TransactionEntity> getYearlyReport() {
        Calendar calendar = Calendar.getInstance();
        int lastYear = calendar.get(Calendar.YEAR) - 1;
        return reportDao.getTransactionsForYear(lastYear);
    }
}
