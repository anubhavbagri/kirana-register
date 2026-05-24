package com.jarapplication.kiranastore.kakfa;

import static com.jarapplication.kiranastore.constants.KafkaConstants.*;

import com.jarapplication.kiranastore.feature_reports.service.ReportService;
import com.jarapplication.kiranastore.feature_transactions.entity.TransactionEntity;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * KAFKA LISTENER: Asynchronous Report Generation via Kafka Messaging
 *
 * WHAT IT DOES:
 * ├─ Listens to Kafka topic "test-topic" for report generation requests
 * ├─ Processes incoming messages based on report type:
 * │   ├─ "Weekly Report"  → getWeeklyReport()
 * │   ├─ "Monthly Report" → getMonthlyReport()
 * │   └─ "Yearly Report"  → getYearlyReport()
 * └─ Prints report data to console (production: would write to file/email/dashboard)
 *
 * WHY KAFKA (not REST API)?
 * ├─ Asynchronous: Report generation is a long-running task → don't block HTTP request
 * ├─ Decoupled: Producer sends message → consumer processes independently
 * │   └─ Producer doesn't wait for report to complete
 * ├─ Reliable: Kafka guarantees message delivery (at-least-once semantics)
 * ├─ Scalable: Multiple consumer instances can process reports in parallel
 * └─ Fault-tolerant: If consumer is down, messages are persisted in Kafka until consumed
 *
 * KAFKA ARCHITECTURE:
 * ├─ Producer (sends message):
 * │   └─ Could be a scheduled job, admin API, or another microservice
 * │   └─ Sends: "Weekly Report" string to "test-topic"
 * │
 * ├─ Kafka Broker:
 * │   └─ Persists message in "test-topic" topic
 * │   └─ Maintains consumer offset (which messages have been read)
 * │
 * └─ Consumer (THIS CLASS):
 *    └─ @KafkaListener(topics = "test-topic") → auto-subscribes to topic
 *    └─ processMessage() called for each new message
 *
 * @KafkaListener ANNOTATION:
 * ├─ topics: Which Kafka topic to subscribe to (KafkaConstants.KAFKA_TOPIC = "test-topic")
 * ├─ groupId: Consumer group ID → Kafka load-balances messages within the group
 * │   └─ "my-group" → all consumers in "my-group" share the workload
 * │   └─ If 2 consumers in same group → each gets ~half the messages
 * │   └─ If 2 consumers in different groups → both get ALL messages (fan-out)
 * ├─ Spring auto-configures: KafkaListenerContainerFactory, ConsumerFactory
 * └─ Configuration from: application.properties (spring.kafka.*)
 *
 * MESSAGE PROCESSING FLOW:
 * ├─ Kafka message received: "Weekly Report"
 * ├─ matches KafkaConstants.WEEKlY_REPORT → calls reportService.getWeeklyReport()
 * ├─ ReportService → ReportDao → TransactionRepository.findTransactionsByDateRange()
 * ├─ Results: List<TransactionEntity> for the previous week
 * └─ Printed to console (System.out.println)
 *    └─ Production improvement: Write to CSV, send email, push to analytics service
 *
 * ERROR HANDLING:
 * ├─ Unknown message → default case: prints "Unknown message: ..."
 * ├─ Kafka consumer errors: Spring Kafka auto-retries (configurable)
 * └─ Report generation errors: Exceptions propagate → Kafka may retry delivery
 *
 * @Service: Registers as Spring bean (Kafka listener beans must be Spring-managed)
 */
@Service // ← Spring bean (required for @KafkaListener to work)
public class ReportKafkaListener {

    private final ReportService reportService;

    @Autowired
    public ReportKafkaListener(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Processes incoming Kafka messages for report generation.
     *
     * MESSAGE → REPORT TYPE MAPPING:
     * ├─ "Weekly Report"  → Previous week's transactions
     * ├─ "Monthly Report" → Previous month's transactions
     * └─ "Yearly Report"  → Previous year's transactions
     *
     * @param message ← Kafka message payload (String)
     */
    @KafkaListener(
            topics = KAFKA_TOPIC, // ← "test-topic"
            groupId = "my-group"  // ← Consumer group for load balancing
    )
    public void processMessage(String message) {
        List<TransactionEntity> transactions;

        // Route message to appropriate report generator
        switch (message) {
            case WEEKlY_REPORT: // ← "Weekly Report"
                transactions = reportService.getWeeklyReport();
                System.out.println("Weekly Report: " + transactions);
                break;

            case MONTHLY_REPORT: // ← "Monthly Report"
                transactions = reportService.getMonthlyReport();
                System.out.println("Monthly Report: " + transactions);
                break;

            case YEARLY_REPORT: // ← "Yearly Report"
                transactions = reportService.getYearlyReport();
                System.out.println("Yearly Report: " + transactions);
                break;

            default:
                // Unknown message type → log and ignore
                System.out.println("Unknown message: " + message);
        }
    }
}
