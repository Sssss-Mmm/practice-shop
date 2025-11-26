import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/showtimes';
const authConfig = () => ({ headers: AuthService.authHeader() });

const getShowtime = (showtimeId) => axios.get(`${API_URL}/${showtimeId}`, authConfig());
const listByEvent = (eventId) => axios.get(`${API_URL}/event/${eventId}`, authConfig());
const createShowtime = (payload) => axios.post(API_URL, payload, authConfig());

const ShowtimeService = {
    getShowtime,
    listByEvent,
    createShowtime,
};

export default ShowtimeService;
