-- ==========================================================
-- HRMS Database Schema & Initial Data
-- DBMS Course Project - Human Resource Management System
-- ==========================================================

CREATE DATABASE IF NOT EXISTS `hrms` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `hrms`;

-- 1. HR User Table (Admin / HR authentication)
CREATE TABLE IF NOT EXISTS `hr_user` (
    `h_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Employee Table
CREATE TABLE IF NOT EXISTS `employee` (
    `e_id` INT AUTO_INCREMENT PRIMARY KEY,
    `emp_code` VARCHAR(50) UNIQUE DEFAULT NULL,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `position` VARCHAR(100) DEFAULT NULL,
    `hire_date` DATE DEFAULT NULL,
    `salary` DECIMAL(12,2) DEFAULT NULL,
    `creation_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `h_id` INT DEFAULT NULL,
    CONSTRAINT `fk_employee_hr_user` FOREIGN KEY (`h_id`) REFERENCES `hr_user`(`h_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Requisition Table (Job openings / recruitment posts)
CREATE TABLE IF NOT EXISTS `requisition` (
    `r_id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT DEFAULT NULL,
    `h_id` INT DEFAULT NULL,
    `creation_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_requisition_hr_user` FOREIGN KEY (`h_id`) REFERENCES `hr_user`(`h_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Candidate Table (Job applicants)
CREATE TABLE IF NOT EXISTS `candidate` (
    `c_id` INT AUTO_INCREMENT PRIMARY KEY,
    `requisition_id` INT DEFAULT NULL,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `resume_url` VARCHAR(255) DEFAULT NULL,
    `status` VARCHAR(50) DEFAULT 'APPLIED',
    `creation_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_candidate_requisition` FOREIGN KEY (`requisition_id`) REFERENCES `requisition`(`r_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Attendance Table (Daily presence records)
CREATE TABLE IF NOT EXISTS `attendance` (
    `a_id` INT AUTO_INCREMENT PRIMARY KEY,
    `employee_id` INT NOT NULL,
    `date` DATE NOT NULL,
    `status` VARCHAR(20) NOT NULL,
    `reason` VARCHAR(255) DEFAULT NULL,
    `h_id` INT DEFAULT NULL,
    `record_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_attendance_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee`(`e_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_attendance_hr_user` FOREIGN KEY (`h_id`) REFERENCES `hr_user`(`h_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Payment Table (Salary & payroll disbursements)
CREATE TABLE IF NOT EXISTS `payment` (
    `p_id` INT AUTO_INCREMENT PRIMARY KEY,
    `employee_id` INT NOT NULL,
    `amount` DECIMAL(12,2) NOT NULL,
    `payment_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `method` VARCHAR(50) DEFAULT NULL,
    `remarks` VARCHAR(255) DEFAULT NULL,
    `h_id` INT DEFAULT NULL,
    `creation_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_payment_employee` FOREIGN KEY (`employee_id`) REFERENCES `employee`(`e_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_payment_hr_user` FOREIGN KEY (`h_id`) REFERENCES `hr_user`(`h_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================================
-- Initial Sample Seed Data
-- ==========================================================

-- Insert Default HR Admin (Username: admin, Password: password123)
INSERT INTO `hr_user` (`username`, `password`, `full_name`)
VALUES ('admin', 'password123', 'Administrator')
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- Sample Employees
INSERT INTO `employee` (`emp_code`, `name`, `email`, `phone`, `position`, `hire_date`, `salary`, `creation_time`, `h_id`)
VALUES 
('EMP-1001', 'Alice Johnson', 'alice.johnson@example.com', '+1-555-0101', 'Senior Software Engineer', '2023-01-15', 95000.00, NOW(), 1),
('EMP-1002', 'David Miller', 'david.miller@example.com', '+1-555-0102', 'Product Manager', '2023-03-01', 105000.00, NOW(), 1),
('EMP-1003', 'Sarah Williams', 'sarah.w@example.com', '+1-555-0103', 'UI/UX Designer', '2023-06-20', 82000.00, NOW(), 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- Sample Job Requisitions
INSERT INTO `requisition` (`title`, `description`, `h_id`, `creation_time`)
VALUES 
('Full Stack Java Developer', 'Looking for a Spring Boot and modern frontend developer with 3+ years experience.', 1, NOW()),
('Database Administrator', 'Responsible for MySQL optimization, backups, and data architecture.', 1, NOW());

-- Sample Candidates
INSERT INTO `candidate` (`requisition_id`, `name`, `email`, `phone`, `resume_url`, `status`, `creation_time`)
VALUES 
(1, 'Michael Brown', 'michael.b@example.com', '+1-555-0201', 'https://example.com/resumes/michael-brown.pdf', 'INTERVIEW', NOW()),
(1, 'Emma Watson', 'emma.w@example.com', '+1-555-0202', 'https://example.com/resumes/emma-watson.pdf', 'APPLIED', NOW()),
(2, 'Robert Green', 'robert.g@example.com', '+1-555-0203', 'https://example.com/resumes/robert-green.pdf', 'SHORTLISTED', NOW());

-- Sample Attendance
INSERT INTO `attendance` (`employee_id`, `date`, `status`, `reason`, `h_id`, `record_time`)
VALUES 
(1, CURDATE(), 'present', 'On-time arrival', 1, NOW()),
(2, CURDATE(), 'present', 'On-time arrival', 1, NOW()),
(3, CURDATE(), 'late', 'Traffic delay', 1, NOW());

-- Sample Payments
INSERT INTO `payment` (`employee_id`, `amount`, `payment_date`, `method`, `remarks`, `h_id`, `creation_time`)
VALUES 
(1, 7916.66, NOW(), 'Bank Transfer', 'Monthly Salary', 1, NOW()),
(2, 8750.00, NOW(), 'Bank Transfer', 'Monthly Salary', 1, NOW()),
(3, 6833.33, NOW(), 'Bank Transfer', 'Monthly Salary', 1, NOW());
