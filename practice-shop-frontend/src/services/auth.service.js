import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

const API_URL = '/api/auth/';

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

    signup(username, email, password) {
        return axios.post(API_URL + 'signup', {
            username,
            email,
            password
        });
    }

    getCurrentUser() {
        return JSON.parse(localStorage.getItem('user'));
    }
}

export default new AuthService();
