package com.eventmgmt.servlet;

import com.eventmgmt.dao.RegistrationDAO;
import com.eventmgmt.model.Registration;
import com.eventmgmt.model.User;
import com.eventmgmt.util.AuthUtil;
import com.eventmgmt.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/registrations/my")
public class UserRegistrationsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.requireUser(request, response);
        if (user == null) return;

        List<Registration> list = registrationDAO.getRegistrationsByUser(user.getId());
        JsonUtil.sendSuccess(response, list);
    }
}
