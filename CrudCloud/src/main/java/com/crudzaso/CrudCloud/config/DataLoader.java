package com.crudzaso.CrudCloud.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * DataLoader: Executes initial data inserts on application startup
 *
 * This component replaces Flyway migrations for databases that aren't yet supported
 * (e.g., PostgreSQL 18.0 with Flyway 11.1.0)
 *
 * When Flyway updates to support PostgreSQL 18, this can be removed
 */
@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Load initial data
            loadPlans(statement);
            loadDatabaseEngines(statement);

            System.out.println("✅ Initial data loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ Error loading initial data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPlans(Statement statement) throws Exception {
        try {
            // Check if plans already exist
            var resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM plans");
            if (resultSet.next() && resultSet.getInt("count") > 0) {
                System.out.println("✅ Plans already exist, skipping insertion");
                return;
            }
        } catch (Exception e) {
            // Table might not exist yet, that's OK
        }

        String plansSql = """
            INSERT INTO plans (name, max_instances, max_storage_gb, price_per_month, description) VALUES
            ('Free', 2, 50, 0.00, 'Free plan with 2 database instances - perfect for testing and development'),
            ('Standard', 5, 500, 15.00, 'Standard plan with 5 database instances - ideal for growing projects'),
            ('Premium', 10, 2000, 50.00, 'Premium plan with 10 database instances - for production-grade applications')
            ON CONFLICT DO NOTHING
            """;

        try {
            statement.execute(plansSql);
            System.out.println("✅ Plans inserted successfully");
        } catch (Exception e) {
            System.err.println("⚠️ Plans insertion error (might be duplicate): " + e.getMessage());
        }
    }

    private void loadDatabaseEngines(Statement statement) throws Exception {
        try {
            // Check if engines already exist
            var resultSet = statement.executeQuery("SELECT COUNT(*) as count FROM database_engines");
            if (resultSet.next() && resultSet.getInt("count") > 0) {
                System.out.println("✅ Database engines already exist, skipping insertion");
                return;
            }
        } catch (Exception e) {
            // Table might not exist yet, that's OK
        }

        String enginesSql = """
            INSERT INTO database_engines (name, version, default_port, docker_image, description) VALUES
            ('PostgreSQL', '14', 5432, 'postgres:14', 'PostgreSQL 14 - Open-source relational database'),
            ('PostgreSQL', '15', 5432, 'postgres:15', 'PostgreSQL 15 - Latest stable release'),
            ('PostgreSQL', '16', 5432, 'postgres:16', 'PostgreSQL 16 - Latest version'),
            ('MySQL', '8.0', 3306, 'mysql:8.0', 'MySQL 8.0 - Open-source relational database'),
            ('MongoDB', '6.0', 27017, 'mongo:6.0', 'MongoDB 6.0 - NoSQL document database'),
            ('Redis', '7.0', 6379, 'redis:7.0', 'Redis 7.0 - In-memory data structure store'),
            ('SQL Server', '2022', 1433, 'mcr.microsoft.com/mssql/server:2022-latest', 'SQL Server 2022 - Microsoft relational database'),
            ('Cassandra', '4.1', 9042, 'cassandra:4.1', 'Cassandra 4.1 - Distributed NoSQL database')
            ON CONFLICT DO NOTHING
            """;

        try {
            statement.execute(enginesSql);
            System.out.println("✅ Database engines inserted successfully");
        } catch (Exception e) {
            System.err.println("⚠️ Database engines insertion error (might be duplicate): " + e.getMessage());
        }
    }
}
