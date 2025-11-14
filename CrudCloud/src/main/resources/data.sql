-- ============================================================================
-- INITIAL DATA FOR CRUDCLOUD - PRICING & STORAGE CONFIGURATION
-- ============================================================================
-- This file is automatically loaded by Spring Boot on startup
-- Only runs if the tables exist (Hibernate DDL creates them first)

-- ============================================================================
-- PLANS (Free, Standard, Premium) in COP
-- ============================================================================
INSERT INTO plan (id, name, max_instances, price, storage_limit_mb, currency, description, created_at, updated_at) VALUES
(1, 'Free', 2, 0.00, 200, 'COP', 'Perfect for learning - 2 instances max, 200 MB storage', NOW(), NOW()),
(2, 'Standard', 5, 89900.00, 1500, 'COP', 'For small projects - 5 instances max, 1.5 GB storage', NOW(), NOW()),
(3, 'Premium', 10, 179900.00, 5000, 'COP', 'For production - 10 instances max, 5 GB storage', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ============================================================================
-- DATABASE ENGINES (6 supported databases with storage estimates)
-- ============================================================================
INSERT INTO database_engine (id, name, version, default_port, docker_image, storage_estimate_mb, description, created_at, updated_at) VALUES
(1, 'PostgreSQL', '15', 5432, 'postgres:15-alpine', 100, 'Powerful open-source relational database', NOW(), NOW()),
(2, 'MySQL', '8.0', 3306, 'mysql:8.0-alpine', 120, 'Popular relational database', NOW(), NOW()),
(3, 'MongoDB', '7.0', 27017, 'mongo:7.0', 150, 'NoSQL document database', NOW(), NOW()),
(4, 'Redis', '7.0', 6379, 'redis:7.0-alpine', 30, 'In-memory data structure store', NOW(), NOW()),
(5, 'SQL Server', '2022', 1433, 'mcr.microsoft.com/mssql/server:2022-latest', 500, 'Enterprise relational database', NOW(), NOW()),
(6, 'Cassandra', '4.1', 9042, 'cassandra:4.1', 200, 'Distributed NoSQL database', NOW(), NOW())
ON CONFLICT DO NOTHING;