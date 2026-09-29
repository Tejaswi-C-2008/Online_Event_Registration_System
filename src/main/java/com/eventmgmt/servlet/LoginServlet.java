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

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JsonObject reqJson = JsonUtil.fromReader(request.getReader(), JsonObject.class);

            if (reqJson == null || !reqJson.has("identifier") || !reqJson.has("password")) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Username/Email and Password are required.");
                return;
            }

            String identifier = reqJson.get("identifier").getAsString().trim();
            String password = reqJson.get("password").getAsString().trim();

            User user = userDAO.login(identifier, password);

            if (user != null) {
                // Invalidate old session and create fresh session
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("role", user.getRole());

                user.setPassword(null); // Mask password
                JsonUtil.sendSuccess(response, "Login successful!", user);
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username/email or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error: " + e.getMessage());
        }
    }
}
