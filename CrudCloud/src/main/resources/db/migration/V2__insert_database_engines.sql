-- Flyway Migration V2: Insert database engines
-- Date: 2025-11-13
-- Description: Insert supported database engines for CrudCloud
-- Includes PostgreSQL, MySQL, MongoDB, Redis, SQL Server, and Cassandra

INSERT INTO database_engines (name, version, default_port, docker_image, description) VALUES

-- PostgreSQL versions
('PostgreSQL', '14', 5432, 'postgres:14', 'PostgreSQL 14 - Open-source relational database'),
('PostgreSQL', '15', 5432, 'postgres:15', 'PostgreSQL 15 - Latest stable release'),
('PostgreSQL', '16', 5432, 'postgres:16', 'PostgreSQL 16 - Latest version'),

-- MySQL
('MySQL', '8.0', 3306, 'mysql:8.0', 'MySQL 8.0 - Open-source relational database'),

-- MongoDB
('MongoDB', '6.0', 27017, 'mongo:6.0', 'MongoDB 6.0 - NoSQL document database'),

-- Redis
('Redis', '7.0', 6379, 'redis:7.0', 'Redis 7.0 - In-memory data structure store'),

-- SQL Server
('SQL Server', '2022', 1433, 'mcr.microsoft.com/mssql/server:2022-latest', 'SQL Server 2022 - Microsoft relational database'),

-- Cassandra
('Cassandra', '4.1', 9042, 'cassandra:4.1', 'Cassandra 4.1 - Distributed NoSQL database');
