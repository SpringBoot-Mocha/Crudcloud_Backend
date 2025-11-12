-- ============================================================================
-- SCHEMA CrudCloud MVP - PostgreSQL 14+
-- Base de datos minimalista para gestión de instancias de BD en la nube
-- Sintaxis específica para PostgreSQL
-- ============================================================================

-- ============================================================================
-- ENUM TYPES - Definir tipos enumerados
-- ============================================================================
CREATE TYPE instance_status AS ENUM (
    'CREATING',
    'RUNNING',
    'SUSPENDED',
    'DELETED'
);

CREATE TYPE transaction_status AS ENUM (
    'PENDING',
    'APPROVED',
    'FAILED'
);

-- ============================================================================
-- 1. USERS - Usuarios del sistema
-- ============================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_organization BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- ============================================================================
-- 2. PLANS - Planes de suscripción (Free/Standard/Premium)
-- ============================================================================
CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    max_instances INT NOT NULL CHECK (max_instances > 0),
    max_storage_gb BIGINT NOT NULL DEFAULT 100,
    price_per_month DECIMAL(10, 2) NOT NULL DEFAULT 0 CHECK (price_per_month >= 0),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índice para plans
CREATE INDEX idx_plans_name ON plans(name);

-- Insertar planes por defecto
INSERT INTO plans (name, max_instances, max_storage_gb, price_per_month, description) VALUES
('Free', 2, 10, 0.00, 'Free plan with limited instances and storage'),
('Standard', 5, 100, 15.00, 'Standard plan with moderate instances and storage'),
('Premium', 10, 500, 50.00, 'Premium plan with maximum instances and storage');

-- ============================================================================
-- TEST DATA - USUARIOS DE PRUEBA
-- ============================================================================
INSERT INTO users (email, password_hash, name, is_organization, created_at, updated_at) VALUES
('admin@crudcloud.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy990qm', 'Admin User', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('john.doe@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy990qm', 'John Doe', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('jane.smith@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy990qm', 'Jane Smith', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('acme.corp@company.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy990qm', 'ACME Corporation', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('startup.io@example.com', '$2a$10$slYQmyNdGzin7olVN3p5be4DlH.PKZbv5H8KnzzVgXXbVxzy990qm', 'Startup IO', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- 3. SUBSCRIPTIONS - Suscripciones activas de usuarios
-- ============================================================================
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id BIGINT NOT NULL REFERENCES plans(id) ON DELETE RESTRICT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para subscriptions
CREATE INDEX idx_subscriptions_user_active ON subscriptions(user_id, is_active);
CREATE INDEX idx_subscriptions_created_at ON subscriptions(created_at DESC);

-- TEST DATA - SUSCRIPCIONES DE PRUEBA
INSERT INTO subscriptions (user_id, plan_id, is_active, created_at, updated_at) VALUES
(1, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),  -- Admin -> Premium
(2, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),  -- John -> Free
(3, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),  -- Jane -> Standard
(4, 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),  -- ACME -> Premium
(5, 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);  -- Startup IO -> Standard

-- ============================================================================
-- 4. DATABASE_ENGINES - Motores de BD disponibles
-- ============================================================================
CREATE TABLE database_engines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    version VARCHAR(50),
    default_port INT NOT NULL,
    docker_image VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índice para database_engines
CREATE INDEX idx_database_engines_name ON database_engines(name);

-- Insertar motores por defecto
INSERT INTO database_engines (name, version, default_port, docker_image, description) VALUES
('MySQL', '8.0', 3306, 'mysql:8.0', 'MySQL 8.0 - Relational Database'),
('PostgreSQL', '14', 5432, 'postgres:14', 'PostgreSQL 14 - Advanced Relational Database'),
('MongoDB', '6.0', 27017, 'mongo:6.0', 'MongoDB 6.0 - NoSQL Document Database'),
('Redis', '7.0', 6379, 'redis:7.0', 'Redis 7.0 - In-Memory Data Structure Store'),
('SQL Server', '2022', 1433, 'mcr.microsoft.com/mssql/server:2022-latest', 'SQL Server 2022 - Enterprise Database'),
('Cassandra', '4.1', 9042, 'cassandra:4.1', 'Cassandra 4.1 - NoSQL Distributed Database');

-- ============================================================================
-- 5. DATABASE_INSTANCES - Instancias de BD creadas por usuarios
-- ============================================================================
CREATE TABLE database_instances (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    engine_id BIGINT NOT NULL REFERENCES database_engines(id) ON DELETE RESTRICT,
    instance_name VARCHAR(255) NOT NULL,
    container_id VARCHAR(255),
    status instance_status DEFAULT 'CREATING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para database_instances
CREATE INDEX idx_database_instances_user_status ON database_instances(user_id, status);
CREATE INDEX idx_database_instances_created_at ON database_instances(created_at DESC);
CREATE INDEX idx_database_instances_user_engine ON database_instances(user_id, engine_id);

-- TEST DATA - INSTANCIAS DE BASE DE DATOS DE PRUEBA
INSERT INTO database_instances (user_id, engine_id, instance_name, container_id, status, created_at, updated_at) VALUES
(1, 1, 'prod-mysql-01', 'abc123def456', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, 'prod-postgres-01', 'xyz789uvw012', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'dev-mysql-01', 'def456ghi789', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, 'dev-mongo-01', 'jkl012mno345', 'SUSPENDED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'test-postgres-01', 'pqr678stu901', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 1, 'acme-mysql-primary', 'vwx234yza567', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 5, 'acme-sqlserver-01', 'bcd890efg123', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- 6. CREDENTIALS - Credenciales de acceso a instancias
-- ============================================================================
CREATE TABLE credentials (
    id BIGSERIAL PRIMARY KEY,
    instance_id BIGINT NOT NULL UNIQUE REFERENCES database_instances(id) ON DELETE CASCADE,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL CHECK (port > 0 AND port < 65536),
    username VARCHAR(255) NOT NULL,
    database_name VARCHAR(255) DEFAULT 'defaultdb',
    password_encrypted VARCHAR(1000) NOT NULL,
    last_rotated_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índice para credentials
CREATE INDEX idx_credentials_instance ON credentials(instance_id);

-- TEST DATA - CREDENCIALES DE PRUEBA (Nota: Las contraseñas están encriptadas en producción)
INSERT INTO credentials (instance_id, host, port, username, database_name, password_encrypted, last_rotated_at, created_at, updated_at) VALUES
(1, 'prod-mysql-01.crudcloud.local', 3306, 'admin', 'crudcloud_db', 'encrypted_password_1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'prod-postgres-01.crudcloud.local', 5432, 'postgres', 'crudcloud_db', 'encrypted_password_2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'dev-mysql-01.crudcloud.local', 3306, 'developer', 'dev_database', 'encrypted_password_3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'dev-mongo-01.crudcloud.local', 27017, 'mongo_user', 'admin', 'encrypted_password_4', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'test-postgres-01.crudcloud.local', 5432, 'test_user', 'test_db', 'encrypted_password_5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'acme-mysql-primary.crudcloud.local', 3306, 'acme_admin', 'acme_production', 'encrypted_password_6', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'acme-sqlserver-01.crudcloud.local', 1433, 'sa', 'master', 'encrypted_password_7', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- 7. TRANSACTIONS - Transacciones de pago en Mercado Pago
-- ============================================================================
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id BIGINT REFERENCES plans(id) ON DELETE SET NULL,
    mercadopago_payment_id VARCHAR(255) UNIQUE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    status transaction_status DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices para transactions
CREATE INDEX idx_transactions_user_status ON transactions(user_id, status);
CREATE INDEX idx_transactions_mercadopago_id ON transactions(mercadopago_payment_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at DESC);

-- TEST DATA - TRANSACCIONES DE PRUEBA
INSERT INTO transactions (user_id, plan_id, mercadopago_payment_id, amount, status, created_at, updated_at) VALUES
(1, 3, 'MP20250101001', 50.00, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'MP20250102001', 0.00, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'MP20250103001', 15.00, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 3, 'MP20250104001', 50.00, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 'MP20250105001', 15.00, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 3, 'MP20250106001', 50.00, 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days'),
(3, 2, 'MP20250107001', 15.00, 'FAILED', CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '15 days');

-- ============================================================================
-- FIN DEL SCHEMA (7 TABLAS CORE) - PostgreSQL 14+
-- ============================================================================
-- Ejecutar en VPS:
--   psql -U postgres -f SCHEMA_CRUDCLOUD_POSTGRESQL.sql
--   O:
--   psql -U postgres -h localhost -c "\i SCHEMA_CRUDCLOUD_POSTGRESQL.sql"
-- ============================================================================
