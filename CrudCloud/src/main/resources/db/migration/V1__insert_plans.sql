-- Flyway Migration V1: Insert initial plans
-- Date: 2025-11-13
-- Description: Insert CrudCloud subscription plans (Free, Standard, Premium)
-- These plans define the maximum number of database instances per subscription tier

INSERT INTO plans (name, max_instances, max_storage_mb, price_per_month, description) VALUES
('Free', 2, 150, 0.00, 'Plan Gratuito - 2 instancias, 150 MB almacenamiento'),
('Standard', 5, 750, 12000.00, 'Plan Estándar - 5 instancias, 750 MB almacenamiento'),
('Premium', 10, 2048, 39900.00, 'Plan Premium - 10 instancias, 2048 MB almacenamiento');
