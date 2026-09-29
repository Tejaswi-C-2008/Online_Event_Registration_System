/**
 * ====================================================================
 * ONLINE EVENT REGISTRATION SYSTEM - ZERO-CONFIG LOCAL SERVER RUNNER
 * Provides an instant runtime for development, grading, and testing.
 * ====================================================================
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = process.env.PORT || 8080;
const WEBAPP_DIR = path.join(__dirname, 'src', 'main', 'webapp');

// In-Memory Database for local zero-config server
let users = [
    { id: 1, username: 'admin', email: 'admin@events.local', password: 'admin123', fullName: 'System Administrator', phone: '+1-555-0100', role: 'ADMIN', createdAt: new Date() },
    { id: 2, username: 'john_doe', email: 'john@example.com', password: 'user123', fullName: 'John Doe', phone: '+1-555-0101', role: 'USER', createdAt: new Date() },
    { id: 3, username: 'sarah_connor', email: 'sarah@example.com', password: 'user123', fullName: 'Sarah Connor', phone: '+1-555-0102', role: 'USER', createdAt: new Date() },
    { id: 4, username: 'alex_kumar', email: 'alex@example.com', password: 'user123', fullName: 'Alex Kumar', phone: '+1-555-0103', role: 'USER', createdAt: new Date() }
];

let events = [
    {
        id: 1,
        title: 'Global AI & Cloud Summit 2026',
        description: 'Explore the bleeding edge of Artificial Intelligence, Cloud Computing, and Neural Architectures with world-class tech leaders.',
        category: 'Technology',
        eventDate: '2026-11-15',
        eventTime: '09:30 AM',
        venue: 'Tech Convention Center, Hall A',
        organizer: 'AI Innovations Lab',
        capacity: 250,
        registeredCount: 42,
        price: 49.99,
        bannerUrl: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800',
        status: 'UPCOMING'
    },
    {
        id: 2,
        title: 'International Jazz & Indie Beats',
        description: 'An evening of electrifying jazz, soul, and indie acoustic performances by award-winning global artists.',
        category: 'Music',
        eventDate: '2026-10-25',
        eventTime: '06:00 PM',
        venue: 'Grand Symphony Amphitheatre',
        organizer: 'Harmony Productions',
        capacity: 400,
        registeredCount: 185,
        price: 29.00,
        bannerUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800',
        status: 'UPCOMING'
    },
    {
        id: 3,
        title: 'Full-Stack Web3 & Microservices Workshop',
        description: 'Hands-on coding masterclass covering Microservices in Java, Docker deployment, and scalable system design.',
        category: 'Workshop',
        eventDate: '2026-10-18',
        eventTime: '10:00 AM',
        venue: 'Silicon Valley Innovation Hub, Lab 3',
        organizer: 'CodeCraft Academy',
        capacity: 60,
        registeredCount: 48,
        price: 15.00,
        bannerUrl: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800',
        status: 'UPCOMING'
    },
    {
        id: 4,
        title: 'Global Venture Startup Pitchfest',
        description: 'Connect with angel investors, VC funds, and promising tech founders showcasing disruptive innovations.',
        category: 'Business',
        eventDate: '2026-11-05',
        eventTime: '01:00 PM',
        venue: 'Metropolitan Business Tower, 14th Floor',
        organizer: 'Venture Hub Network',
        capacity: 150,
        registeredCount: 95,
        price: 35.00,
        bannerUrl: 'https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800',
        status: 'UPCOMING'
    },
    {
        id: 5,
        title: 'City Marathon & Fitness Carnival 2026',
        description: 'Annual 10K/21K run promoting health, wellness, and youth community sports with medals and refreshments.',
        category: 'Sports',
        eventDate: '2026-11-20',
        eventTime: '06:00 AM',
        venue: 'Central Riverside Park Boulevard',
        organizer: 'City Sports Commission',
        capacity: 500,
        registeredCount: 320,
        price: 10.00,
        bannerUrl: 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=800',
        status: 'UPCOMING'
    },
    {
        id: 6,
        title: 'Digital Arts & Immersive VR Exhibition',
        description: 'Interactive art installation featuring digital NFT galleries, 3D projection mapping, and VR experiences.',
        category: 'Cultural',
        eventDate: '2026-12-02',
        eventTime: '11:00 AM',
        venue: 'Contemporary Design Museum',
        organizer: 'Creative Minds Collective',
        capacity: 120,
        registeredCount: 28,
        price: 20.00,
        bannerUrl: 'https://images.unsplash.com/photo-1508997449629-303059a039c0?w=800',
        status: 'UPCOMING'
    }
];

let registrations = [
    { id: 1, userId: 2, eventId: 1, ticketsCount: 2, totalPrice: 99.98, ticketCode: 'TKT-2026-AI7821', status: 'CONFIRMED', registrationDate: new Date() },
    { id: 2, userId: 2, eventId: 3, ticketsCount: 1, totalPrice: 15.00, ticketCode: 'TKT-2026-WS4412', status: 'CONFIRMED', registrationDate: new Date() },
    { id: 3, userId: 3, eventId: 2, ticketsCount: 3, totalPrice: 87.00, ticketCode: 'TKT-2026-JZ9034', status: 'CONFIRMED', registrationDate: new Date() },
    { id: 4, userId: 4, eventId: 5, ticketsCount: 1, totalPrice: 10.00, ticketCode: 'TKT-2026-SP1198', status: 'CONFIRMED', registrationDate: new Date() }
];

let feedbacks = [
    { id: 1, userId: 2, eventId: 1, rating: 5, comment: 'Outstanding lineup of speakers and state-of-the-art keynote topics! Highly recommended.', userFullName: 'John Doe', createdAt: new Date() },
    { id: 2, userId: 3, eventId: 2, rating: 4, comment: 'Great musical atmosphere and very well-organized seating arrangement.', userFullName: 'Sarah Connor', createdAt: new Date() },
    { id: 3, userId: 4, eventId: 3, rating: 5, comment: 'The hands-on coding exercises were extremely insightful and practical.', userFullName: 'Alex Kumar', createdAt: new Date() }
];

// In-Memory Sessions
const sessions = new Map();

function getSession(req) {
    const cookie = req.headers.cookie;
    if (cookie) {
        const match = cookie.match(/SESSIONID=([^;]+)/);
        if (match && sessions.has(match[1])) {
            return { id: match[1], data: sessions.get(match[1]) };
        }
    }
    return null;
}

function parseBody(req) {
    return new Promise((resolve) => {
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            try {
                resolve(body ? JSON.parse(body) : {});
            } catch (e) {
                resolve({});
            }
        });
    });
}

function sendJson(res, statusCode, data) {
    res.writeHead(statusCode, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(data));
}

// MIME Types
const MIME_TYPES = {
    '.html': 'text/html',
    '.css': 'text/css',
    '.js': 'text/javascript',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon'
};

const server = http.createServer(async (req, res) => {
    const parsedUrl = url.parse(req.url, true);
    let pathname = parsedUrl.pathname;

    // CORS Headers
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        res.writeHead(200);
        res.end();
        return;
    }

    const session = getSession(req);
    const currentUser = session ? session.data.user : null;

    // ==========================================
    // REST API ROUTES
    // ==========================================

    // 1. Auth Status
    if (pathname === '/api/auth/status' && req.method === 'GET') {
        return sendJson(res, 200, {
            success: true,
            data: {
                authenticated: Boolean(currentUser),
                user: currentUser ? { ...currentUser, password: null } : null
            }
        });
    }

    // 2. Login
    if (pathname === '/api/login' && req.method === 'POST') {
        const body = await parseBody(req);
        const identifier = (body.identifier || '').trim().toLowerCase();
        const password = (body.password || '').trim();

        const user = users.find(u =>
            (u.username.toLowerCase() === identifier || u.email.toLowerCase() === identifier) &&
            u.password === password
        );

        if (user) {
            const sid = Math.random().toString(36).substring(2) + Date.now().toString(36);
            sessions.set(sid, { user });
            res.setHeader('Set-Cookie', `SESSIONID=${sid}; Path=/; HttpOnly`);
            return sendJson(res, 200, {
                success: true,
                message: 'Login successful!',
                data: { ...user, password: null }
            });
        } else {
            return sendJson(res, 401, { success: false, message: 'Invalid username/email or password.' });
        }
    }

    // 3. Register
    if (pathname === '/api/register' && req.method === 'POST') {
        const body = await parseBody(req);
        const { username, email, password, fullName, phone } = body;

        if (!username || !email || !password || !fullName) {
            return sendJson(res, 400, { success: false, message: 'All required fields must be provided.' });
        }

        if (users.some(u => u.username.toLowerCase() === username.trim().toLowerCase())) {
            return sendJson(res, 409, { success: false, message: 'Username is already taken.' });
        }
        if (users.some(u => u.email.toLowerCase() === email.trim().toLowerCase())) {
            return sendJson(res, 409, { success: false, message: 'Email address is already registered.' });
        }

        const newUser = {
            id: users.length + 1,
            username: username.trim(),
            email: email.trim().toLowerCase(),
            password: password.trim(),
            fullName: fullName.trim(),
            phone: phone ? phone.trim() : '',
            role: 'USER',
            createdAt: new Date()
        };
        users.push(newUser);

        const sid = Math.random().toString(36).substring(2) + Date.now().toString(36);
        sessions.set(sid, { user: newUser });
        res.setHeader('Set-Cookie', `SESSIONID=${sid}; Path=/; HttpOnly`);

        return sendJson(res, 200, {
            success: true,
            message: 'Account created successfully!',
            data: { ...newUser, password: null }
        });
    }

    // 4. Logout
    if (pathname === '/api/logout') {
        if (session) {
            sessions.delete(session.id);
        }
        res.setHeader('Set-Cookie', 'SESSIONID=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT');
        return sendJson(res, 200, { success: true, message: 'Logged out.' });
    }

    // 5. Events List & Single Event
    if (pathname.startsWith('/api/events')) {
        const parts = pathname.split('/').filter(Boolean); // ['api', 'events', '123']
        if (parts.length === 3 && !isNaN(parts[2])) {
            const evId = parseInt(parts[2]);
            const event = events.find(e => e.id === evId);
            if (event) {
                const eventFeedbacks = feedbacks.filter(f => f.eventId === evId);
                const avgRating = eventFeedbacks.length > 0
                    ? eventFeedbacks.reduce((sum, f) => sum + f.rating, 0) / eventFeedbacks.length
                    : 5.0;
                return sendJson(res, 200, {
                    success: true,
                    data: { event, feedbacks: eventFeedbacks, averageRating: avgRating }
                });
            } else {
                return sendJson(res, 404, { success: false, message: 'Event not found.' });
            }
        }

        // List with filters
        let result = [...events];
        const { category, q, sortBy, limit } = parsedUrl.query;

        if (category && category !== 'ALL') {
            result = result.filter(e => e.category.toLowerCase() === category.toLowerCase());
        }
        if (q) {
            const query = q.toLowerCase();
            result = result.filter(e =>
                e.title.toLowerCase().includes(query) ||
                e.description.toLowerCase().includes(query) ||
                e.venue.toLowerCase().includes(query)
            );
        }
        if (sortBy === 'price_asc') {
            result.sort((a, b) => a.price - b.price);
        } else if (sortBy === 'price_desc') {
            result.sort((a, b) => b.price - a.price);
        } else if (sortBy === 'popular') {
            result.sort((a, b) => b.registeredCount - a.registeredCount);
        } else {
            result.sort((a, b) => new Date(a.eventDate) - new Date(b.eventDate));
        }

        if (limit && !isNaN(limit)) {
            result = result.slice(0, parseInt(limit));
        }

        return sendJson(res, 200, { success: true, data: result });
    }

    // 6. Admin Event CRUD
    if (pathname.startsWith('/api/admin/events')) {
        if (!currentUser || currentUser.role !== 'ADMIN') {
            return sendJson(res, 403, { success: false, message: 'Administrator privileges required.' });
        }

        if (req.method === 'POST') {
            const body = await parseBody(req);
            const newEvent = {
                id: events.length + 1,
                title: body.title,
                description: body.description || '',
                category: body.category || 'Technology',
                eventDate: body.eventDate,
                eventTime: body.eventTime || '10:00 AM',
                venue: body.venue,
                organizer: body.organizer || 'Event Management',
                capacity: parseInt(body.capacity) || 100,
                registeredCount: 0,
                price: parseFloat(body.price) || 0,
                bannerUrl: body.bannerUrl || 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800',
                status: body.status || 'UPCOMING'
            };
            events.push(newEvent);
            return sendJson(res, 200, { success: true, message: 'Event created!', data: newEvent });
        }

        if (req.method === 'PUT') {
            const body = await parseBody(req);
            const idx = events.findIndex(e => e.id === body.id);
            if (idx !== -1) {
                events[idx] = { ...events[idx], ...body, price: parseFloat(body.price) || 0, capacity: parseInt(body.capacity) || 100 };
                return sendJson(res, 200, { success: true, message: 'Event updated!', data: events[idx] });
            }
            return sendJson(res, 404, { success: false, message: 'Event not found.' });
        }

        if (req.method === 'DELETE') {
            const parts = pathname.split('/').filter(Boolean);
            const evId = parseInt(parts[parts.length - 1]);
            events = events.filter(e => e.id !== evId);
            return sendJson(res, 200, { success: true, message: 'Event deleted!' });
        }
    }

    // 7. Book Tickets
    if (pathname === '/api/registrations/book' && req.method === 'POST') {
        if (!currentUser) {
            return sendJson(res, 401, { success: false, message: 'Login required to book tickets.' });
        }

        const body = await parseBody(req);
        const eventId = parseInt(body.eventId);
        const ticketsCount = parseInt(body.ticketsCount) || 1;

        const event = events.find(e => e.id === eventId);
        if (!event) {
            return sendJson(res, 404, { success: false, message: 'Event not found.' });
        }

        const available = event.capacity - event.registeredCount;
        if (available < ticketsCount) {
            return sendJson(res, 400, { success: false, message: 'Not enough available seats.' });
        }

        event.registeredCount += ticketsCount;
        const ticketCode = 'TKT-' + (1000 + Math.floor(Math.random() * 9000)) + '-' + Math.random().toString(36).substring(2, 8).toUpperCase();
        const newReg = {
            id: registrations.length + 1,
            userId: currentUser.id,
            eventId: event.id,
            ticketsCount,
            totalPrice: event.price * ticketsCount,
            ticketCode,
            status: 'CONFIRMED',
            registrationDate: new Date(),
            userName: currentUser.fullName,
            userEmail: currentUser.email,
            eventTitle: event.title,
            eventCategory: event.category,
            eventDate: event.eventDate,
            eventTime: event.eventTime,
            eventVenue: event.venue
        };
        registrations.push(newReg);

        return sendJson(res, 200, {
            success: true,
            message: 'Tickets booked successfully!',
            data: newReg
        });
    }

    // 8. User Registrations
    if (pathname === '/api/registrations/my') {
        if (!currentUser) {
            return sendJson(res, 401, { success: false, message: 'Login required.' });
        }
        const myRegs = registrations.filter(r => r.userId === currentUser.id).map(r => {
            const ev = events.find(e => e.id === r.eventId);
            return {
                ...r,
                userName: currentUser.fullName,
                userEmail: currentUser.email,
                eventTitle: ev ? ev.title : 'Event',
                eventCategory: ev ? ev.category : 'Category',
                eventDate: ev ? ev.eventDate : '',
                eventTime: ev ? ev.eventTime : '',
                eventVenue: ev ? ev.venue : ''
            };
        });
        return sendJson(res, 200, { success: true, data: myRegs });
    }

    // 9. Cancel Registration
    if (pathname === '/api/registrations/cancel' && req.method === 'POST') {
        if (!currentUser) return sendJson(res, 401, { success: false, message: 'Login required.' });
        const body = await parseBody(req);
        const regId = parseInt(body.registrationId);

        const reg = registrations.find(r => r.id === regId && r.status === 'CONFIRMED');
        if (reg && (reg.userId === currentUser.id || currentUser.role === 'ADMIN')) {
            reg.status = 'CANCELLED';
            const ev = events.find(e => e.id === reg.eventId);
            if (ev) {
                ev.registeredCount = Math.max(0, ev.registeredCount - reg.ticketsCount);
            }
            return sendJson(res, 200, { success: true, message: 'Registration cancelled.' });
        }
        return sendJson(res, 400, { success: false, message: 'Cannot cancel registration.' });
    }

    // 10. Admin Registrations
    if (pathname === '/api/admin/registrations') {
        if (!currentUser || currentUser.role !== 'ADMIN') {
            return sendJson(res, 403, { success: false, message: 'Admin access required.' });
        }
        const allRegs = registrations.map(r => {
            const u = users.find(user => user.id === r.userId);
            const ev = events.find(e => e.id === r.eventId);
            return {
                ...r,
                userName: u ? u.fullName : 'Attendee',
                userEmail: u ? u.email : '',
                eventTitle: ev ? ev.title : 'Event',
                eventCategory: ev ? ev.category : '',
                eventDate: ev ? ev.eventDate : '',
                eventVenue: ev ? ev.venue : ''
            };
        });
        return sendJson(res, 200, { success: true, data: allRegs });
    }

    // 11. Analytics
    if (pathname === '/api/analytics') {
        const totalUsers = users.filter(u => u.role === 'USER').length;
        const totalEvents = events.length;
        const activeRegistrations = registrations.filter(r => r.status === 'CONFIRMED').length;
        const totalRevenue = registrations.filter(r => r.status === 'CONFIRMED').reduce((s, r) => s + r.totalPrice, 0);

        // Category breakdown
        const categoryStats = [];
        const cats = [...new Set(events.map(e => e.category))];
        cats.forEach(cat => {
            const catEvents = events.filter(e => e.category === cat);
            const catEventIds = catEvents.map(e => e.id);
            const catRegs = registrations.filter(r => catEventIds.includes(r.eventId) && r.status === 'CONFIRMED');
            const rev = catRegs.reduce((sum, r) => sum + r.totalPrice, 0);
            categoryStats.push({
                category: cat,
                registrations: catRegs.reduce((sum, r) => sum + r.ticketsCount, 0),
                revenue: rev
            });
        });

        // Top Events
        const topEvents = [...events].sort((a, b) => b.registeredCount - a.registeredCount).slice(0, 5);

        return sendJson(res, 200, {
            success: true,
            data: {
                totalUsers,
                totalEvents,
                activeRegistrations,
                totalRevenue,
                categoryStats,
                topEvents
            }
        });
    }

    // 12. Feedback
    if (pathname === '/api/feedback') {
        if (req.method === 'POST') {
            if (!currentUser) return sendJson(res, 401, { success: false, message: 'Login required.' });
            const body = await parseBody(req);
            const newFb = {
                id: feedbacks.length + 1,
                userId: currentUser.id,
                eventId: parseInt(body.eventId),
                rating: parseInt(body.rating),
                comment: body.comment || '',
                userFullName: currentUser.fullName,
                createdAt: new Date()
            };
            feedbacks.push(newFb);
            return sendJson(res, 200, { success: true, message: 'Feedback saved!', data: newFb });
        }
    }

    // 13. Ticket Verify
    if (pathname === '/api/ticket/verify') {
        const code = (parsedUrl.query.code || '').trim().toUpperCase();
        const reg = registrations.find(r => r.ticketCode.toUpperCase() === code);
        if (reg) {
            const u = users.find(user => user.id === reg.userId);
            const ev = events.find(e => e.id === reg.eventId);
            return sendJson(res, 200, {
                success: true,
                data: {
                    ...reg,
                    userName: u ? u.fullName : 'Attendee',
                    userEmail: u ? u.email : '',
                    eventTitle: ev ? ev.title : 'Event'
                }
            });
        }
        return sendJson(res, 404, { success: false, message: 'Invalid ticket code.' });
    }

    // ==========================================
    // STATIC FILE SERVING (src/main/webapp)
    // ==========================================
    let filePath = path.join(WEBAPP_DIR, pathname === '/' ? 'index.html' : pathname);

    fs.stat(filePath, (err, stats) => {
        if (err || !stats.isFile()) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('404 Not Found');
            return;
        }

        const ext = path.extname(filePath).toLowerCase();
        const mimeType = MIME_TYPES[ext] || 'application/octet-stream';

        res.writeHead(200, { 'Content-Type': mimeType });
        fs.createReadStream(filePath).pipe(res);
    });
});

server.listen(PORT, () => {
    console.log('================================================================');
    console.log(` 🎟️ Online Event Registration System Server Running!`);
    console.log(` 🌐 Web Application URL : http://localhost:${PORT}/`);
    console.log(` 👤 Demo Admin Account   : admin / admin123`);
    console.log(` 👤 Demo User Account    : john@example.com / user123`);
    console.log('================================================================');
});
