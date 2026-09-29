package com.eventmgmt.servlet;

import com.eventmgmt.dao.RegistrationDAO;
import com.eventmgmt.model.User;
import com.eventmgmt.util.AuthUtil;
import com.eventmgmt.util.JsonUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/registrations/cancel")
public class CancelRegistrationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.requireUser(request, response);
        if (user == null) return;

        try {
            JsonObject json = JsonUtil.fromReader(request.getReader(), JsonObject.class);
            if (json == null || !json.has("registrationId")) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing registrationId.");
                return;
            }

            int registrationId = json.get("registrationId").getAsInt();
            boolean success = registrationDAO.cancelRegistration(registrationId, user.getId(), user.isAdmin());

            if (success) {
                JsonUtil.sendSuccess(response, "Registration has been successfully cancelled.", null);
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Unable to cancel registration. It may already be cancelled or unauthorized.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
}
