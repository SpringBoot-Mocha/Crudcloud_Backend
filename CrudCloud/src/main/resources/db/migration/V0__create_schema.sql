-- ============================================================================
-- V0__create_schema.sql - Initial Schema Creation for CrudCloud
-- ============================================================================
-- This migration creates all base tables for the CrudCloud platform
-- Tables are created in dependency order to avoid foreign key violations
-- ============================================================================

-- ============================================================================
-- TABLE: users
-- Description: Stores user accounts (individuals and organizations)
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_organization BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at DESC);

-- ============================================================================
-- TABLE: plans
-- Description: Subscription plans (Free, Standard, Premium)
-- ============================================================================
CREATE TABLE IF NOT EXISTS plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    max_instances INTEGER NOT NULL,
    max_storage_gb BIGINT NOT NULL,
    price_per_month NUMERIC(10, 2) NOT NULL,
    description TEXT
);

-- Create index for plan lookups
CREATE INDEX IF NOT EXISTS idx_plans_name ON plans(name);

-- ============================================================================
-- TABLE: database_engines
-- Description: Available database engines (PostgreSQL, MySQL, MongoDB, etc.)
-- ============================================================================
CREATE TABLE IF NOT EXISTS database_engines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    default_port INTEGER NOT NULL,
    docker_image VARCHAR(255) NOT NULL,
    description TEXT
);

-- Create indexes for engine lookups
CREATE INDEX IF NOT EXISTS idx_database_engines_name ON database_engines(name);
CREATE INDEX IF NOT EXISTS idx_database_engine_name_version ON database_engines(name, version);

-- ============================================================================
-- TABLE: subscriptions
-- Description: User subscriptions to plans
-- ============================================================================
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_subscriptions_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_subscriptions_plan_id FOREIGN KEY (plan_id) REFERENCES plans(id)
);

-- Create indexes for subscription queries
CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_plan_id ON subscriptions(plan_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_is_active ON subscriptions(is_active);

-- ============================================================================
-- TABLE: database_instances
-- Description: User database instances running in Docker containers
-- ============================================================================
CREATE TABLE IF NOT EXISTS database_instances (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    database_engine_id BIGINT NOT NULL,
    container_name VARCHAR(255) NOT NULL UNIQUE,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_database_instances_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_database_instances_subscription_id FOREIGN KEY (subscription_id) REFERENCES subscriptions(id),
    CONSTRAINT fk_database_instances_database_engine_id FOREIGN KEY (database_engine_id) REFERENCES database_engines(id)
);

-- Create indexes for instance queries
CREATE INDEX IF NOT EXISTS idx_database_instances_user_id ON database_instances(user_id);
CREATE INDEX IF NOT EXISTS idx_database_instances_subscription_id ON database_instances(subscription_id);
CREATE INDEX IF NOT EXISTS idx_database_instances_status ON database_instances(status);

-- ============================================================================
-- TABLE: credentials
-- Description: Database access credentials (encrypted passwords)
-- ============================================================================
CREATE TABLE IF NOT EXISTS credentials (
    id BIGSERIAL PRIMARY KEY,
    database_instance_id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL,
    encrypted_password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_credentials_database_instance_id FOREIGN KEY (database_instance_id) REFERENCES database_instances(id) ON DELETE CASCADE
);

-- Create index for credential lookups
CREATE INDEX IF NOT EXISTS idx_credentials_database_instance_id ON credentials(database_instance_id);

-- ============================================================================
-- TABLE: transactions
-- Description: Payment transactions from Mercado Pago
-- ============================================================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mercadopago_payment_id VARCHAR(255) NOT NULL UNIQUE,
    amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_transactions_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for transaction queries
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_mercadopago_payment_id ON transactions(mercadopago_payment_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);

-- ============================================================================
-- End of V0__create_schema.sql
-- ============================================================================
