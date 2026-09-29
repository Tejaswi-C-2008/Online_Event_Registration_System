package com.eventmgmt.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model class representing user feedback & rating for an event.
 */
public class Feedback implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private int eventId;
    private int rating; // 1 to 5
    private String comment;
    private Timestamp createdAt;

    // Joined fields
    private String userFullName;
    private String eventTitle;

    public Feedback() {}

    public Feedback(int id, int userId, int eventId, int rating, String comment, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
}
