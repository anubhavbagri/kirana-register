package com.jarapplication.kiranastore.kakfa;

import static com.jarapplication.kiranastore.constants.KafkaConstants.*;

import com.jarapplication.kiranastore.feature_reports.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReportKafkaListener {

    ReportService reportService;

    @Autowired
    public ReportKafkaListener(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Kafka Consumer which triggers to generate reports
     *
     * @param message
     */
    @KafkaListener(topics = KAFKA_TOPIC)
    public void processUserAttributionEvent(String message) {

        if (message.equals(WEEKlY_REPORT)) {
            System.out.println(reportService.getWeeklyReport());
        }
        if (message.equals(MONTHLY_REPORT)) {
            System.out.println(reportService.getMonthlyReport());
        }
        if (message.equals(YEARLY_REPORT)) {
            System.out.println(reportService.getYearlyReport());
        }
    }
}
