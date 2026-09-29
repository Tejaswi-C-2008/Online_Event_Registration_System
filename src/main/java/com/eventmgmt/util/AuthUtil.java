package com.eventmgmt.util;

import com.eventmgmt.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Session Authorization Utility for checking authentication and access roles.
 */
public final class AuthUtil {

    private AuthUtil() {}

    /**
     * Retrieves the authenticated user from the HTTP Session.
     */
    public static User getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userObj = session.getAttribute("user");
        if (userObj instanceof User) {
            return (User) userObj;
        }
        return null;
    }

    /**
     * Checks if the session has an active logged-in user.
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getUser(request) != null;
    }

    /**
     * Checks if the active user possesses the ADMIN role.
     */
    public static boolean isAdmin(HttpServletRequest request) {
        User user = getUser(request);
        return user != null && user.isAdmin();
    }

    /**
     * Ensures the request has a valid logged in user, otherwise responds with HTTP 401 Unauthorized.
     */
    public static User requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = getUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Authentication required. Please login first.\"}");
            return null;
        }
        return user;
    }

    /**
     * Ensures the request is from an Administrator, otherwise responds with HTTP 403 Forbidden.
     */
    public static User requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = requireUser(request, response);
        if (user == null) {
            return null;
        }
        if (!user.isAdmin()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Access denied. Administrator privileges required.\"}");
            return null;
        }
        return user;
    }
}
