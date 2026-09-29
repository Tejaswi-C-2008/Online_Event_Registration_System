package com.eventmgmt.servlet;

import com.eventmgmt.model.User;
import com.eventmgmt.util.AuthUtil;
import com.eventmgmt.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/status")
public class AuthStatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.getUser(request);
        Map<String, Object> data = new HashMap<>();

        if (user != null) {
            data.put("authenticated", true);
            data.put("user", new User(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null,
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
            ));
        } else {
            data.put("authenticated", false);
            data.put("user", null);
        }

        JsonUtil.sendSuccess(response, data);
    }
}
