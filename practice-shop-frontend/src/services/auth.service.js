import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const API_URL = '/auth';
const USER_STORAGE_KEY = 'user';
const AUTH_EVENT = 'auth-change';

class AuthService {
    async login(email, password) {
        const response = await axios.post(`${API_URL}/login`, { email, password });
        if (response.data?.accessToken) {
            this.persistUser(response.data.accessToken, response.data.refreshToken);
        }
        return response.data;
    }

    async logout() {
        const user = this.getCurrentUser();
        if (user?.accessToken && user?.refreshToken) {
            try {
                await axios.post(`${API_URL}/logout`, {
                    accessToken: user.accessToken,
                    refreshToken: user.refreshToken,
                });
            } catch (error) {
                // ignore logout failure; we still clear local state
            }
        }
        this.clearUser();
    }

    async signup(payload) {
        return axios.post(`${API_URL}/register`, payload);
    }

    async resendVerificationEmail(email) {
        return axios.post(`${API_URL}/verify-email/resend`, { email });
    }

    async verifyEmail(token) {
        return axios.post(`${API_URL}/verify-email`, { token });
    }

    async requestPasswordReset(email) {
        return axios.post(`${API_URL}/forgot-password`, { email });
    }

    async resetPassword(token, newPassword) {
        return axios.post(`${API_URL}/reset-password`, { token, newPassword });
    }

    async completeOAuth2Registration(temporaryToken, details) {
        const response = await axios.post(`${API_URL}/oauth2/register`, {
            temporaryToken,
            ...details,
        });
        if (response.data?.accessToken) {
            this.persistUser(response.data.accessToken, response.data.refreshToken);
        }
        return response.data;
    }

    getCurrentUser() {
        const raw = localStorage.getItem(USER_STORAGE_KEY);
        if (!raw) {
            return null;
        }

        try {
            return JSON.parse(raw);
        } catch (error) {
            this.clearUser();
            return null;
        }
    }

    getUserProfile() {
        return axios.get('/api/users/profile', { headers: this.authHeader() });
    }

    updateUserProfile(profile) {
        return axios.put('/api/users/profile', profile, { headers: this.authHeader() });
    }

    async refreshAccessToken() {
        const user = this.getCurrentUser();
        if (!user?.refreshToken) {
            throw new Error('No refresh token available');
        }

        const response = await axios.post(`${API_URL}/refresh-token`, user.refreshToken, {
            headers: { 'Content-Type': 'text/plain' },
        });

        if (response.data?.accessToken) {
            this.persistUser(response.data.accessToken, response.data.refreshToken);
        }

        return response.data;
    }

    authHeader() {
        const user = this.getCurrentUser();
        if (user?.accessToken) {
            return { Authorization: `Bearer ${user.accessToken}` };
        }
        return {};
    }

    persistUser(accessToken, refreshToken) {
        const decoded = this.decodeToken(accessToken);
        const user = {
            accessToken,
            refreshToken,
            email: decoded?.sub ?? null,
            username: decoded?.username ?? decoded?.sub ?? null,
            roles: decoded?.role ? [decoded.role] : [],
        };
        localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
        window.dispatchEvent(new Event(AUTH_EVENT));
        return user;
    }

    clearUser() {
        localStorage.removeItem(USER_STORAGE_KEY);
        window.dispatchEvent(new Event(AUTH_EVENT));
    }

    decodeToken(token) {
        try {
            return jwtDecode(token);
        } catch (error) {
            return null;
        }
    }

    onAuthChange(callback) {
        window.addEventListener(AUTH_EVENT, callback);
        return () => window.removeEventListener(AUTH_EVENT, callback);
    }
}

const authService = new AuthService();

export default authService;
