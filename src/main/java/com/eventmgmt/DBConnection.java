package com.eventmgmt;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Connection Manager with Dual Support:
 * 1. Automatic Fallback to Embedded H2 Database (zero-config, runs anywhere without MySQL).
 * 2. MySQL 8.0 support (if configured or available).
 *
 * Automatically creates required tables and seeds starter demo data on first startup.
 */
public class DBConnection {

    // MySQL Configuration
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/event_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "";

    // H2 Embedded Configuration
    private static final String H2_URL = "jdbc:h2:./data/event_db;AUTO_SERVER=TRUE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDER=HIGH";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    private static boolean useH2 = true;
    private static boolean initialized = false;

    static {
        // Ensure data directory exists for embedded H2 database
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        initDatabase();
    }

    /**
     * Initializes database tables and starter seed records.
     */
    public static synchronized void initDatabase() {
        if (initialized) return;

        // Try MySQL first; if unavailable, seamlessly fall back to H2
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD)) {
                useH2 = false;
                System.out.println("[DBConnection] Connected to MySQL successfully.");
                createTables(conn);
                seedData(conn);
                initialized = true;
                return;
            } catch (SQLException e) {
                System.out.println("[DBConnection] MySQL not reachable (" + e.getMessage() + "). Switching to Embedded H2 database...");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[DBConnection] MySQL driver not found. Using Embedded H2 database...");
        }

        // Fallback to Embedded H2
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD)) {
                useH2 = true;
                System.out.println("[DBConnection] Embedded H2 Database initialized at ./data/event_db.");
                createTables(conn);
                seedData(conn);
                initialized = true;
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Critical error initializing H2 database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtains an active JDBC Connection.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initDatabase();
        }
        if (useH2) {
            return DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
        } else {
            return DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
        }
    }

    /**
     * Creates all necessary tables if they do not exist.
     */
    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Users table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  username VARCHAR(50) NOT NULL UNIQUE," +
                "  email VARCHAR(100) NOT NULL UNIQUE," +
                "  password VARCHAR(255) NOT NULL," +
                "  full_name VARCHAR(100) NOT NULL," +
                "  phone VARCHAR(20)," +
                "  role VARCHAR(20) DEFAULT 'USER'," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Events table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS events (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  title VARCHAR(150) NOT NULL," +
                "  description TEXT NOT NULL," +
                "  category VARCHAR(50) NOT NULL," +
                "  event_date DATE NOT NULL," +
                "  event_time VARCHAR(20) NOT NULL," +
                "  venue VARCHAR(150) NOT NULL," +
                "  organizer VARCHAR(100) NOT NULL," +
                "  capacity INT NOT NULL DEFAULT 100," +
                "  registered_count INT NOT NULL DEFAULT 0," +
                "  price DECIMAL(10, 2) NOT NULL DEFAULT 0.00," +
                "  banner_url VARCHAR(255)," +
                "  status VARCHAR(20) DEFAULT 'UPCOMING'," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // Registrations table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS registrations (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  event_id INT NOT NULL," +
                "  tickets_count INT NOT NULL DEFAULT 1," +
                "  total_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00," +
                "  ticket_code VARCHAR(50) NOT NULL UNIQUE," +
                "  status VARCHAR(20) DEFAULT 'CONFIRMED'," +
                "  registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE" +
                ")"
            );

            // Feedbacks table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS feedbacks (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  event_id INT NOT NULL," +
                "  rating INT NOT NULL," +
                "  comment TEXT," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "  FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE" +
                ")"
            );
        }
    }

    /**
     * Seeds initial records into database if empty.
     */
    private static void seedData(Connection conn) {
        try {
            // Seed Users
            try (Statement checkStmt = conn.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO users (username, email, password, full_name, phone, role) VALUES (?, ?, ?, ?, ?, ?)")) {
                        // Admin
                        ps.setString(1, "admin");
                        ps.setString(2, "admin@events.local");
                        ps.setString(3, "admin123");
                        ps.setString(4, "System Administrator");
                        ps.setString(5, "+1-555-0100");
                        ps.setString(6, "ADMIN");
                        ps.executeUpdate();

                        // Regular User 1
                        ps.setString(1, "john_doe");
                        ps.setString(2, "john@example.com");
                        ps.setString(3, "user123");
                        ps.setString(4, "John Doe");
                        ps.setString(5, "+1-555-0101");
                        ps.setString(6, "USER");
                        ps.executeUpdate();

                        // Regular User 2
                        ps.setString(1, "sarah_connor");
                        ps.setString(2, "sarah@example.com");
                        ps.setString(3, "user123");
                        ps.setString(4, "Sarah Connor");
                        ps.setString(5, "+1-555-0102");
                        ps.setString(6, "USER");
                        ps.executeUpdate();

                        // Regular User 3
                        ps.setString(1, "alex_kumar");
                        ps.setString(2, "alex@example.com");
                        ps.setString(3, "user123");
                        ps.setString(4, "Alex Kumar");
                        ps.setString(5, "+1-555-0103");
                        ps.setString(6, "USER");
                        ps.executeUpdate();
                    }
                }
            }

            // Seed Events
            try (Statement checkStmt = conn.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM events")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertEventSql = "INSERT INTO events (title, description, category, event_date, event_time, venue, organizer, capacity, registered_count, price, banner_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertEventSql)) {
                        // Event 1
                        ps.setString(1, "Global AI & Cloud Summit 2026");
                        ps.setString(2, "Explore the bleeding edge of Artificial Intelligence, Cloud Computing, and Neural Architectures with world-class tech leaders.");
                        ps.setString(3, "Technology");
                        ps.setDate(4, java.sql.Date.valueOf("2026-11-15"));
                        ps.setString(5, "09:30 AM");
                        ps.setString(6, "Tech Convention Center, Hall A");
                        ps.setString(7, "AI Innovations Lab");
                        ps.setInt(8, 250);
                        ps.setInt(9, 42);
                        ps.setBigDecimal(10, new java.math.BigDecimal("49.99"));
                        ps.setString(11, "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800");
                        ps.setString(12, "UPCOMING");
                        ps.executeUpdate();

                        // Event 2
                        ps.setString(1, "International Jazz & Indie Beats");
                        ps.setString(2, "An evening of electrifying jazz, soul, and indie acoustic performances by award-winning global artists.");
                        ps.setString(3, "Music");
                        ps.setDate(4, java.sql.Date.valueOf("2026-10-25"));
                        ps.setString(5, "06:00 PM");
                        ps.setString(6, "Grand Symphony Amphitheatre");
                        ps.setString(7, "Harmony Productions");
                        ps.setInt(8, 400);
                        ps.setInt(9, 185);
                        ps.setBigDecimal(10, new java.math.BigDecimal("29.00"));
                        ps.setString(11, "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800");
                        ps.setString(12, "UPCOMING");
                        ps.executeUpdate();

                        // Event 3
                        ps.setString(1, "Full-Stack Web3 & Microservices Workshop");
                        ps.setString(2, "Hands-on coding masterclass covering Microservices in Java, Docker deployment, and scalable system design.");
                        ps.setString(3, "Workshop");
                        ps.setDate(4, java.sql.Date.valueOf("2026-10-18"));
                        ps.setString(5, "10:00 AM");
                        ps.setString(6, "Silicon Valley Innovation Hub, Lab 3");
                        ps.setString(7, "CodeCraft Academy");
                        ps.setInt(8, 60);
                        ps.setInt(9, 48);
                        ps.setBigDecimal(10, new java.math.BigDecimal("15.00"));
                        ps.setString(11, "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800");
                        ps.setString(12, "UPCOMING");
                        ps.executeUpdate();

                        // Event 4
                        ps.setString(1, "Global Venture Startup Pitchfest");
                        ps.setString(2, "Connect with angel investors, VC funds, and promising tech founders showcasing disruptive innovations.");
                        ps.setString(3, "Business");
                        ps.setDate(4, java.sql.Date.valueOf("2026-11-05"));
                        ps.setString(5, "01:00 PM");
                        ps.setString(6, "Metropolitan Business Tower, 14th Floor");
                        ps.setString(7, "Venture Hub Network");
                        ps.setInt(8, 150);
                        ps.setInt(9, 95);
                        ps.setBigDecimal(10, new java.math.BigDecimal("35.00"));
                        ps.setString(11, "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800");
                        ps.setString(12, "UPCOMING");
                        ps.executeUpdate();

                        // Event 5
                        ps.setString(1, "City Marathon & Fitness Carnival 2026");
                        ps.setString(2, "Annual 10K/21K run promoting health, wellness, and youth community sports with medals and refreshments.");
                        ps.setString(3, "Sports");
                        ps.setDate(4, java.sql.Date.valueOf("2026-11-20"));
                        ps.setString(5, "06:00 AM");
                        ps.setString(6, "Central Riverside Park Boulevard");
                        ps.setString(7, "City Sports Commission");
                        ps.setInt(8, 500);
                        ps.setInt(9, 320);
                        ps.setBigDecimal(10, new java.math.BigDecimal("10.00"));
                        ps.setString(11, "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=800");
                        ps.setString(12, "UPCOMING");
                        ps.executeUpdate();

                        // Event 6
                        ps.setString(1, "Digital Arts & Immersive VR Exhibition");
                        ps.setString(2, "Interactive art installation featuring digital galleries, 3D projection mapping, and VR experiences.");
                        ps.setString(3, "Cultural");
                        ps.setDate(4, java.sql.Date.valueOf("2026-12-02"));
                        ps.setString(5, "11:00 AM");
                        ps.setString(6, "Contemporary Design Museum");
                        ps.setString(7, "Creative Minds Collective");
                        ps.setInt(8, 120);
                        ps.setInt(9, 28);
                        ps.setBigDecimal(10, new java.math.BigDecimal("20.00"));
                        ps.setString(11, "https://images.unsplash.com/photo-1508997449629-303059a039c0?w=800");
                        ps.setString(12, "UPCOMING");
                        ps.executeUpdate();
                    }
                }
            }

            // Seed Registrations
            try (Statement checkStmt = conn.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM registrations")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertRegSql = "INSERT INTO registrations (user_id, event_id, tickets_count, total_price, ticket_code, status) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertRegSql)) {
                        // Registration 1
                        ps.setInt(1, 2);
                        ps.setInt(2, 1);
                        ps.setInt(3, 2);
                        ps.setBigDecimal(4, new java.math.BigDecimal("99.98"));
                        ps.setString(5, "TKT-2026-AI7821");
                        ps.setString(6, "CONFIRMED");
                        ps.executeUpdate();

                        // Registration 2
                        ps.setInt(1, 2);
                        ps.setInt(2, 3);
                        ps.setInt(3, 1);
                        ps.setBigDecimal(4, new java.math.BigDecimal("15.00"));
                        ps.setString(5, "TKT-2026-WS4412");
                        ps.setString(6, "CONFIRMED");
                        ps.executeUpdate();

                        // Registration 3
                        ps.setInt(1, 3);
                        ps.setInt(2, 2);
                        ps.setInt(3, 3);
                        ps.setBigDecimal(4, new java.math.BigDecimal("87.00"));
                        ps.setString(5, "TKT-2026-JZ9034");
                        ps.setString(6, "CONFIRMED");
                        ps.executeUpdate();
                    }
                }
            }

            // Seed Feedbacks
            try (Statement checkStmt = conn.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM feedbacks")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertFeedbackSql = "INSERT INTO feedbacks (user_id, event_id, rating, comment) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertFeedbackSql)) {
                        ps.setInt(1, 2);
                        ps.setInt(2, 1);
                        ps.setInt(3, 5);
                        ps.setString(4, "Outstanding lineup of speakers and state-of-the-art keynote topics! Highly recommended.");
                        ps.executeUpdate();

                        ps.setInt(1, 3);
                        ps.setInt(2, 2);
                        ps.setInt(3, 4);
                        ps.setString(4, "Great musical atmosphere and very well-organized seating arrangement.");
                        ps.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[DBConnection] Error during data seeding: " + e.getMessage());
        }
    }
}
