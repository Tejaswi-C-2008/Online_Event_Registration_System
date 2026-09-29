package com.eventmgmt.servlet;

import com.eventmgmt.dao.FeedbackDAO;
import com.eventmgmt.model.Feedback;
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
import java.util.List;

@WebServlet("/api/feedback")
public class FeedbackServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String eventIdParam = request.getParameter("eventId");
        if (eventIdParam == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "eventId query parameter is required.");
            return;
        }

        try {
            int eventId = Integer.parseInt(eventIdParam);
            List<Feedback> feedbacks = feedbackDAO.getFeedbacksByEvent(eventId);
            JsonUtil.sendSuccess(response, feedbacks);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid eventId parameter.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = AuthUtil.requireUser(request, response);
        if (user == null) return;

        try {
            JsonObject json = JsonUtil.fromReader(request.getReader(), JsonObject.class);
            if (json == null || !json.has("eventId") || !json.has("rating")) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "eventId and rating are required.");
                return;
            }

            int eventId = json.get("eventId").getAsInt();
            int rating = json.get("rating").getAsInt();
            String comment = json.has("comment") ? json.get("comment").getAsString().trim() : "";

            if (rating < 1 || rating > 5) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Rating must be between 1 and 5 stars.");
                return;
            }

            Feedback fb = new Feedback();
            fb.setUserId(user.getId());
            fb.setEventId(eventId);
            fb.setRating(rating);
            fb.setComment(comment);

            boolean success = feedbackDAO.addFeedback(fb);
            if (success) {
                JsonUtil.sendSuccess(response, "Thank you for your review!", fb);
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to submit feedback.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
