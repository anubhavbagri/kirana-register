package com.jarapplication.kiranastore.feature_reports.service;

import com.jarapplication.kiranastore.feature_reports.dao.ReportDao;
import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import java.util.Calendar;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * REPORT SERVICE: Business Logic for Generating Transaction Reports
 *
 * WHAT IT DOES:
 * ├─ Generates weekly, monthly, and yearly transaction reports
 * ├─ Automatically calculates "previous" period (last week, last month, last year)
 * └─ Delegates date-range queries to ReportDao
 *
 * WHY IT'S NEEDED:
 * ├─ Business logic: Determines WHICH period to report on (previous week/month/year)
 * ├─ Abstraction: ReportKafkaListener doesn't need to calculate dates
 * ├─ Reusability: Could be called from REST API, scheduled jobs, or Kafka consumers
 * └─ Consistency: All report consumers get the same period calculation logic
 *
 * TRIGGER MECHANISM:
 * ├─ ReportKafkaListener receives message on "test-topic" Kafka topic
 * ├─ Message content determines report type:
 * │   ├─ "Weekly Report"  → getWeeklyReport()
 * │   ├─ "Monthly Report" → getMonthlyReport()
 * │   └─ "Yearly Report"  → getYearlyReport()
 * └─ Results currently printed to console (System.out.println in listener)
 *    └─ Future: Could write to file, send email, or push to analytics service
 *
 * CALENDAR USAGE:
 * ├─ Calendar.getInstance() → gets current date/time in default timezone
 * ├─ Calendar.WEEK_OF_YEAR → ISO week number (1-52)
 * ├─ Calendar.MONTH → 0-based (January=0, December=11)
 * ├─ Calendar.YEAR → 4-digit year
 * └─ "Previous" period: current value - 1
 *
 * NOTE: Uses java.util.Calendar (legacy API)
 *       Consider migrating to java.time (LocalDate, YearMonth) for cleaner code
 *
 * @Service: Registers as Spring bean with business logic semantic
 */
@Service // ← Spring bean with "business logic" semantic marker
public class ReportService {

    private final ReportDao reportDao;

    @Autowired
    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    /**
     * Generates a report for the PREVIOUS week's transactions.
     *
     * LOGIC:
     * ├─ Gets current week number → subtracts 1 → "last week"
     * ├─ Gets current month and year (for date range calculation)
     * └─ Delegates to ReportDao.getTransactionsForWeek()
     *
     * EDGE CASE: If current week is week 1, lastWeekNumber = 0
     *            → DateUtil handles this, but could produce unexpected results
     *            → Consider: handling year boundary (December → January)
     *
     * @return List of TransactionEntity from the previous week
     */
    public List<TransactionEntity> getWeeklyReport() {
        Calendar calendar = Calendar.getInstance();
        int lastWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR) - 1; // ← Previous week
        int lastMonth = calendar.get(Calendar.MONTH);                  // ← Current month (0-based)
        int lastYear = calendar.get(Calendar.YEAR);                    // ← Current year
        return reportDao.getTransactionsForWeek(lastWeekNumber, lastMonth, lastYear);
    }

    /**
     * Generates a report for the PREVIOUS month's transactions.
     *
     * LOGIC:
     * ├─ Gets current month → subtracts 1 → "last month"
     * └─ Gets current year (for date range calculation)
     *
     * EDGE CASE: If current month is January (0), lastMonth = -1
     *            → Should decrement year and set month to 11 (December)
     *            → Currently NOT handled → potential bug at year boundary
     *
     * @return List of TransactionEntity from the previous month
     */
    public List<TransactionEntity> getMonthlyReport() {
        Calendar calendar = Calendar.getInstance();
        int lastMonth = calendar.get(Calendar.MONTH) - 1; // ← Previous month (0-based)
        int lastYear = calendar.get(Calendar.YEAR);
        return reportDao.getTransactionsForMonth(lastMonth, lastYear);
    }

    /**
     * Generates a report for the PREVIOUS year's transactions.
     *
     * LOGIC:
     * ├─ Gets current year → subtracts 1 → "last year"
     * └─ Delegates to ReportDao.getTransactionsForYear()
     *
     * @return List of TransactionEntity from the previous year
     */
    public List<TransactionEntity> getYearlyReport() {
        Calendar calendar = Calendar.getInstance();
        int lastYear = calendar.get(Calendar.YEAR) - 1; // ← Previous year
        return reportDao.getTransactionsForYear(lastYear);
    }
}
