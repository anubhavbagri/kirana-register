package com.jarapplication.kiranastore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * KIRANA STORE APPLICATION: Spring Boot Entry Point
 *
 * WHAT IT DOES:
 * ├─ Bootstrap point for the entire Kirana Store application
 * ├─ Triggers Spring's auto-configuration, component scanning, and bean creation
 * └─ Starts embedded Tomcat server (default port 8080)
 *
 * @SpringBootApplication IS A META-ANNOTATION (combines 3 annotations):
 * ├─ @Configuration: This class can define @Bean methods
 * ├─ @EnableAutoConfiguration: Spring Boot auto-configures based on classpath:
 * │   ├─ Detects spring-data-mongodb → auto-configures MongoTemplate, MongoRepository
 * │   ├─ Detects spring-data-jpa → auto-configures DataSource, EntityManager
 * │   ├─ Detects spring-security → auto-configures SecurityFilterChain
 * │   ├─ Detects spring-kafka → auto-configures KafkaListenerContainerFactory
 * │   ├─ Detects spring-data-redis → auto-configures RedisTemplate
 * │   └─ All from application.properties/yml configuration
 * │
 * └─ @ComponentScan(basePackages = "com.jarapplication.kiranastore"):
 *    ├─ Scans THIS package and all sub-packages for Spring components
 *    ├─ Finds: @Component, @Service, @Repository, @Controller, @RestController
 *    ├─ Finds: @Configuration, @Aspect, @Component
 *    └─ Creates beans for all discovered classes
 *
 * STARTUP ORDER:
 * ├─ 1. SpringApplication.run() starts
 * ├─ 2. @ComponentScan discovers all beans:
 * │   ├─ Controllers: UserController, ProductController, TransactionController, RefreshController
 * │   ├─ Services: AuthServiceImp, UserServiceImp, ProductServiceImp, TransactionServiceImpl, etc.
 * │   ├─ DAOs: UserDAO, ProductDao, TransactionDao, BillDao, ReportDao, RefreshTokenDAO
 * │   ├─ Repositories: UserRepository, ProductRepository, TransactionRepository, BillRepository, etc.
 * │   ├─ Filters: JwtFilter, RateLimiterFilter
 * │   ├─ Config: SecurityConfig (creates SecurityFilterChain, PasswordEncoder, AuthenticationManager)
 * │   ├─ AOP: CapitalizeAspect, RateLimiterAspect
 * │   ├─ Cache: CacheService → RedisStorageService
 * │   └─ Kafka: ReportKafkaListener
 * ├─ 3. Auto-configuration creates infrastructure beans
 * ├─ 4. Dependency injection wires all beans together
 * ├─ 5. Embedded Tomcat starts on port 8080
 * └─ 6. Application ready to serve HTTP requests
 */
@SpringBootApplication // ← @Configuration + @EnableAutoConfiguration + @ComponentScan
public class KiranaStoreApplication {

    /**
     * Java main method: JVM entry point.
     *
     * SpringApplication.run():
     * ├─ Creates ApplicationContext (Spring IoC container)
     * ├─ Performs component scanning
     * ├─ Creates and injects all beans
     * ├─ Starts embedded web server
     * └─ Blocks main thread (keeps app running)
     *
     * @param args ← Command-line arguments (e.g., --server.port=9090)
     */
    public static void main(String[] args) {
        SpringApplication.run(KiranaStoreApplication.class, args);
    }
}
