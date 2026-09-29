package com.eventmgmt.servlet;

import com.eventmgmt.dao.EventDAO;
import com.eventmgmt.dao.RegistrationDAO;
import com.eventmgmt.model.Event;
import com.eventmgmt.model.Registration;
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
import java.math.BigDecimal;

@WebServlet("/api/registrations/book")
public class BookTicketServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final EventDAO eventDAO = new EventDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.requireUser(request, response);
        if (user == null) return;

        try {
            JsonObject json = JsonUtil.fromReader(request.getReader(), JsonObject.class);
            if (json == null || !json.has("eventId") || !json.has("ticketsCount")) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing eventId or ticketsCount.");
                return;
            }

            int eventId = json.get("eventId").getAsInt();
            int ticketsCount = json.get("ticketsCount").getAsInt();

            if (ticketsCount <= 0 || ticketsCount > 10) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid tickets count (min 1, max 10 per booking).");
                return;
            }

            // Check if user is already registered? (Optional business rule to restrict 1 booking per event per user)
            // if (registrationDAO.isUserRegistered(user.getId(), eventId)) {
            //     JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "You are already registered for this event.");
            //     return;
            // }

            Event event = eventDAO.getEventById(eventId);
            if (event == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Event not found.");
                return;
            }

            if (!"UPCOMING".equalsIgnoreCase(event.getStatus()) && !"ONGOING".equalsIgnoreCase(event.getStatus())) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "This event is no longer accepting registrations.");
                return;
            }

            // Transactional booking
            Registration reg = registrationDAO.bookTickets(user.getId(), eventId, ticketsCount, event.getPrice());

            if (reg != null) {
                JsonUtil.sendSuccess(response, "Tickets booked successfully! Your pass is ready.", reg);
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "Booking failed. The event might be sold out or unavailable.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error computing booking: " + e.getMessage());
        }
    }
}
