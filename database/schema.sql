CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;
CREATE TABLE IF NOT EXISTS rooms (
 id INT PRIMARY KEY, number VARCHAR(10) NOT NULL UNIQUE, type VARCHAR(30) NOT NULL,
 capacity INT NOT NULL CHECK(capacity>0), rate DECIMAL(12,2) NOT NULL CHECK(rate>=0)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS bookings (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, room_id INT NOT NULL,
 guest VARCHAR(100) NOT NULL, email VARCHAR(150) NOT NULL, guests INT NOT NULL CHECK(guests>0),
 arrival DATE NOT NULL, departure DATE NOT NULL,
 status ENUM('RESERVED','CHECKED_IN','CHECKED_OUT','CANCELLED') NOT NULL,
 nightly_rate DECIMAL(12,2) NOT NULL, extras DECIMAL(12,2) NOT NULL DEFAULT 0,
 tax_percent DECIMAL(5,2) NOT NULL DEFAULT 0, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_room FOREIGN KEY(room_id) REFERENCES rooms(id),
 CHECK(departure>arrival), CHECK(nightly_rate>=0), CHECK(extras>=0), CHECK(tax_percent BETWEEN 0 AND 100),
 INDEX idx_room_dates (room_id,status,arrival,departure), INDEX idx_guest (guest)
) ENGINE=InnoDB;
INSERT IGNORE INTO rooms VALUES(1,'101','Standard',2,1800),(2,'102','Standard',2,1800),(3,'201','Deluxe',3,3200),(4,'202','Deluxe',3,3200),(5,'301','Suite',4,5500),(6,'302','Suite',4,5500);
-- Create a dedicated local app user separately (choose your own password):
-- CREATE USER 'hotel_app'@'localhost' IDENTIFIED BY 'your-password';
-- GRANT SELECT, INSERT, UPDATE ON hotel_management.* TO 'hotel_app'@'localhost';
