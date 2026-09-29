package com.eventmgmt.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model class representing a Ticket / Event Registration.
 */
public class Registration implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private int eventId;
    private int ticketsCount;
    private BigDecimal totalPrice;
    private String ticketCode;
    private String status; // "CONFIRMED", "CANCELLED"
    private Timestamp registrationDate;

    // Joined fields for display
    private String userName;
    private String userEmail;
    private String eventTitle;
    private String eventCategory;
    private Date eventDate;
    private String eventTime;
    private String eventVenue;

    public Registration() {}

    public Registration(int id, int userId, int eventId, int ticketsCount,
                        BigDecimal totalPrice, String ticketCode, String status, Timestamp registrationDate) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.ticketsCount = ticketsCount;
        this.totalPrice = totalPrice;
        this.ticketCode = ticketCode;
        this.status = status;
        this.registrationDate = registrationDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getTicketsCount() { return ticketsCount; }
    public void setTicketsCount(int ticketsCount) { this.ticketsCount = ticketsCount; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Timestamp registrationDate) { this.registrationDate = registrationDate; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getEventCategory() { return eventCategory; }
    public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }

    public Date getEventDate() { return eventDate; }
    public void setEventDate(Date eventDate) { this.eventDate = eventDate; }

    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

    public String getEventVenue() { return eventVenue; }
    public void setEventVenue(String eventVenue) { this.eventVenue = eventVenue; }
}
