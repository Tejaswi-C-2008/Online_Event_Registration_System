package com.eventmgmt.servlet;

import com.eventmgmt.dao.EventDAO;
import com.eventmgmt.dao.FeedbackDAO;
import com.eventmgmt.model.Event;
import com.eventmgmt.model.Feedback;
import com.eventmgmt.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/events/*")
public class EventServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final EventDAO eventDAO = new EventDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        // 1. Fetch single event details: /api/events/123
        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                int eventId = Integer.parseInt(pathInfo.substring(1));
                Event event = eventDAO.getEventById(eventId);
                if (event != null) {
                    List<Feedback> feedbacks = feedbackDAO.getFeedbacksByEvent(eventId);
                    double avgRating = feedbackDAO.getAverageRating(eventId);

                    Map<String, Object> detailMap = new HashMap<>();
                    detailMap.put("event", event);
                    detailMap.put("feedbacks", feedbacks);
                    detailMap.put("averageRating", avgRating);

                    JsonUtil.sendSuccess(response, detailMap);
                } else {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Event not found.");
                }
                return;
            } catch (NumberFormatException e) {
                // If path is a special keyword like /categories or /stats
                if ("/categories".equalsIgnoreCase(pathInfo)) {
                    List<String> categories = eventDAO.getCategories();
                    JsonUtil.sendSuccess(response, categories);
                    return;
                } else if ("/stats".equalsIgnoreCase(pathInfo)) {
                    Map<String, Object> stats = eventDAO.getStats();
                    JsonUtil.sendSuccess(response, stats);
                    return;
                }
            }
        }

        // 2. Fetch event list with optional filters
        String category = request.getParameter("category");
        String query = request.getParameter("q");
        String status = request.getParameter("status");
        String sortBy = request.getParameter("sortBy");
        String limitParam = request.getParameter("limit");

        List<Event> events;
        if (limitParam != null) {
            try {
                int limit = Integer.parseInt(limitParam);
                events = eventDAO.getUpcomingEvents(limit);
            } catch (NumberFormatException e) {
                events = eventDAO.getAllEvents();
            }
        } else {
            events = eventDAO.filterEvents(category, query, status, sortBy);
        }

        JsonUtil.sendSuccess(response, events);
    }
}
