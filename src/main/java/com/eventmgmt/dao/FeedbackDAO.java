package com.eventmgmt.dao;

import com.eventmgmt.DBConnection;
import com.eventmgmt.model.Feedback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Event Feedbacks and Reviews.
 */
public class FeedbackDAO {

    public boolean addFeedback(Feedback feedback) {
        String sql = "INSERT INTO feedbacks (user_id, event_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, feedback.getUserId());
            ps.setInt(2, feedback.getEventId());
            ps.setInt(3, feedback.getRating());
            ps.setString(4, feedback.getComment());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        feedback.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO.addFeedback] Error: " + e.getMessage());
        }
        return false;
    }

    public List<Feedback> getFeedbacksByEvent(int eventId) {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.*, u.full_name as user_full_name, e.title as event_title " +
                     "FROM feedbacks f " +
                     "JOIN users u ON f.user_id = u.id " +
                     "JOIN events e ON f.event_id = e.id " +
                     "WHERE f.event_id = ? ORDER BY f.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Feedback fb = new Feedback(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("event_id"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getTimestamp("created_at")
                    );
                    fb.setUserFullName(rs.getString("user_full_name"));
                    fb.setEventTitle(rs.getString("event_title"));
                    list.add(fb);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO.getFeedbacksByEvent] Error: " + e.getMessage());
        }
        return list;
    }

    public double getAverageRating(int eventId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM feedbacks WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO.getAverageRating] Error: " + e.getMessage());
        }
        return 0.0;
    }
}
