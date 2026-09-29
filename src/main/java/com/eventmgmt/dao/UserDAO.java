package com.eventmgmt.dao;

import com.eventmgmt.DBConnection;
import com.eventmgmt.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entities.
 */
public class UserDAO {

    /**
     * Registers a new user account.
     */
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, email, password, full_name, phone, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername().trim());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getFullName().trim());
            ps.setString(5, user.getPhone() != null ? user.getPhone().trim() : "");
            ps.setString(6, user.getRole() != null ? user.getRole() : "USER");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.register] Error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Authenticates a user by username or email and password.
     */
    public User login(String identifier, String password) {
        String sql = "SELECT * FROM users WHERE (LOWER(username) = ? OR LOWER(email) = ?) AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String cleanId = identifier.trim().toLowerCase();
            ps.setString(1, cleanId);
            ps.setString(2, cleanId);
            ps.setString(3, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.login] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a user by primary key ID.
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.findById] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a user by email address.
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.findByEmail] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a user by username.
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.findByUsername] Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all registered users in the system.
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO.getAllUsers] Error: " + e.getMessage());
        }
        return list;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("full_name"),
            rs.getString("phone"),
            rs.getString("role"),
            rs.getTimestamp("created_at")
        );
    }
}
