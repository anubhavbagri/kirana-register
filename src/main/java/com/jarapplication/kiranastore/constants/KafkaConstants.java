package com.jarapplication.kiranastore.constants;

/**
 * KAFKA CONSTANTS: Topic Names and Report Type Identifiers
 *
 * WHAT IT DOES:
 * ├─ Stores Kafka topic names and report type message strings
 * ├─ Used by producers (to publish messages) and consumers (to subscribe/match)
 * └─ Single source of truth for Kafka message contracts
 *
 * WHY IT'S NEEDED:
 * ├─ Consistency: Producer and consumer use the same topic name
 * │   └─ If topic name changes → change in ONE place → both sides update
 * ├─ Message contract: Report type strings define the message protocol
 * │   └─ Producer sends "Weekly Report" → Consumer matches WEEKlY_REPORT constant
 * │   └─ Prevents typo bugs (e.g., "weekly report" vs "Weekly Report")
 * └─ Discoverability: All Kafka-related constants in one place
 *
 * ARCHITECTURE:
 * ├─ KAFKA_TOPIC → Used in:
 * │   ├─ ReportKafkaListener @KafkaListener(topics = KAFKA_TOPIC)
 * │   └─ Any producer that sends to this topic
 * │
 * └─ Report type constants → Used in:
 *    ├─ ReportKafkaListener.processMessage() → switch/case matching
 *    └─ Any producer that triggers report generation
 *
 * NOTE: Topic name "test-topic" is clearly for development/testing.
 *       Production should use: "kirana-store.reports" or similar naming convention.
 *       └─ Convention: {domain}.{feature} or {app-name}.{event-type}
 *
 * TYPO ALERT: WEEKlY_REPORT has lowercase 'l' (should be 'L')
 *             → Not a bug (code works), but naming inconsistency
 */
public class KafkaConstants {
    // Kafka topic name for report generation messages
    // ALL report types are sent to this single topic (could be split per type)
    public static final String KAFKA_TOPIC = "test-topic";

    // Report type message payloads (sent by producers, matched by consumers)
    public static final String WEEKlY_REPORT = "Weekly Report";   // ← Note: lowercase 'l' in WEEKlY
    public static final String MONTHLY_REPORT = "Monthly Report";
    public static final String YEARLY_REPORT = "Yearly Report";
}
