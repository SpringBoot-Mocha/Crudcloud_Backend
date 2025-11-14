-- Flyway Migration V1: Insert initial plans
-- Date: 2025-11-13
-- Description: Insert CrudCloud subscription plans (Free, Standard, Premium)
-- These plans define the maximum number of database instances per subscription tier

INSERT INTO plans (name, max_instances, price_per_month, description) VALUES
('Free', 2, 0.00, 'Free plan with 2 database instances - perfect for testing and development'),
('Standard', 5, 15.00, 'Standard plan with 5 database instances - ideal for growing projects'),
('Premium', 10, 50.00, 'Premium plan with 10 database instances - for production-grade applications');
