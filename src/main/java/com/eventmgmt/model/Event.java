package com.eventmgmt.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model class representing an Event.
 */
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String description;
    private String category; // "Technology", "Music", "Workshop", "Business", "Sports", "Cultural"
    private Date eventDate;
    private String eventTime;
    private String venue;
    private String organizer;
    private int capacity;
    private int registeredCount;
    private BigDecimal price;
    private String bannerUrl;
    private String status; // "UPCOMING", "ONGOING", "COMPLETED", "CANCELLED"
    private Timestamp createdAt;

    public Event() {}

    public Event(int id, String title, String description, String category, Date eventDate,
                 String eventTime, String venue, String organizer, int capacity,
                 int registeredCount, BigDecimal price, String bannerUrl, String status, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.venue = venue;
        this.organizer = organizer;
        this.capacity = capacity;
        this.registeredCount = registeredCount;
        this.price = price;
        this.bannerUrl = bannerUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Computed properties
    public int getAvailableSeats() {
        return Math.max(0, capacity - registeredCount);
    }

    public boolean isSoldOut() {
        return getAvailableSeats() <= 0;
    }

    public double getOccupancyRate() {
        if (capacity == 0) return 0.0;
        return ((double) registeredCount / capacity) * 100.0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }

    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
