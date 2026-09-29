-- ====================================================================
-- Database Schema: Online Event Registration System
-- Database Name: event_db (or embedded H2 database)
-- ====================================================================

-- Create Database if not exists (for MySQL)
CREATE DATABASE IF NOT EXISTS event_db;
USE event_db;

-- --------------------------------------------------------------------
-- Table: users
-- Stores attendee and administrator credentials and profile details
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'USER', -- 'USER' or 'ADMIN'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- --------------------------------------------------------------------
-- Table: events
-- Stores event information, schedules, venue, and capacity
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL, -- 'Technology', 'Music', 'Workshop', 'Business', 'Sports', 'Cultural'
    event_date DATE NOT NULL,
    event_time VARCHAR(20) NOT NULL,
    venue VARCHAR(150) NOT NULL,
    organizer VARCHAR(100) NOT NULL,
    capacity INT NOT NULL DEFAULT 100,
    registered_count INT NOT NULL DEFAULT 0,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    banner_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'UPCOMING', -- 'UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- --------------------------------------------------------------------
-- Table: registrations
-- Stores event ticket bookings made by users
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    tickets_count INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    ticket_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'CANCELLED'
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- --------------------------------------------------------------------
-- Table: feedbacks
-- Stores attendee ratings and reviews for events
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS feedbacks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- ====================================================================
-- SEED DATA
-- ====================================================================

-- 1. Insert Default Users (Password: admin123 / user123)
INSERT INTO users (username, email, password, full_name, phone, role) VALUES
('admin', 'admin@events.local', 'admin123', 'System Administrator', '+1-555-0100', 'ADMIN'),
('john_doe', 'john@example.com', 'user123', 'John Doe', '+1-555-0101', 'USER'),
('sarah_connor', 'sarah@example.com', 'user123', 'Sarah Connor', '+1-555-0102', 'USER'),
('alex_kumar', 'alex@example.com', 'user123', 'Alex Kumar', '+1-555-0103', 'USER')
ON DUPLICATE KEY UPDATE username=username;

-- 2. Insert Sample Events
INSERT INTO events (title, description, category, event_date, event_time, venue, organizer, capacity, registered_count, price, banner_url, status) VALUES
('Global AI & Cloud Summit 2026', 'Explore the bleeding edge of Artificial Intelligence, Cloud Computing, and Neural Architectures with world-class tech leaders.', 'Technology', '2026-11-15', '09:30 AM', 'Tech Convention Center, Hall A', 'AI Innovations Lab', 250, 42, 49.99, 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800', 'UPCOMING'),

('International Jazz & Indie Beats', 'An evening of electrifying jazz, soul, and indie acoustic performances by award-winning global artists.', 'Music', '2026-10-25', '06:00 PM', 'Grand Symphony Amphitheatre', 'Harmony Productions', 400, 185, 29.00, 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800', 'UPCOMING'),

('Full-Stack Web3 & Microservices Workshop', 'Hands-on coding masterclass covering Microservices in Java, Docker deployment, and scalable system design.', 'Workshop', '2026-10-18', '10:00 AM', 'Silicon Valley Innovation Hub, Lab 3', 'CodeCraft Academy', 60, 48, 15.00, 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800', 'UPCOMING'),

('Global Venture Startup Pitchfest', 'Connect with angel investors, VC funds, and promising tech founders showcasing disruptive innovations.', 'Business', '2026-11-05', '01:00 PM', 'Metropolitan Business Tower, 14th Floor', 'Venture Hub Network', 150, 95, 35.00, 'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800', 'UPCOMING'),

('City Marathon & Fitness Carnival 2026', 'Annual 10K/21K run promoting health, wellness, and youth community sports with medals and refreshments.', 'Sports', '2026-11-20', '06:00 AM', 'Central Riverside Park Boulevard', 'City Sports Commission', 500, 320, 10.00, 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=800', 'UPCOMING'),

('Digital Arts & Immersive VR Exhibition', 'Interactive art installation featuring digital NFT galleries, 3D projection mapping, and VR experiences.', 'Cultural', '2026-12-02', '11:00 AM', 'Contemporary Design Museum', 'Creative Minds Collective', 120, 28, 20.00, 'https://images.unsplash.com/photo-1508997449629-303059a039c0?w=800', 'UPCOMING');

-- 3. Insert Initial Registrations
INSERT INTO registrations (user_id, event_id, tickets_count, total_price, ticket_code, status) VALUES
(2, 1, 2, 99.98, 'TKT-2026-AI7821', 'CONFIRMED'),
(2, 3, 1, 15.00, 'TKT-2026-WS4412', 'CONFIRMED'),
(3, 2, 3, 87.00, 'TKT-2026-JZ9034', 'CONFIRMED'),
(4, 5, 1, 10.00, 'TKT-2026-SP1198', 'CONFIRMED');

-- 4. Insert Initial Feedbacks
INSERT INTO feedbacks (user_id, event_id, rating, comment) VALUES
(2, 1, 5, 'Outstanding lineup of speakers and state-of-the-art keynote topics! Highly recommended.'),
(3, 2, 4, 'Great musical atmosphere and very well-organized seating arrangement.'),
(4, 3, 5, 'The hands-on coding exercises were extremely insightful and practical.');
