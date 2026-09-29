package com.eventmgmt.servlet;

import com.eventmgmt.dao.EventDAO;
import com.eventmgmt.model.Event;
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
import java.sql.Date;

@WebServlet("/api/admin/events/*")
public class AdminEventServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final EventDAO eventDAO = new EventDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.requireAdmin(request, response);
        if (admin == null) return;

        try {
            JsonObject json = JsonUtil.fromReader(request.getReader(), JsonObject.class);
            if (json == null || !json.has("title") || !json.has("category") || !json.has("eventDate") || !json.has("venue")) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required event properties.");
                return;
            }

            Event event = new Event();
            event.setTitle(json.get("title").getAsString().trim());
            event.setDescription(json.has("description") ? json.get("description").getAsString().trim() : "");
            event.setCategory(json.get("category").getAsString().trim());
            event.setEventDate(Date.valueOf(json.get("eventDate").getAsString().trim()));
            event.setEventTime(json.has("eventTime") ? json.get("eventTime").getAsString().trim() : "10:00 AM");
            event.setVenue(json.get("venue").getAsString().trim());
            event.setOrganizer(json.has("organizer") ? json.get("organizer").getAsString().trim() : "Event Management");
            event.setCapacity(json.has("capacity") ? json.get("capacity").getAsInt() : 100);
            event.setRegisteredCount(0);
            event.setPrice(json.has("price") ? new BigDecimal(json.get("price").getAsString()) : BigDecimal.ZERO);
            event.setBannerUrl(json.has("bannerUrl") ? json.get("bannerUrl").getAsString().trim() : "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800");
            event.setStatus(json.has("status") ? json.get("status").getAsString().trim().toUpperCase() : "UPCOMING");

            boolean success = eventDAO.createEvent(event);
            if (success) {
                JsonUtil.sendSuccess(response, "Event created successfully.", event);
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create event in database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.requireAdmin(request, response);
        if (admin == null) return;

        try {
            JsonObject json = JsonUtil.fromReader(request.getReader(), JsonObject.class);
            if (json == null || !json.has("id")) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Event ID is required for update.");
                return;
            }

            int id = json.get("id").getAsInt();
            Event event = eventDAO.getEventById(id);
            if (event == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Event not found.");
                return;
            }

            if (json.has("title")) event.setTitle(json.get("title").getAsString().trim());
            if (json.has("description")) event.setDescription(json.get("description").getAsString().trim());
            if (json.has("category")) event.setCategory(json.get("category").getAsString().trim());
            if (json.has("eventDate")) event.setEventDate(Date.valueOf(json.get("eventDate").getAsString().trim()));
            if (json.has("eventTime")) event.setEventTime(json.get("eventTime").getAsString().trim());
            if (json.has("venue")) event.setVenue(json.get("venue").getAsString().trim());
            if (json.has("organizer")) event.setOrganizer(json.get("organizer").getAsString().trim());
            if (json.has("capacity")) event.setCapacity(json.get("capacity").getAsInt());
            if (json.has("price")) event.setPrice(new BigDecimal(json.get("price").getAsString()));
            if (json.has("bannerUrl")) event.setBannerUrl(json.get("bannerUrl").getAsString().trim());
            if (json.has("status")) event.setStatus(json.get("status").getAsString().trim().toUpperCase());

            boolean success = eventDAO.updateEvent(event);
            if (success) {
                JsonUtil.sendSuccess(response, "Event updated successfully.", event);
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update event.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.requireAdmin(request, response);
        if (admin == null) return;

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Event ID required in URL path.");
            return;
        }

        try {
            int eventId = Integer.parseInt(pathInfo.substring(1));
            boolean success = eventDAO.deleteEvent(eventId);
            if (success) {
                JsonUtil.sendSuccess(response, "Event deleted successfully.", null);
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete event.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Event ID format.");
        }
    }
}
