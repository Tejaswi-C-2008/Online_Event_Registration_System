package com.eventmgmt.dao;

import com.eventmgmt.DBConnection;
import com.eventmgmt.model.Registration;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data Access Object for Registrations & Ticket Bookings.
 */
public class RegistrationDAO {

    /**
     * Books tickets for an event within a single ACID transaction.
     */
    public Registration bookTickets(int userId, int eventId, int ticketsCount, BigDecimal pricePerTicket) {
        String checkCapacitySql = "SELECT capacity, registered_count, price, title FROM events WHERE id = ? FOR UPDATE";
        String insertRegSql = "INSERT INTO registrations (user_id, event_id, tickets_count, total_price, ticket_code, status) VALUES (?, ?, ?, ?, ?, ?)";
        String updateEventSql = "UPDATE events SET registered_count = registered_count + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Check availability
            int capacity = 0;
            int registered = 0;
            BigDecimal eventPrice = BigDecimal.ZERO;

            try (PreparedStatement psCheck = conn.prepareStatement(checkCapacitySql)) {
                psCheck.setInt(1, eventId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return null; // Event not found
                    }
                    capacity = rs.getInt("capacity");
                    registered = rs.getInt("registered_count");
                    eventPrice = rs.getBigDecimal("price");
                }
            }

            int available = capacity - registered;
            if (available < ticketsCount) {
                conn.rollback();
                return null; // Insufficient capacity
            }

            // 2. Generate unique Ticket Code
            String ticketCode = "TKT-" + (1000 + (int)(Math.random() * 9000)) + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            BigDecimal totalPrice = (pricePerTicket != null && pricePerTicket.compareTo(BigDecimal.ZERO) > 0)
                    ? pricePerTicket.multiply(new BigDecimal(ticketsCount))
                    : eventPrice.multiply(new BigDecimal(ticketsCount));

            // 3. Insert Registration Record
            int registrationId = 0;
            try (PreparedStatement psInsert = conn.prepareStatement(insertRegSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, userId);
                psInsert.setInt(2, eventId);
                psInsert.setInt(3, ticketsCount);
                psInsert.setBigDecimal(4, totalPrice);
                psInsert.setString(5, ticketCode);
                psInsert.setString(6, "CONFIRMED");
                psInsert.executeUpdate();

                try (ResultSet rsKeys = psInsert.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        registrationId = rsKeys.getInt(1);
                    }
                }
            }

            // 4. Update Event Registered Count
            try (PreparedStatement psUpdate = conn.prepareStatement(updateEventSql)) {
                psUpdate.setInt(1, ticketsCount);
                psUpdate.setInt(2, eventId);
                psUpdate.executeUpdate();
            }

            // Commit Transaction
            conn.commit();

            return getRegistrationById(registrationId);

        } catch (SQLException e) {
            System.err.println("[RegistrationDAO.bookTickets] Transaction failed: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Cancels an existing registration and updates event seat availability.
     */
    public boolean cancelRegistration(int registrationId, int userId, boolean isAdmin) {
        String findRegSql = "SELECT * FROM registrations WHERE id = ? AND status = 'CONFIRMED'";
        String updateRegSql = "UPDATE registrations SET status = 'CANCELLED' WHERE id = ?";
        String updateEventSql = "UPDATE events SET registered_count = GREATEST(0, registered_count - ?) WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int eventId = 0;
            int ticketsCount = 0;
            int regUserId = 0;

            try (PreparedStatement psFind = conn.prepareStatement(findRegSql)) {
                psFind.setInt(1, registrationId);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    regUserId = rs.getInt("user_id");
                    eventId = rs.getInt("event_id");
                    ticketsCount = rs.getInt("tickets_count");
                }
            }

            // Permission check: only booking owner or admin can cancel
            if (!isAdmin && regUserId != userId) {
                conn.rollback();
                return false;
            }

            // Update registration status
            try (PreparedStatement psUpdateReg = conn.prepareStatement(updateRegSql)) {
                psUpdateReg.setInt(1, registrationId);
                psUpdateReg.executeUpdate();
            }

            // Decrement registered count in event
            try (PreparedStatement psUpdateEvent = conn.prepareStatement(updateEventSql)) {
                psUpdateEvent.setInt(1, ticketsCount);
                psUpdateEvent.setInt(2, eventId);
                psUpdateEvent.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[RegistrationDAO.cancelRegistration] Error: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
            }
        }
        return false;
    }

    /**
     * Gets registrations made by a specific user.
     */
    public List<Registration> getRegistrationsByUser(int userId) {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as user_name, u.email as user_email, " +
                     "e.title as event_title, e.category as event_category, " +
                     "e.event_date, e.event_time, e.venue as event_venue " +
                     "FROM registrations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN events e ON r.event_id = e.id " +
                     "WHERE r.user_id = ? " +
                     "ORDER BY r.registration_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[RegistrationDAO.getRegistrationsByUser] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Gets all registrations in system for administrator view.
     */
    public List<Registration> getAllRegistrations() {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as user_name, u.email as user_email, " +
                     "e.title as event_title, e.category as event_category, " +
                     "e.event_date, e.event_time, e.venue as event_venue " +
                     "FROM registrations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN events e ON r.event_id = e.id " +
                     "ORDER BY r.registration_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[RegistrationDAO.getAllRegistrations] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Finds a registration by Ticket Code (useful for QR verification).
     */
    public Registration getRegistrationByCode(String ticketCode) {
        String sql = "SELECT r.*, u.full_name as user_name, u.email as user_email, " +
                     "e.title as event_title, e.category as event_category, " +
                     "e.event_date, e.event_time, e.venue as event_venue " +
                     "FROM registrations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN events e ON r.event_id = e.id " +
                     "WHERE UPPER(r.ticket_code) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketCode.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[RegistrationDAO.getRegistrationByCode] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a registration by Primary Key ID.
     */
    public Registration getRegistrationById(int id) {
        String sql = "SELECT r.*, u.full_name as user_name, u.email as user_email, " +
                     "e.title as event_title, e.category as event_category, " +
                     "e.event_date, e.event_time, e.venue as event_venue " +
                     "FROM registrations r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN events e ON r.event_id = e.id " +
                     "WHERE r.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[RegistrationDAO.getRegistrationById] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if a user is already registered for an event.
     */
    public boolean isUserRegistered(int userId, int eventId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE user_id = ? AND event_id = ? AND status = 'CONFIRMED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[RegistrationDAO.isUserRegistered] Error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Aggregates key metrics for the Admin Dashboard.
     */
    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> map = new HashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM users WHERE role = 'USER') as total_users, " +
                     "(SELECT COUNT(*) FROM events) as total_events, " +
                     "(SELECT COUNT(*) FROM registrations WHERE status = 'CONFIRMED') as active_registrations, " +
                     "(SELECT COALESCE(SUM(total_price), 0) FROM registrations WHERE status = 'CONFIRMED') as total_revenue";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                map.put("totalUsers", rs.getInt("total_users"));
                map.put("totalEvents", rs.getInt("total_events"));
                map.put("activeRegistrations", rs.getInt("active_registrations"));
                map.put("totalRevenue", rs.getBigDecimal("total_revenue"));
            }
        } catch (SQLException e) {
            System.err.println("[RegistrationDAO.getDashboardMetrics] Error: " + e.getMessage());
        }
        return map;
    }

    /**
     * Aggregates chart analytics data for Chart.js.
     */
    public Map<String, Object> getAnalyticsData() {
        Map<String, Object> analytics = new HashMap<>();

        // 1. Registrations & Revenue by Category
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        String catSql = "SELECT e.category, COUNT(r.id) as reg_count, COALESCE(SUM(r.total_price), 0) as cat_revenue " +
                        "FROM events e " +
                        "LEFT JOIN registrations r ON e.id = r.event_id AND r.status = 'CONFIRMED' " +
                        "GROUP BY e.category ORDER BY reg_count DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(catSql)) {
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("category", rs.getString("category"));
                item.put("registrations", rs.getInt("reg_count"));
                item.put("revenue", rs.getDouble("cat_revenue"));
                categoryStats.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        analytics.put("categoryStats", categoryStats);

        // 2. Top Booked Events
        List<Map<String, Object>> topEvents = new ArrayList<>();
        String topEventSql = "SELECT title, capacity, registered_count, " +
                             "ROUND((registered_count * 100.0 / NULLIF(capacity, 0)), 1) as occupancy_rate " +
                             "FROM events ORDER BY registered_count DESC LIMIT 5";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(topEventSql)) {
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("title", rs.getString("title"));
                item.put("capacity", rs.getInt("capacity"));
                item.put("registered", rs.getInt("registered_count"));
                item.put("occupancyRate", rs.getDouble("occupancy_rate"));
                topEvents.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        analytics.put("topEvents", topEvents);

        return analytics;
    }

    private Registration mapRow(ResultSet rs) throws SQLException {
        Registration reg = new Registration(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("event_id"),
            rs.getInt("tickets_count"),
            rs.getBigDecimal("total_price"),
            rs.getString("ticket_code"),
            rs.getString("status"),
            rs.getTimestamp("registration_date")
        );

        // Optional mapped fields
        try { reg.setUserName(rs.getString("user_name")); } catch (SQLException ignored) {}
        try { reg.setUserEmail(rs.getString("user_email")); } catch (SQLException ignored) {}
        try { reg.setEventTitle(rs.getString("event_title")); } catch (SQLException ignored) {}
        try { reg.setEventCategory(rs.getString("event_category")); } catch (SQLException ignored) {}
        try { reg.setEventDate(rs.getDate("event_date")); } catch (SQLException ignored) {}
        try { reg.setEventTime(rs.getString("event_time")); } catch (SQLException ignored) {}
        try { reg.setEventVenue(rs.getString("event_venue")); } catch (SQLException ignored) {}

        return reg;
    }
}
