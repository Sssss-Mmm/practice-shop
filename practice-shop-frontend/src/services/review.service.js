import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/reviews';
const authConfig = () => ({ headers: AuthService.authHeader() });

const createReview = (eventId, payload) => {
    return axios.post(`${API_URL}/event/${eventId}`, payload, authConfig());
};

const listReviews = (eventId) => {
    return axios.get(`${API_URL}/event/${eventId}`, authConfig());
};

const deleteReview = (reviewId) => {
    return axios.delete(`${API_URL}/${reviewId}`, authConfig());
};

const ReviewService = {
    createReview,
    listReviews,
    deleteReview,
};

export default ReviewService;
