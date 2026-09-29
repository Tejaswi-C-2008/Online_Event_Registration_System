package com.eventmgmt.servlet;

import com.eventmgmt.dao.RegistrationDAO;
import com.eventmgmt.model.Registration;
import com.eventmgmt.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/ticket/verify")
public class TicketVerifyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        if (code == null || code.trim().isEmpty()) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Ticket code query parameter is required.");
            return;
        }

        Registration reg = registrationDAO.getRegistrationByCode(code.trim());
        if (reg != null) {
            JsonUtil.sendSuccess(response, "Ticket is valid.", reg);
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Invalid ticket code or record not found.");
        }
    }
}
