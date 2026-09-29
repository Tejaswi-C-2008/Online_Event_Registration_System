/**
 * ====================================================================
 * ONLINE EVENT REGISTRATION SYSTEM - CLIENT APPLICATION (app.js)
 * High-performance ES6 Vanilla JavaScript App handling API requests,
 * state, dynamic rendering, modals, and Chart.js visualizations.
 * ====================================================================
 */

// Global State
const AppState = {
    currentUser: null,
    events: [],
    categories: [],
    selectedCategory: 'ALL'
};

// API Base Endpoints
const API = {
    AUTH_STATUS: 'api/auth/status',
    LOGIN: 'api/login',
    REGISTER: 'api/register',
    LOGOUT: 'api/logout',
    EVENTS: 'api/events',
    ADMIN_EVENTS: 'api/admin/events',
    BOOK_TICKET: 'api/registrations/book',
    MY_REGISTRATIONS: 'api/registrations/my',
    CANCEL_REGISTRATION: 'api/registrations/cancel',
    ADMIN_REGISTRATIONS: 'api/admin/registrations',
    ANALYTICS: 'api/analytics',
    FEEDBACK: 'api/feedback',
    TICKET_VERIFY: 'api/ticket/verify'
};

// Utility: Toast Notification
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `custom-toast ${type}`;

    let icon = 'bi-info-circle-fill text-primary';
    if (type === 'success') icon = 'bi-check-circle-fill text-success';
    if (type === 'error') icon = 'bi-exclamation-triangle-fill text-danger';
    if (type === 'warning') icon = 'bi-exclamation-circle-fill text-warning';

    toast.innerHTML = `
        <i class="bi ${icon} fs-5"></i>
        <div class="flex-grow-1">${message}</div>
        <button type="button" class="btn-close ms-2" style="font-size:0.75rem;" onclick="this.parentElement.remove()"></button>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        if (toast.parentElement) {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }
    }, 4500);
}

// Utility: Format Date
function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

// Utility: Format Currency
function formatCurrency(amount) {
    const val = parseFloat(amount || 0);
    if (val === 0) return 'Free';
    return '$' + val.toFixed(2);
}

// Initialize Auth State & Update Navbars
async function initAuth() {
    try {
        const res = await fetch(API.AUTH_STATUS);
        const data = await res.json();
        if (data.success && data.data && data.data.authenticated) {
            AppState.currentUser = data.data.user;
        } else {
            AppState.currentUser = null;
        }
    } catch (e) {
        console.warn('Auth check skipped or failed:', e);
        AppState.currentUser = null;
    }
    updateNavbarUI();
}

function updateNavbarUI() {
    const authNav = document.getElementById('navbar-auth-section');
    if (!authNav) return;

    if (AppState.currentUser) {
        const isAdmin = AppState.currentUser.role === 'ADMIN';
        authNav.innerHTML = `
            <div class="d-flex align-items-center gap-3">
                ${isAdmin ? `
                    <a href="admin.html" class="btn btn-outline-warning btn-sm d-flex align-items-center gap-1 font-monospace">
                        <i class="bi bi-shield-lock-fill"></i> Admin Panel
                    </a>
                ` : `
                    <a href="dashboard.html" class="btn btn-outline-primary btn-sm d-flex align-items-center gap-1">
                        <i class="bi bi-ticket-perforated-fill"></i> My Tickets
                    </a>
                `}
                <div class="dropdown">
                    <button class="btn btn-light dropdown-toggle d-flex align-items-center gap-2 border" type="button" data-bs-toggle="dropdown">
                        <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center fw-bold" style="width:30px; height:30px; font-size: 0.85rem;">
                            ${AppState.currentUser.fullName ? AppState.currentUser.fullName.charAt(0).toUpperCase() : 'U'}
                        </div>
                        <span class="d-none d-md-inline fw-semibold">${AppState.currentUser.fullName || AppState.currentUser.username}</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0">
                        <li class="dropdown-header text-muted small">Signed in as <strong>${AppState.currentUser.role}</strong></li>
                        ${!isAdmin ? `<li><a class="dropdown-item" href="dashboard.html"><i class="bi bi-person me-2"></i>My Bookings</a></li>` : ''}
                        ${isAdmin ? `<li><a class="dropdown-item" href="admin.html"><i class="bi bi-speedometer2 me-2"></i>Admin Dashboard</a></li>` : ''}
                        ${isAdmin ? `<li><a class="dropdown-item" href="analytics.html"><i class="bi bi-bar-chart-line me-2"></i>System Analytics</a></li>` : ''}
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="handleLogout()"><i class="bi bi-box-arrow-right me-2"></i>Sign Out</a></li>
                    </ul>
                </div>
            </div>
        `;
    } else {
        authNav.innerHTML = `
            <div class="d-flex align-items-center gap-2">
                <a href="login.html" class="btn btn-outline-custom btn-sm">Sign In</a>
                <a href="register.html" class="btn btn-primary-custom btn-sm">Register</a>
            </div>
        `;
    }
}

// Auth Handlers
async function handleLogin(e) {
    if (e) e.preventDefault();
    const form = document.getElementById('login-form');
    if (!form) return;

    const identifier = form.identifier.value.trim();
    const password = form.password.value.trim();

    if (!identifier || !password) {
        showToast('Please enter both username/email and password.', 'error');
        return;
    }

    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Signing in...';

    try {
        const res = await fetch(API.LOGIN, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identifier, password })
        });
        const data = await res.json();

        if (data.success) {
            showToast('Login successful! Redirecting...', 'success');
            setTimeout(() => {
                if (data.data && data.data.role === 'ADMIN') {
                    window.location.href = 'admin.html';
                } else {
                    window.location.href = 'events.html';
                }
            }, 800);
        } else {
            showToast(data.message || 'Invalid credentials.', 'error');
            btn.disabled = false;
            btn.innerHTML = 'Sign In';
        }
    } catch (err) {
        showToast('Server connection error. Please try again.', 'error');
        btn.disabled = false;
        btn.innerHTML = 'Sign In';
    }
}

async function handleRegister(e) {
    if (e) e.preventDefault();
    const form = document.getElementById('register-form');
    if (!form) return;

    const username = form.username.value.trim();
    const email = form.email.value.trim();
    const password = form.password.value.trim();
    const confirmPassword = form.confirmPassword.value.trim();
    const fullName = form.fullName.value.trim();
    const phone = form.phone ? form.phone.value.trim() : '';

    if (password !== confirmPassword) {
        showToast('Passwords do not match.', 'error');
        return;
    }

    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creating account...';

    try {
        const res = await fetch(API.REGISTER, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password, fullName, phone, role: 'USER' })
        });
        const data = await res.json();

        if (data.success) {
            showToast('Account created successfully! Welcome aboard.', 'success');
            setTimeout(() => {
                window.location.href = 'events.html';
            }, 900);
        } else {
            showToast(data.message || 'Registration failed.', 'error');
            btn.disabled = false;
            btn.innerHTML = 'Create Account';
        }
    } catch (err) {
        showToast('Server error during registration.', 'error');
        btn.disabled = false;
        btn.innerHTML = 'Create Account';
    }
}

async function handleLogout() {
    try {
        await fetch(API.LOGOUT, { method: 'POST' });
        showToast('Logged out successfully.', 'info');
        AppState.currentUser = null;
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 500);
    } catch (e) {
        window.location.href = 'index.html';
    }
}

// Event Rendering Functions
function createEventCardHTML(event) {
    const availableSeats = Math.max(0, event.capacity - event.registeredCount);
    const percentFilled = Math.min(100, Math.round((event.registeredCount / event.capacity) * 100));
    const isSoldOut = availableSeats <= 0;

    return `
        <div class="col-lg-4 col-md-6 mb-4">
            <div class="event-card">
                <div class="event-card-img-wrap">
                    <img src="${event.bannerUrl || 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800'}" alt="${event.title}" class="event-card-img" loading="lazy">
                    <span class="event-category-badge">${event.category}</span>
                    <span class="event-price-tag">${formatCurrency(event.price)}</span>
                </div>
                <div class="event-card-body">
                    <h5 class="event-title" title="${event.title}">${event.title}</h5>
                    <div class="event-meta-item">
                        <i class="bi bi-calendar3"></i>
                        <span>${formatDate(event.eventDate)} • ${event.eventTime}</span>
                    </div>
                    <div class="event-meta-item">
                        <i class="bi bi-geo-alt-fill"></i>
                        <span class="text-truncate">${event.venue}</span>
                    </div>
                    <p class="event-description">${event.description}</p>

                    <div class="capacity-meter-wrap">
                        <div class="capacity-labels">
                            <span><i class="bi bi-people-fill me-1"></i>${event.registeredCount} booked</span>
                            <span class="${isSoldOut ? 'text-danger' : 'text-success'}">${isSoldOut ? 'Sold Out' : availableSeats + ' spots left'}</span>
                        </div>
                        <div class="capacity-bar">
                            <div class="capacity-progress" style="width: ${percentFilled}%"></div>
                        </div>
                    </div>

                    <div class="d-flex gap-2 mt-auto">
                        <a href="event-details.html?id=${event.id}" class="btn btn-outline-secondary btn-sm flex-fill">
                            <i class="bi bi-eye me-1"></i> Details
                        </a>
                        <button class="btn btn-primary-custom btn-sm flex-fill ${isSoldOut ? 'disabled' : ''}"
                                onclick="openBookingModal(${event.id}, '${escapeHTML(event.title)}', ${event.price}, ${availableSeats})">
                            <i class="bi bi-ticket-perforated me-1"></i> ${isSoldOut ? 'Sold Out' : 'Book Now'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function escapeHTML(str) {
    if (!str) return '';
    return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

// Booking Modal Logic
let activeBookingEvent = null;

function openBookingModal(eventId, title, price, availableSeats) {
    if (!AppState.currentUser) {
        showToast('Please sign in to register and book tickets.', 'warning');
        setTimeout(() => {
            window.location.href = `login.html?redirect=${encodeURIComponent(window.location.href)}`;
        }, 1200);
        return;
    }

    activeBookingEvent = { id: eventId, title, price: parseFloat(price || 0), availableSeats };

    const modalTitle = document.getElementById('bookingModalTitle');
    const eventNameEl = document.getElementById('bookingModalEventName');
    const priceEl = document.getElementById('bookingModalPrice');
    const qtyInput = document.getElementById('ticketQuantityInput');
    const totalEl = document.getElementById('bookingModalTotal');

    if (modalTitle) modalTitle.innerText = 'Book Tickets';
    if (eventNameEl) eventNameEl.innerText = title;
    if (priceEl) priceEl.innerText = formatCurrency(price);
    if (qtyInput) {
        qtyInput.value = 1;
        qtyInput.max = Math.min(10, availableSeats);
    }
    if (totalEl) totalEl.innerText = formatCurrency(price);

    const bookingModal = new bootstrap.Modal(document.getElementById('ticketBookingModal'));
    bookingModal.show();
}

function updateBookingTotal() {
    if (!activeBookingEvent) return;
    const qtyInput = document.getElementById('ticketQuantityInput');
    const totalEl = document.getElementById('bookingModalTotal');
    const qty = parseInt(qtyInput ? qtyInput.value : 1) || 1;
    const total = qty * activeBookingEvent.price;
    if (totalEl) totalEl.innerText = formatCurrency(total);
}

async function confirmTicketBooking() {
    if (!activeBookingEvent) return;
    const qtyInput = document.getElementById('ticketQuantityInput');
    const ticketsCount = parseInt(qtyInput ? qtyInput.value : 1) || 1;

    const btn = document.getElementById('btnConfirmBooking');
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Confirming...';
    }

    try {
        const res = await fetch(API.BOOK_TICKET, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ eventId: activeBookingEvent.id, ticketsCount })
        });
        const data = await res.json();

        if (data.success) {
            const bookingModal = bootstrap.Modal.getInstance(document.getElementById('ticketBookingModal'));
            if (bookingModal) bookingModal.hide();

            showToast('Booking confirmed! Your ticket pass is generated.', 'success');

            // Show ticket confirmation modal
            renderTicketConfirmation(data.data);
        } else {
            showToast(data.message || 'Booking failed.', 'error');
        }
    } catch (e) {
        showToast('Error booking tickets. Please try again.', 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-check-circle me-1"></i> Complete Booking';
        }
    }
}

function renderTicketConfirmation(ticket) {
    const confirmModalEl = document.getElementById('ticketConfirmationModal');
    if (!confirmModalEl) return;

    document.getElementById('confirmTicketCode').innerText = ticket.ticketCode;
    document.getElementById('confirmEventTitle').innerText = ticket.eventTitle || activeBookingEvent.title;
    document.getElementById('confirmEventVenue').innerText = ticket.eventVenue || 'Venue confirmed in email';
    document.getElementById('confirmTicketsCount').innerText = `${ticket.ticketsCount} Ticket(s)`;
    document.getElementById('confirmTotalPrice').innerText = formatCurrency(ticket.totalPrice);

    const modal = new bootstrap.Modal(confirmModalEl);
    modal.show();
}

// User Dashboard: Load Registrations
async function loadUserRegistrations() {
    const listContainer = document.getElementById('user-registrations-container');
    if (!listContainer) return;

    listContainer.innerHTML = `
        <div class="text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="text-muted mt-2">Loading your tickets...</p>
        </div>
    `;

    try {
        const res = await fetch(API.MY_REGISTRATIONS);
        const data = await res.json();

        if (data.success && Array.isArray(data.data) && data.data.length > 0) {
            listContainer.innerHTML = data.data.map(reg => {
                const isConfirmed = reg.status === 'CONFIRMED';
                return `
                    <div class="col-lg-6 mb-4">
                        <div class="ticket-pass">
                            <div class="ticket-header d-flex justify-content-between align-items-center">
                                <div>
                                    <span class="badge bg-light text-dark text-uppercase mb-1">${reg.eventCategory || 'Event'}</span>
                                    <h5 class="mb-0 text-white">${reg.eventTitle}</h5>
                                </div>
                                <span class="status-badge ${isConfirmed ? 'status-upcoming' : 'status-cancelled'}">
                                    ${reg.status}
                                </span>
                            </div>
                            <div class="ticket-body">
                                <div class="row align-items-center">
                                    <div class="col-8">
                                        <div class="mb-2">
                                            <small class="text-muted d-block"><i class="bi bi-calendar-event me-1"></i> Date & Time</small>
                                            <strong>${formatDate(reg.eventDate)} • ${reg.eventTime || '10:00 AM'}</strong>
                                        </div>
                                        <div class="mb-2">
                                            <small class="text-muted d-block"><i class="bi bi-geo-alt me-1"></i> Venue</small>
                                            <span class="text-truncate d-block">${reg.eventVenue}</span>
                                        </div>
                                        <div class="mb-2">
                                            <small class="text-muted d-block"><i class="bi bi-ticket me-1"></i> Quantity & Total</small>
                                            <span>${reg.ticketsCount} Ticket(s) — <strong>${formatCurrency(reg.totalPrice)}</strong></span>
                                        </div>
                                        <div class="mt-3">
                                            <span class="ticket-code-badge">${reg.ticketCode}</span>
                                        </div>
                                    </div>
                                    <div class="col-4 text-center">
                                        <div class="qr-sim-box mx-auto">
                                            <i class="bi bi-qr-code"></i>
                                        </div>
                                        <small class="text-muted d-block mt-1 font-monospace" style="font-size:0.7rem;">Scan at Gate</small>
                                    </div>
                                </div>
                                ${isConfirmed ? `
                                    <div class="border-top pt-3 mt-3 d-flex justify-content-between align-items-center">
                                        <button class="btn btn-outline-danger btn-sm" onclick="cancelRegistration(${reg.id})">
                                            <i class="bi bi-x-circle me-1"></i> Cancel Booking
                                        </button>
                                        <button class="btn btn-outline-primary btn-sm" onclick="printTicket('${reg.ticketCode}')">
                                            <i class="bi bi-printer me-1"></i> Print Pass
                                        </button>
                                    </div>
                                ` : ''}
                            </div>
                        </div>
                    </div>
                `;
            }).join('');
        } else {
            listContainer.innerHTML = `
                <div class="col-12 text-center py-5 bg-white rounded-3 border">
                    <i class="bi bi-ticket-perforated display-3 text-muted"></i>
                    <h4 class="mt-3">No Bookings Yet</h4>
                    <p class="text-muted">You haven't registered for any events yet.</p>
                    <a href="events.html" class="btn btn-primary-custom mt-2">Explore Events</a>
                </div>
            `;
        }
    } catch (e) {
        listContainer.innerHTML = `<div class="alert alert-danger">Error loading registrations.</div>`;
    }
}

async function cancelRegistration(registrationId) {
    if (!confirm('Are you sure you want to cancel this event registration? This will release your reserved tickets.')) {
        return;
    }

    try {
        const res = await fetch(API.CANCEL_REGISTRATION, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ registrationId })
        });
        const data = await res.json();

        if (data.success) {
            showToast('Registration cancelled successfully.', 'success');
            loadUserRegistrations();
        } else {
            showToast(data.message || 'Failed to cancel.', 'error');
        }
    } catch (e) {
        showToast('Error processing cancellation.', 'error');
    }
}

function printTicket(ticketCode) {
    window.print();
}

// Auto-run on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    initAuth();
});
