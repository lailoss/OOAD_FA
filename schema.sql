-- phpMyAdmin SQL Dump
-- Database: parking_system
-- Complete schema for Parking Lot Management System

CREATE DATABASE IF NOT EXISTS parking_system;
USE parking_system;

-- 1. VEHICLES TABLE
CREATE TABLE IF NOT EXISTS vehicles (
    license_plate VARCHAR(20) PRIMARY KEY,
    vehicle_type ENUM('MOTORCYCLE', 'CAR', 'SUV', 'HANDICAPPED') NOT NULL,
    has_handicapped_card BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. PARKING SPOTS TABLE
CREATE TABLE IF NOT EXISTS parking_spots (
    spot_id VARCHAR(20) PRIMARY KEY,
    floor_number INT NOT NULL,
    row_number INT NOT NULL,
    spot_type ENUM('COMPACT', 'REGULAR', 'HANDICAPPED', 'RESERVED') NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED') DEFAULT 'AVAILABLE',
    current_vehicle VARCHAR(20),
    FOREIGN KEY (current_vehicle) REFERENCES vehicles(license_plate)
);

-- 3. TICKETS TABLE
CREATE TABLE IF NOT EXISTS tickets (
    ticket_id VARCHAR(50) PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    spot_id VARCHAR(20) NOT NULL,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (license_plate) REFERENCES vehicles(license_plate),
    FOREIGN KEY (spot_id) REFERENCES parking_spots(spot_id),
    INDEX idx_active (is_active),
    INDEX idx_plate (license_plate)
);

-- 4. FINES TABLE
CREATE TABLE IF NOT EXISTS fines (
    fine_id VARCHAR(50) PRIMARY KEY,
    license_plate VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    issued_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(255) NOT NULL,
    is_paid BOOLEAN DEFAULT FALSE,
    paid_date TIMESTAMP NULL,
    FOREIGN KEY (license_plate) REFERENCES vehicles(license_plate),
    INDEX idx_unpaid (is_paid)
);

-- 5. PAYMENTS TABLE
CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'CARD') NOT NULL,
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'COMPLETED', 'FAILED') DEFAULT 'COMPLETED',
    card_last_four VARCHAR(4),
    cash_tendered DECIMAL(10,2),
    change_amount DECIMAL(10,2)
);

-- 6. RECEIPTS TABLE
CREATE TABLE IF NOT EXISTS receipts (
    receipt_id VARCHAR(50) PRIMARY KEY,
    ticket_id VARCHAR(50) NOT NULL,
    exit_time TIMESTAMP NOT NULL,
    duration_hours INT NOT NULL,
    parking_fee DECIMAL(10,2) NOT NULL,
    total_fines DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_id VARCHAR(50),
    FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id),
    FOREIGN KEY (payment_id) REFERENCES payments(payment_id)
);

-- 7. FINE_SCHEMES TABLE
CREATE TABLE IF NOT EXISTS fine_schemes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    scheme_type ENUM('FIXED', 'PROGRESSIVE', 'HOURLY') NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 8. REVENUE_SUMMARY TABLE
CREATE TABLE IF NOT EXISTS revenue_summary (
    date DATE PRIMARY KEY,
    total_parking_fees DECIMAL(10,2) DEFAULT 0,
    total_fines_collected DECIMAL(10,2) DEFAULT 0,
    total_revenue DECIMAL(10,2) DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default fine schemes (Option A = FIXED is default)
INSERT INTO fine_schemes (scheme_type, is_active) VALUES 
('FIXED', true),
('PROGRESSIVE', false),
('HOURLY', false);

-- Generate parking spots (5 floors, 3 rows, 10 spots per row)
INSERT INTO parking_spots (spot_id, floor_number, row_number, spot_type, hourly_rate, status) VALUES
-- Floor 1
('F1-R1-S1', 1, 1, 'HANDICAPPED', 2.00, 'AVAILABLE'),
('F1-R1-S2', 1, 1, 'HANDICAPPED', 2.00, 'AVAILABLE'),
('F1-R1-S3', 1, 1, 'COMPACT', 2.00, 'AVAILABLE'),
('F1-R1-S4', 1, 1, 'COMPACT', 2.00, 'AVAILABLE'),
('F1-R1-S5', 1, 1, 'COMPACT', 2.00, 'AVAILABLE'),
('F1-R1-S6', 1, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R1-S7', 1, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R1-S8', 1, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R1-S9', 1, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R1-S10', 1, 1, 'REGULAR', 5.00, 'AVAILABLE'),
-- Floor 1 Row 2
('F1-R2-S1', 1, 2, 'COMPACT', 2.00, 'AVAILABLE'),
('F1-R2-S2', 1, 2, 'COMPACT', 2.00, 'AVAILABLE'),
('F1-R2-S3', 1, 2, 'COMPACT', 2.00, 'AVAILABLE'),
('F1-R2-S4', 1, 2, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R2-S5', 1, 2, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R2-S6', 1, 2, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R2-S7', 1, 2, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R2-S8', 1, 2, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R2-S9', 1, 2, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R2-S10', 1, 2, 'REGULAR', 5.00, 'AVAILABLE'),
-- Floor 1 Row 3
('F1-R3-S1', 1, 3, 'COMPACT', 2.00, 'AVAILABLE'),
('F1-R3-S2', 1, 3, 'COMPACT', 2.00, 'AVAILABLE'),
('F1-R3-S3', 1, 3, 'COMPACT', 2.00, 'AVAILABLE'),
('F1-R3-S4', 1, 3, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R3-S5', 1, 3, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R3-S6', 1, 3, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R3-S7', 1, 3, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R3-S8', 1, 3, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R3-S9', 1, 3, 'REGULAR', 5.00, 'AVAILABLE'),
('F1-R3-S10', 1, 3, 'REGULAR', 5.00, 'AVAILABLE'),

-- Floor 2 (similar pattern - continue for floors 2-4)
('F2-R1-S1', 2, 1, 'COMPACT', 2.00, 'AVAILABLE'),
('F2-R1-S2', 2, 1, 'COMPACT', 2.00, 'AVAILABLE'),
('F2-R1-S3', 2, 1, 'COMPACT', 2.00, 'AVAILABLE'),
('F2-R1-S4', 2, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F2-R1-S5', 2, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F2-R1-S6', 2, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F2-R1-S7', 2, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F2-R1-S8', 2, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F2-R1-S9', 2, 1, 'REGULAR', 5.00, 'AVAILABLE'),
('F2-R1-S10', 2, 1, 'REGULAR', 5.00, 'AVAILABLE'),

-- ... (continue for all floors, rows, spots)

-- Floor 5 Reserved spots
('F5-R3-S8', 5, 3, 'RESERVED', 10.00, 'AVAILABLE'),
('F5-R3-S9', 5, 3, 'RESERVED', 10.00, 'AVAILABLE'),
('F5-R3-S10', 5, 3, 'RESERVED', 10.00, 'AVAILABLE');

-- Create indexes
CREATE INDEX idx_tickets_plate ON tickets(license_plate);
CREATE INDEX idx_tickets_active ON tickets(is_active);
CREATE INDEX idx_fines_unpaid ON fines(is_paid);
CREATE INDEX idx_fines_plate ON fines(license_plate);

-- Sample query to verify data
SELECT COUNT(*) as total_spots FROM parking_spots;
SELECT spot_type, COUNT(*) as count FROM parking_spots GROUP BY spot_type;