import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const API_URL = '/auth';
const USER_STORAGE_KEY = 'user';
const AUTH_EVENT = 'auth-change';

class AuthService {
    async login(username, password) { // LoginPage에서 'username'으로 호출
        // LoginPage에서는 'username'으로 사용하지만, 백엔드 API는 'email'을 기대하므로
        // 요청 객체의 키를 'email'로 명시적으로 설정합니다.
        const response = await axios.post(`${API_URL}/login`, { 
            email: username, // 'username' 변수의 값을 'email' 키에 담아 전송
            password 
        }); 
        if (response.data?.accessToken) {
            this.persistUser(response.data.accessToken, response.data.refreshToken);
            console.log(response.data);
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
        console.log(response.data);
        return response.data;
    }

    getCurrentUser() {
        const raw = localStorage.getItem(USER_STORAGE_KEY);
        console.log('Getting current user from localStorage:', raw);
        if (!raw) {
            return null;
        }

        try {
            const stored = JSON.parse(raw);
            // roles가 비어있거나 누락된 경우 토큰에서 재생성
            if (!stored?.roles?.length && stored?.accessToken) {
                const decoded = this.decodeToken(stored.accessToken);
                const rawRoles = decoded?.roles || decoded?.authorities || decoded?.scope || decoded?.role;
                const rolesArray = Array.isArray(rawRoles)
                    ? rawRoles
                    : rawRoles
                        ? String(rawRoles).split(',').map((r) => r.trim())
                        : [];
                stored.roles = rolesArray;
                localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(stored));
            }
            return stored;
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
        const rawRoles = decoded?.roles || decoded?.authorities || decoded?.scope || decoded?.role;
        const rolesArray = Array.isArray(rawRoles)
            ? rawRoles
            : rawRoles
                ? String(rawRoles).split(',').map((r) => r.trim())
                : [];

        const user = {
            accessToken,
            refreshToken,
            email: decoded?.sub ?? null,
            username: decoded?.username ?? decoded?.sub ?? null,
            roles: rolesArray,
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
