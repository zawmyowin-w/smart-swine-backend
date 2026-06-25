-- ============================================================
-- SMART SWINE SYSTEM - DATABASE SCHEMA
-- Database: MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_swine_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_swine_db;

-- ============================================================
-- USERS & ROLES
-- ============================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role ENUM('SUPER_ADMIN','SYSTEM_ADMIN','HR','MANAGER','SUPERVISOR','STAFF','CUSTOMER') NOT NULL DEFAULT 'STAFF',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    profile_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- FARM & BUILDINGS
-- ============================================================
CREATE TABLE farms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    manager_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE buildings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type ENUM('BREEDING','PREGNANCY','FARROWING','NURSERY','GROWING','FINISHING','ISOLATION') NOT NULL,
    capacity INT NOT NULL DEFAULT 0,
    current_count INT NOT NULL DEFAULT 0,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farm_id) REFERENCES farms(id) ON DELETE CASCADE
);

-- ============================================================
-- PIG LIFECYCLE
-- ============================================================
CREATE TABLE pigs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_number VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100),
    gender ENUM('MALE','FEMALE') NOT NULL,
    breed VARCHAR(100),
    birth_date DATE,
    weight_kg DECIMAL(8,2),
    status ENUM('ACTIVE','BREEDING','PREGNANT','SOLD','DECEASED','AVAILABLE') NOT NULL DEFAULT 'ACTIVE',
    building_id BIGINT,
    farm_id BIGINT NOT NULL,
    parent_sow_id BIGINT,
    parent_boar_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE SET NULL,
    FOREIGN KEY (farm_id) REFERENCES farms(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_sow_id) REFERENCES pigs(id) ON DELETE SET NULL,
    FOREIGN KEY (parent_boar_id) REFERENCES pigs(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE breeding_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sow_id BIGINT NOT NULL,
    boar_id BIGINT NOT NULL,
    breeding_date DATE NOT NULL,
    method ENUM('NATURAL','ARTIFICIAL') NOT NULL DEFAULT 'NATURAL',
    status ENUM('PENDING','CONFIRMED','FAILED') NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    recorded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sow_id) REFERENCES pigs(id) ON DELETE CASCADE,
    FOREIGN KEY (boar_id) REFERENCES pigs(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE pregnancies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    breeding_record_id BIGINT NOT NULL,
    sow_id BIGINT NOT NULL,
    confirmed_date DATE,
    expected_birth_date DATE,
    actual_birth_date DATE,
    status ENUM('CONFIRMED','IN_PROGRESS','COMPLETED','FAILED') NOT NULL DEFAULT 'CONFIRMED',
    notes TEXT,
    confirmed_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (breeding_record_id) REFERENCES breeding_records(id) ON DELETE CASCADE,
    FOREIGN KEY (sow_id) REFERENCES pigs(id) ON DELETE CASCADE,
    FOREIGN KEY (confirmed_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE births (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pregnancy_id BIGINT NOT NULL,
    sow_id BIGINT NOT NULL,
    birth_date DATE NOT NULL,
    total_born INT NOT NULL DEFAULT 0,
    born_alive INT NOT NULL DEFAULT 0,
    stillborn INT NOT NULL DEFAULT 0,
    buffer_count INT NOT NULL DEFAULT 0,
    available_count INT NOT NULL DEFAULT 0,
    status ENUM('PENDING_HR','HR_CONFIRMED','AVAILABLE') NOT NULL DEFAULT 'PENDING_HR',
    supervisor_id BIGINT,
    hr_confirmed_by BIGINT,
    hr_confirmed_at TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (pregnancy_id) REFERENCES pregnancies(id) ON DELETE CASCADE,
    FOREIGN KEY (sow_id) REFERENCES pigs(id) ON DELETE CASCADE,
    FOREIGN KEY (supervisor_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (hr_confirmed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- MARKETPLACE & PRODUCTS
-- ============================================================
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category ENUM('LIVE_PIG','PORK_MEAT','PIGLET','OTHER') NOT NULL DEFAULT 'LIVE_PIG',
    price DECIMAL(12,2) NOT NULL,
    unit VARCHAR(50) NOT NULL DEFAULT 'head',
    stock_quantity INT NOT NULL DEFAULT 0,
    min_order INT NOT NULL DEFAULT 1,
    max_order INT,
    image_url VARCHAR(500),
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    birth_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (birth_id) REFERENCES births(id) ON DELETE SET NULL
);

-- ============================================================
-- CART & ORDERS
-- ============================================================
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_cart_product (cart_id, product_id)
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING','STOCK_RESERVED','PAYMENT_PENDING','PAYMENT_VERIFIED','CONFIRMED','DELIVERING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    delivery_address TEXT,
    delivery_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ============================================================
-- PAYMENTS
-- ============================================================
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    method ENUM('KPAY','WAVE','CASH','BANK_TRANSFER') NOT NULL,
    qr_code_url VARCHAR(500),
    qr_code_data TEXT,
    status ENUM('PENDING','CUSTOMER_PAID','FINANCE_VERIFIED','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(200),
    payment_screenshot VARCHAR(500),
    paid_at TIMESTAMP NULL,
    verified_at TIMESTAMP NULL,
    verified_by BIGINT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- INVENTORY (Feed & Medicine)
-- ============================================================
CREATE TABLE inventory_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category ENUM('FEED','MEDICINE','SUPPLEMENT','EQUIPMENT','OTHER') NOT NULL,
    unit VARCHAR(50) NOT NULL DEFAULT 'kg',
    current_stock DECIMAL(12,2) NOT NULL DEFAULT 0,
    min_stock DECIMAL(12,2) NOT NULL DEFAULT 0,
    unit_cost DECIMAL(12,2) NOT NULL DEFAULT 0,
    supplier VARCHAR(200),
    farm_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (farm_id) REFERENCES farms(id) ON DELETE SET NULL
);

CREATE TABLE inventory_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL,
    type ENUM('IN','OUT','ADJUSTMENT') NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    unit_cost DECIMAL(12,2),
    total_cost DECIMAL(12,2),
    reason VARCHAR(255),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    performed_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- FINANCE
-- ============================================================
CREATE TABLE revenue_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    payment_id BIGINT,
    amount DECIMAL(12,2) NOT NULL,
    description VARCHAR(255),
    record_date DATE NOT NULL,
    month_year VARCHAR(7) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL
);

CREATE TABLE expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category ENUM('FEED','MEDICINE','LABOR','UTILITIES','MAINTENANCE','TRANSPORT','OTHER') NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    description VARCHAR(255),
    reference_id BIGINT,
    reference_type VARCHAR(50),
    expense_date DATE NOT NULL,
    month_year VARCHAR(7) NOT NULL,
    recorded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- AUDIT LOGS
-- ============================================================
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- HEALTH RECORDS
-- ============================================================
CREATE TABLE health_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pig_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    type ENUM('VACCINATION','TREATMENT','CHECKUP','DEWORMING') NOT NULL,
    diagnosis TEXT,
    treatment TEXT,
    medicine_used VARCHAR(255),
    dosage VARCHAR(100),
    vet_name VARCHAR(100),
    next_checkup_date DATE,
    status ENUM('ONGOING','RECOVERED','DECEASED') NOT NULL DEFAULT 'ONGOING',
    recorded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pig_id) REFERENCES pigs(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE feeding_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    building_id BIGINT NOT NULL,
    inventory_item_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    feed_date DATE NOT NULL,
    feed_time TIME,
    pig_count INT,
    notes TEXT,
    recorded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_pigs_status ON pigs(status);
CREATE INDEX idx_pigs_farm ON pigs(farm_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
CREATE INDEX idx_revenue_month ON revenue_records(month_year);
CREATE INDEX idx_expenses_month ON expenses(month_year);

-- ============================================================
-- SEED DATA - DEFAULT SUPER ADMIN
-- Password: Admin@123 (BCrypt encoded)
-- ============================================================
INSERT INTO users (username, password, full_name, email, role) VALUES
('superadmin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj2NjWHyNi.q', 'Super Administrator', 'admin@smartswine.com', 'SUPER_ADMIN'),
('hr_manager', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj2NjWHyNi.q', 'HR Manager', 'hr@smartswine.com', 'HR'),
('farm_manager', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj2NjWHyNi.q', 'Farm Manager', 'manager@smartswine.com', 'MANAGER'),
('supervisor1', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj2NjWHyNi.q', 'Farm Supervisor', 'supervisor@smartswine.com', 'SUPERVISOR'),
('staff1', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj2NjWHyNi.q', 'Farm Staff', 'staff@smartswine.com', 'STAFF'),
('customer1', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj2NjWHyNi.q', 'Test Customer', 'customer@example.com', 'CUSTOMER');

INSERT INTO farms (name, location, description, manager_id) VALUES
('Smart Farm 1', 'Yangon, Myanmar', 'Main pig farm facility', 3);

INSERT INTO buildings (farm_id, name, type, capacity) VALUES
(1, 'Building A - Breeding', 'BREEDING', 50),
(1, 'Building B - Pregnancy', 'PREGNANCY', 40),
(1, 'Building C - Farrowing', 'FARROWING', 30),
(1, 'Building D - Nursery', 'NURSERY', 100),
(1, 'Building E - Growing', 'GROWING', 80),
(1, 'Building F - Finishing', 'FINISHING', 60);

INSERT INTO inventory_items (name, category, unit, current_stock, min_stock, unit_cost, farm_id) VALUES
('Grower Feed', 'FEED', 'kg', 500, 100, 1500, 1),
('Sow Feed', 'FEED', 'kg', 300, 80, 1800, 1),
('Piglet Starter', 'FEED', 'kg', 200, 50, 2500, 1),
('Vitamin B Complex', 'MEDICINE', 'bottle', 20, 5, 5000, 1),
('Antibiotic - Amoxicillin', 'MEDICINE', 'bottle', 15, 3, 8000, 1),
('Vaccine - FMD', 'MEDICINE', 'dose', 100, 20, 3000, 1);
