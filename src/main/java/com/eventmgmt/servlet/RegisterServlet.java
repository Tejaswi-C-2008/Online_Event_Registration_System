package com.eventmgmt.servlet;

import com.eventmgmt.dao.UserDAO;
import com.eventmgmt.model.User;
import com.eventmgmt.util.JsonUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JsonObject reqJson = JsonUtil.fromReader(request.getReader(), JsonObject.class);

            if (reqJson == null || !reqJson.has("username") || !reqJson.has("email") || !reqJson.has("password") || !reqJson.has("fullName")) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "All required fields (username, email, password, fullName) must be provided.");
                return;
            }

            String username = reqJson.get("username").getAsString().trim();
            String email = reqJson.get("email").getAsString().trim().toLowerCase();
            String password = reqJson.get("password").getAsString().trim();
            String fullName = reqJson.get("fullName").getAsString().trim();
            String phone = reqJson.has("phone") ? reqJson.get("phone").getAsString().trim() : "";
            String role = (reqJson.has("role") && "ADMIN".equalsIgnoreCase(reqJson.get("role").getAsString())) ? "ADMIN" : "USER";

            // Validations
            if (username.length() < 3) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Username must be at least 3 characters.");
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid email address format.");
                return;
            }
            if (password.length() < 6) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters long.");
                return;
            }

            // Check if username or email is taken
            if (userDAO.findByUsername(username) != null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "Username is already registered. Please choose another.");
                return;
            }
            if (userDAO.findByEmail(email) != null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "Email is already registered. Please sign in.");
                return;
            }

            User newUser = new User(username, email, password, fullName, phone, role);
            boolean created = userDAO.register(newUser);

            if (created) {
                // Auto login on successful registration
                HttpSession session = request.getSession(true);
                session.setAttribute("user", newUser);
                session.setAttribute("userId", newUser.getId());
                session.setAttribute("role", newUser.getRole());

                // Mask password before returning
                newUser.setPassword(null);
                JsonUtil.sendSuccess(response, "Registration successful! Welcome to the Event Platform.", newUser);
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to register user. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
}
