# Kirana Store - Transaction Register Backend

The objective is to develop a backend service aiding Kirana stores in managing their transaction registers. Responsibility of this service is to track daily credit and debit transactions, ensuring effective financial management.

## Functional Requirements

### Transactional API:
An API which will record transactions in different currencies (INR & USD). Use the reference API for currency conversion (currency conversion rates are volatile).
> Reference API: https://api.fxratesapi.com/latest

### Reporting API:
API for generating weekly, monthly, and yearly financial reports with insights like total credits, debits, and net flow. Implemented using Kafka and should be implemented using an async flow.

### User Authentication and Authorization:
Secure access with a clear distinction between authentication and authorization.

### API Rate Limiting:
Implement configurable rate limiting mechanism for both APIs to prevent abuse, exploring various algorithms and tools. For example, transaction record API can have 10 requests/minute limit.

### Caching:
Cache currency conversion API response to avoid hitting API limit.

### Implement DAO Layer:
Read and understand the importance of a DAO layer and how to use it for cache validation and invalidation.

### Metrics (Prometheus & Grafana):
Implement Prometheus and Grafana to monitor the application for performance and degradation. Demonstrate latency and throughput of the application on the dashboard.

### Logging:
Implement logging using ELK stack. Use logback XML with logstash to route the logs to your ELK destination.

## Add ons

### Handle Products / Items Purchased in Each Transaction
In each transaction, information about the products/items purchased needs to be recorded. The system must associate multiple products with a transaction. Store relevant details such as quantity, price, category, etc. in product details.

#### Steps
1. Design database schemas to capture product details, including attributes like product name, category, price, and quantity.
2. Develop APIs for creating, updating, and retrieving product information.
3. Implement logic to associate products with each transaction and store detailed purchase information within the transaction record.

### Handle Refunds for Transactions
When a customer requests a refund, the system should reverse the transaction by adding/updating the records. The transaction entries should reflect the refund, and the reporting APIs should adjust sales totals and inventory levels accordingly.

#### Steps
1. Implement logic to add/update transaction entries when a refund is processed.
2. Adjust inventory and financial records to account for the refunded products.
3. Update the reporting APIs to reflect changes in total credits, debits, net_flow, and inventory after refunds are processed.
