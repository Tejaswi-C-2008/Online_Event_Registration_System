#  Online Event Registration System — NRD Lab Project

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-orange.svg)](https://www.oracle.com/java/)
[![Jakarta Servlets](https://img.shields.io/badge/Jakarta%20Servlets-5.0.0-blue.svg)](https://jakarta.ee/)
[![Database](https://img.shields.io/badge/Database-MySQL%20%7C%20Embedded%20H2-brightgreen.svg)](https://www.h2database.com/)
[![Frontend](https://img.shields.io/badge/Frontend-HTML5%20%7C%20CSS3%20%7C%20Bootstrap5%20%7C%20Chart.js-purple.svg)](https://getbootstrap.com/)
[![Architecture](https://img.shields.io/badge/Architecture-MVC%20%2B%20DAO-red.svg)](#architecture)

An enterprise-grade, full-stack **Online Event Registration and Management Web Application** designed and implemented as a capstone laboratory project. The system facilitates end-to-end event discovery, real-time seat reservation, digital QR ticket pass generation, administrative event CRUD management, attendee management, and visual analytics dashboards.

---

## 📑 Table of Contents

- [Overview & Key Features](#overview--key-features)
- [Lab Syllabus & Experiments Mapping](#lab-syllabus--experiments-mapping)
- [Technology Stack](#technology-stack)
- [System Architecture (MVC + DAO)](#system-architecture-mvc--dao)
- [Database Schema & ER Model](#database-schema--er-model)
- [XML Schema & Validation](#xml-schema--validation)
- [REST API Endpoints Specification](#rest-api-endpoints-specification)
- [Setup & Execution Guide](#setup--execution-guide)
- [Default Demo Credentials](#default-demo-credentials)
- [Project Directory Structure](#project-directory-structure)

---

## 🌟 Overview & Key Features

### 🎟️ Attendee / User Capabilities
* **Interactive Event Discovery**: Browse events with real-time keyword search, category filters (Technology, Music, Workshop, Business, Sports, Cultural), and sorting options (Date, Price, Popularity).
* **Detailed Event View**: View venue details, organizer information, live seat countdown, occupancy meters, and attendee reviews.
* **Instant Ticket Booking**: Select ticket quantities (1–10) with automatic total price calculation and transactional seat reservation guarantee.
* **Digital Ticket Wallet**: User dashboard displaying all active and historical bookings, complete with scannable QR pass visualization and print capability.
* **Self-Service Cancellation**: Cancel bookings anytime to automatically release seats back to the public pool.
* **Reviews & Ratings**: Submit 1 to 5-star ratings and textual feedback for completed events.

### 🛡️ Administrator / Organizer Capabilities
* **Executive KPI Dashboard**: Monitor real-time metrics (Total Events, Active Bookings, Gross Revenue, Registered Users).
* **Event Lifecycle Management**: Full CRUD operations (Create, Read, Update, Delete) on events with custom banner URLs, capacities, and pricing.
* **Attendee Management**: Real-time table of all attendee bookings across all events with instant cancellation controls.
* **Visual Data Analytics**: Chart.js data visualizations for registrations by category (Doughnut), gross revenue breakdown (Bar), and top event occupancy rates.
* **Ticket Verification Tool**: Gate scanner to authenticate attendee alphanumeric ticket codes (`TKT-XXXX-XXXXXX`).

---

## 🎓 Lab Syllabus & Experiments Mapping

This project is systematically structured to satisfy and demonstrate key web development and Java enterprise competencies:

| Experiment No. | Experiment Topic | Implementation in this Project |
|:--------------:|:-----------------|:-------------------------------|
| **Exp 1** | CSS3 Layout Techniques (Flexbox, Grid, Variables) | `src/main/webapp/css/style.css` featuring CSS variables, responsive grid, glassmorphism, and status badges. |
| **Exp 2** | Responsive Web Design using Bootstrap 5 | Mobile-first responsive UI components, navigation, modals, and forms in all `.html` pages. |
| **Exp 3** | Client-Side JavaScript Validation | Real-time email regex validation, password strength meters, and matching password checks in `register.html`. |
| **Exp 4** | Asynchronous Programming (AJAX / Fetch API) | `src/main/webapp/js/app.js` using ES6 `async/await` and Fetch API for seamless non-blocking server communication. |
| **Exp 5** | Database Connectivity with JDBC & Dual Fallback | `DBConnection.java` with automatic MySQL connection and embedded file-based H2 database fallback. |
| **Exp 6** | XML Schema Definition (DTD & XSD) and DOM Parsing | `events.xml`, `events.dtd`, `events.xsd`, and `XMLReader.java` demonstrating DOM parser validation. |
| **Exp 7** | Jakarta Servlets Request Routing & REST API | Modular servlets (`EventServlet`, `RegisterServlet`, `LoginServlet`, `AdminEventServlet`, `BookTicketServlet`). |
| **Exp 8** | Session Management & Access Control | `AuthUtil.java` enforcing role-based authorization (USER / ADMIN) via `HttpSession`. |
| **Exp 9** | ACID Database Transactions | `RegistrationDAO.bookTickets()` utilizing transactional `setAutoCommit(false)` and row locking. |
| **Exp 10** | Secure Credential Storage & Auth Verification | Parameterized SQL queries (`PreparedStatement`) preventing SQL injection. |
| **Exp 11** | Dynamic DOM Rendering & State Management | Client-side reactive UI rendering for cards, modals, and ticket passes in `app.js`. |
| **Exp 12** | Event Verification & Alphanumeric Code Engine | UUID-based ticket token generation and `TicketVerifyServlet` validator. |
| **Exp 13** | Visual Analytics Reporting & Chart.js | `analytics.html` rendering dynamic Doughnut, Bar, and Comparative Occupancy Charts. |

---

## 💻 Technology Stack

* **Backend:** Java 17 / 21, Jakarta Servlet API 5.0+, JDBC (Java Database Connectivity), Google Gson 2.10.
* **Database:** MySQL 8.0 (default) with zero-config Embedded H2 Database auto-fallback.
* **Frontend:** HTML5, CSS3, JavaScript (ES6+), Bootstrap 5.3, Bootstrap Icons, Chart.js 4.4.
* **Build & Deployment:** Apache Maven 3.8+ with Cargo Tomcat 10 Embedded Plugin.
* **Alternative Zero-Config Runner:** Node.js HTTP runtime (`server.js`) for instant standalone evaluation.

---

## 🏛️ System Architecture (MVC + DAO)

```text
               +---------------------------------------------------+
               |             CLIENT BROWSER (HTML5 / ES6)          |
               |   index.html | events.html | admin.html | etc.   |
               +-------------------------+-------------------------+
                                         |
                                (HTTP / Fetch API)
                                         |
                                         v
+---------------------------------------------------------------------------------+
|                       JAKARTA SERVLET CONTROLLER LAYER                          |
|   RegisterServlet  |  LoginServlet       |  EventServlet  |  BookTicketServlet  |
|   AdminEventServlet|  AnalyticsServlet   |  FeedbackServlet| TicketVerifyServlet|
+----------------------------------------+----------------------------------------+
                                         |
                                (Delegates Request)
                                         |
                                         v
+---------------------------------------------------------------------------------+
|                           DATA ACCESS OBJECT (DAO) LAYER                        |
|        UserDAO         |       EventDAO       |       RegistrationDAO          |
+----------------------------------------+----------------------------------------+
                                         |
                                 (JDBC Operations)
                                         |
                                         v
+---------------------------------------------------------------------------------+
|                              PERSISTENCE LAYER                                  |
|            MySQL 8.0 Database  <--->  Embedded File H2 Database (./data)        |
+---------------------------------------------------------------------------------+
```

---

## 🗄️ Database Schema & ER Model

### Relational Schema

```sql
users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    event_date DATE NOT NULL,
    event_time VARCHAR(20) NOT NULL,
    venue VARCHAR(150) NOT NULL,
    organizer VARCHAR(100) NOT NULL,
    capacity INT NOT NULL DEFAULT 100,
    registered_count INT NOT NULL DEFAULT 0,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    banner_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'UPCOMING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    event_id INT NOT NULL REFERENCES events(id),
    tickets_count INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    ticket_code VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

feedbacks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    event_id INT NOT NULL REFERENCES events(id),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 📄 XML Schema & Validation

The project includes structured XML representations in `src/main/resources/`:
* `events.xml`: Structured XML representation of events.
* `events.dtd`: Document Type Definition establishing element grammar.
* `events.xsd`: W3C XML Schema validating data types (`xs:date`, `xs:decimal`, `xs:positiveInteger`, `xs:enumeration`).
* `XMLReader.java`: Standalone DOM parser demonstrating XML reading and XSD validation.

---

## 🔌 REST API Endpoints Specification

| Method | Endpoint | Access | Description |
|:-------|:---------|:-------|:------------|
| `GET` | `/api/auth/status` | Public | Returns current session login state and user object |
| `POST` | `/api/login` | Public | Authenticates credentials and starts `HttpSession` |
| `POST` | `/api/register` | Public | Registers a new user account |
| `POST` | `/api/logout` | Authenticated | Invalidates user session |
| `GET` | `/api/events` | Public | Lists events with optional filters (`?category=`, `?q=`, `?sortBy=`) |
| `GET` | `/api/events/{id}` | Public | Returns event details, average rating, and feedbacks |
| `POST` | `/api/admin/events` | Admin Only | Creates a new event |
| `PUT` | `/api/admin/events` | Admin Only | Updates an existing event |
| `DELETE` | `/api/admin/events/{id}`| Admin Only | Deletes an event |
| `POST` | `/api/registrations/book` | User Only | Books tickets with seat decrement |
| `GET` | `/api/registrations/my` | User Only | Retrieves user's booked tickets |
| `POST` | `/api/registrations/cancel`| User/Admin | Cancels booking and restores seats |
| `GET` | `/api/admin/registrations`| Admin Only | Lists all attendee registrations |
| `GET` | `/api/analytics` | Admin Only | Returns metrics & data arrays for Chart.js |
| `GET` | `/api/ticket/verify` | Public | Validates a ticket by alphanumeric code |

---

## 🚀 Setup & Execution Guide

### Option 1: Instant Zero-Config Execution (Recommended for Fast Testing)
```bash
# 1. Run the local server runner
node server.js

# 2. Open your browser at:
http://localhost:8080/
```

### Option 2: Apache Maven & Embedded Tomcat (Standard Java Web)
```bash
# 1. Build and run using the Maven Cargo Tomcat Plugin
mvn clean package cargo:run

# 2. Open your browser at:
http://localhost:8080/online-event-registration/
```

### Option 3: XML Parser & Validator Execution (Lab Exp 6)
```bash
# Compile and run XMLReader
javac -d target_classes -cp src/main/resources src/main/java/com/eventmgmt/XMLReader.java
java -cp target_classes com.eventmgmt.XMLReader
```

---

## 🔑 Default Demo Credentials

| Role | Username / Email | Password | Access Level |
|:-----|:-----------------|:---------|:-------------|
| **Administrator** | `admin` / `admin@events.local` | `admin123` | Full access to Admin Portal, Event CRUD, Attendees, and Analytics |
| **Attendee 1** | `john_doe` / `john@example.com` | `user123` | Booking tickets, viewing wallet, cancelling bookings |
| **Attendee 2** | `sarah_connor` / `sarah@example.com` | `user123` | Event registration & reviews |

---

## 📂 Project Directory Structure

```text
online-event-registration/
├── .gitignore
├── pom.xml
├── database.sql
├── server.js
├── README.md
├── data/
│   └── (Embedded H2 database storage)
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── eventmgmt/
        │           ├── DBConnection.java
        │           ├── XMLReader.java
        │           ├── model/
        │           │   ├── User.java
        │           │   ├── Event.java
        │           │   ├── Registration.java
        │           │   └── Feedback.java
        │           ├── dao/
        │           │   ├── UserDAO.java
        │           │   ├── EventDAO.java
        │           │   ├── RegistrationDAO.java
        │           │   └── FeedbackDAO.java
        │           ├── servlet/
        │           │   ├── RegisterServlet.java
        │           │   ├── LoginServlet.java
        │           │   ├── LogoutServlet.java
        │           │   ├── AuthStatusServlet.java
        │           │   ├── EventServlet.java
        │           │   ├── AdminEventServlet.java
        │           │   ├── BookTicketServlet.java
        │           │   ├── UserRegistrationsServlet.java
        │           │   ├── CancelRegistrationServlet.java
        │           │   ├── AdminRegistrationsServlet.java
        │           │   ├── AnalyticsServlet.java
        │           │   ├── FeedbackServlet.java
        │           │   └── TicketVerifyServlet.java
        │           └── util/
        │               ├── AuthUtil.java
        │               └── JsonUtil.java
        ├── resources/
        │   ├── events.xml
        │   ├── events.dtd
        │   └── events.xsd
        └── webapp/
            ├── WEB-INF/
            │   └── web.xml
            ├── index.html
            ├── events.html
            ├── event-details.html
            ├── login.html
            ├── register.html
            ├── dashboard.html
            ├── admin.html
            ├── analytics.html
            ├── verify-ticket.html
            ├── css/
            │   └── style.css
            └── js/
                └── app.js
```

---

## 📜 License

This project is developed for educational and laboratory evaluation purposes under the **MIT License**.
