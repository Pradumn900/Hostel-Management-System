// ===== API CONFIGURATION =====
const API_BASE = '/api';

// ===== AUTH UTILITIES =====
const Auth = {
    getToken() { return sessionStorage.getItem('token'); },
    getUser() {
        const u = sessionStorage.getItem('user');
        return u ? JSON.parse(u) : null;
    },
    setSession(data) {
        sessionStorage.setItem('token', data.token);
        sessionStorage.setItem('user', JSON.stringify({ username: data.username, fullName: data.fullName, role: data.role }));
    },
    clearSession() {
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('user');
    },
    isLoggedIn() { return !!this.getToken(); },
    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = '/login.html';
        }
    },
    redirectIfLoggedIn() {
        if (this.isLoggedIn()) {
            window.location.href = '/dashboard.html';
        }
    }
};

// ===== HTTP CLIENT =====
const API = {
    async request(method, path, body = null) {
        const headers = { 'Content-Type': 'application/json' };
        const token = Auth.getToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;
        const options = { method, headers };
        if (body !== null) options.body = JSON.stringify(body);
        const response = await fetch(API_BASE + path, options);
        if (response.status === 401) {
            Auth.clearSession();
            window.location.href = '/login.html';
            return;
        }
        const text = await response.text();
        let data;
        try { data = text ? JSON.parse(text) : null; } catch { data = text; }
        if (!response.ok) {
            const msg = (data && data.error) ? data.error :
                        (typeof data === 'object' ? Object.values(data).join(', ') : 'Request failed');
            throw new Error(msg);
        }
        return data;
    },
    get: (path) => API.request('GET', path),
    post: (path, body) => API.request('POST', path, body),
    put: (path, body) => API.request('PUT', path, body),
    patch: (path, body) => API.request('PATCH', path, body),
    delete: (path) => API.request('DELETE', path)
};

// ===== TOAST NOTIFICATIONS =====
const Toast = {
    container: null,
    init() {
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.className = 'toast-container';
            document.body.appendChild(this.container);
        }
    },
    show(message, type = 'default', duration = 3500) {
        this.init();
        const icons = { success: '✅', error: '❌', warning: '⚠️', default: 'ℹ️' };
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `<span>${icons[type] || icons.default}</span><span>${message}</span>`;
        this.container.appendChild(toast);
        setTimeout(() => { toast.style.opacity = '0'; toast.style.transition = 'opacity 0.3s'; setTimeout(() => toast.remove(), 300); }, duration);
    },
    success: (msg) => Toast.show(msg, 'success'),
    error: (msg) => Toast.show(msg, 'error'),
    warning: (msg) => Toast.show(msg, 'warning')
};

// ===== MODAL UTILITIES =====
const Modal = {
    open(id) {
        const el = document.getElementById(id);
        if (el) el.classList.add('show');
    },
    close(id) {
        const el = document.getElementById(id);
        if (el) el.classList.remove('show');
    },
    closeAll() {
        document.querySelectorAll('.modal-overlay.show').forEach(m => m.classList.remove('show'));
    }
};

// Close modal on overlay click
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) Modal.closeAll();
});
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') Modal.closeAll();
});

// ===== SIDEBAR SETUP =====
function initSidebar(activePage) {
    const user = Auth.getUser();
    if (!user) return;
    // Set user info
    const nameEl = document.getElementById('sidebarUserName');
    const initEl = document.getElementById('sidebarUserInitial');
    const roleEl = document.getElementById('sidebarUserRole');
    if (nameEl) nameEl.textContent = user.fullName;
    if (initEl) initEl.textContent = user.fullName ? user.fullName[0].toUpperCase() : 'W';
    if (roleEl) roleEl.textContent = user.role;
    // Set active nav link
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.dataset.page === activePage) link.classList.add('active');
    });
}

function logout() {
    API.post('/auth/logout').catch(() => {});
    Auth.clearSession();
    window.location.href = '/login.html';
}

// ===== DATE UTILITIES =====
const DateUtil = {
    today() { return new Date().toISOString().split('T')[0]; },
    format(dateStr) {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    },
    formatDateTime(dt) {
        if (!dt) return '-';
        return new Date(dt).toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    }
};

// ===== LOADING STATE =====
function setLoading(containerId, isLoading) {
    const el = document.getElementById(containerId);
    if (!el) return;
    if (isLoading) {
        el.innerHTML = '<div class="loading"><div class="spinner"></div><span>Loading...</span></div>';
    }
}

// ===== DEBOUNCE =====
function debounce(fn, delay) {
    let timeout;
    return (...args) => { clearTimeout(timeout); timeout = setTimeout(() => fn(...args), delay); };
}
