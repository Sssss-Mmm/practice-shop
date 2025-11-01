import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const API_URL = '/auth/';

class AuthService {
    login(email, password) {
        return axios
            .post(API_URL + 'login', {
                email,
                password
            })
            .then(response => {
                if (response.data.accessToken) {
                    const decodedToken = jwtDecode(response.data.accessToken);
                    const user = {
                        accessToken: response.data.accessToken,
                        email: decodedToken.sub, // Assuming email is in the 'sub' claim
                        username: decodedToken.username // Assuming username is in the 'username' claim
                    };
                    localStorage.setItem('user', JSON.stringify(user));
                }
                return response.data;
            });
    }

    logout() {
        localStorage.removeItem('user');
    }

    signup(email, password, name, phoneNumber, nickname, region, address, gender, birthDate) {
        return axios.post(API_URL + 'signup', {
            email,
            password,
            name,
            phoneNumber,
            nickname,
            region,
            address,
            gender,
            birthDate
        });
    }

    completeOAuth2Registration(temporaryToken, name, phoneNumber, nickname, region, address, gender, birthDate) {
        return axios.post(API_URL + 'oauth2/register', {
            temporaryToken,
            name,
            phoneNumber,
            nickname,
            region,
            address,
            gender,
            birthDate
        }).then(response => {
            // Assuming the backend returns accessToken and refreshToken directly
            return response.data;
        });
    }

    getCurrentUser() {
        return JSON.parse(localStorage.getItem('user'));
    }

    getUserProfile() {
        return axios.get('/api/users/profile', { headers: this.authHeader() });
    }

    updateUserProfile(profile) {
        return axios.put('/api/users/profile', profile, { headers: this.authHeader() });
    }

    authHeader() {
        const user = this.getCurrentUser();
        if (user && user.accessToken) {
            return { Authorization: 'Bearer ' + user.accessToken };
        } else {
            return {};
        }
    }
}

export default new AuthService();
