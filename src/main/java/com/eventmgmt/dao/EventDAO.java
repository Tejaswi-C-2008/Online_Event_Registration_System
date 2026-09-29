package com.eventmgmt.dao;

import com.eventmgmt.DBConnection;
import com.eventmgmt.model.Event;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Event entities.
 */
public class EventDAO {

    /**
     * Retrieves all events ordered by event date.
     */
    public List<Event> getAllEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY event_date ASC, event_time ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[EventDAO.getAllEvents] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves upcoming featured events with a limit.
     */
    public List<Event> getUpcomingEvents(int limit) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE status != 'CANCELLED' ORDER BY event_date ASC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventDAO.getUpcomingEvents] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves a single event by ID.
     */
    public Event getEventById(int id) {
        String sql = "SELECT * FROM events WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventDAO.getEventById] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Filters events by category, search keywords, status, and sorting options.
     */
    public List<Event> filterEvents(String category, String query, String status, String sortBy) {
        List<Event> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM events WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category.trim())) {
            sql.append(" AND LOWER(category) = ?");
            params.add(category.trim().toLowerCase());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append(" AND LOWER(status) = ?");
            params.add(status.trim().toLowerCase());
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ? OR LOWER(venue) LIKE ? OR LOWER(organizer) LIKE ?)");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        // Sorting
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY price ASC");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY price DESC");
        } else if ("popular".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY registered_count DESC");
        } else {
            sql.append(" ORDER BY event_date ASC, event_time ASC");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[EventDAO.filterEvents] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Creates a new event record.
     */
    public boolean createEvent(Event event) {
        String sql = "INSERT INTO events (title, description, category, event_date, event_time, venue, organizer, capacity, registered_count, price, banner_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getCategory());
            ps.setDate(4, event.getEventDate());
            ps.setString(5, event.getEventTime());
            ps.setString(6, event.getVenue());
            ps.setString(7, event.getOrganizer());
            ps.setInt(8, event.getCapacity());
            ps.setInt(9, event.getRegisteredCount());
            ps.setBigDecimal(10, event.getPrice());
            ps.setString(11, event.getBannerUrl());
            ps.setString(12, event.getStatus() != null ? event.getStatus() : "UPCOMING");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        event.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[EventDAO.createEvent] Error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates an existing event.
     */
    public boolean updateEvent(Event event) {
        String sql = "UPDATE events SET title = ?, description = ?, category = ?, event_date = ?, event_time = ?, venue = ?, organizer = ?, capacity = ?, price = ?, banner_url = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getCategory());
            ps.setDate(4, event.getEventDate());
            ps.setString(5, event.getEventTime());
            ps.setString(6, event.getVenue());
            ps.setString(7, event.getOrganizer());
            ps.setInt(8, event.getCapacity());
            ps.setBigDecimal(9, event.getPrice());
            ps.setString(10, event.getBannerUrl());
            ps.setString(11, event.getStatus());
            ps.setInt(12, event.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EventDAO.updateEvent] Error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes an event by ID.
     */
    public boolean deleteEvent(int id) {
        String sql = "DELETE FROM events WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EventDAO.deleteEvent] Error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Atomically adjusts the registered count of an event.
     */
    public boolean updateRegisteredCount(int eventId, int delta) {
        String sql = "UPDATE events SET registered_count = registered_count + ? WHERE id = ? AND (registered_count + ?) <= capacity AND (registered_count + ?) >= 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, eventId);
            ps.setInt(3, delta);
            ps.setInt(4, delta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[EventDAO.updateRegisteredCount] Error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns list of distinct categories.
     */
    public List<String> getCategories() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM events WHERE category IS NOT NULL ORDER BY category ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("[EventDAO.getCategories] Error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Aggregates event statistics.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) as total_events, " +
                     "SUM(capacity) as total_capacity, " +
                     "SUM(registered_count) as total_registered, " +
                     "COUNT(CASE WHEN status = 'UPCOMING' THEN 1 END) as upcoming_events " +
                     "FROM events";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                stats.put("totalEvents", rs.getInt("total_events"));
                stats.put("totalCapacity", rs.getInt("total_capacity"));
                stats.put("totalRegistered", rs.getInt("total_registered"));
                stats.put("upcomingEvents", rs.getInt("upcoming_events"));
            }
        } catch (SQLException e) {
            System.err.println("[EventDAO.getStats] Error: " + e.getMessage());
        }
        return stats;
    }

    private Event mapRow(ResultSet rs) throws SQLException {
        return new Event(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("category"),
            rs.getDate("event_date"),
            rs.getString("event_time"),
            rs.getString("venue"),
            rs.getString("organizer"),
            rs.getInt("capacity"),
            rs.getInt("registered_count"),
            rs.getBigDecimal("price"),
            rs.getString("banner_url"),
            rs.getString("status"),
            rs.getTimestamp("created_at")
        );
    }
}
