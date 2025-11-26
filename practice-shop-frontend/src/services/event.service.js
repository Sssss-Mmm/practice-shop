import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/events';
const authConfig = () => ({ headers: AuthService.authHeader() });

const listEvents = (status) => {
    const params = status ? `?status=${status}` : '';
    return axios.get(`${API_URL}${params}`, authConfig());
};

const getEvent = (eventId) => axios.get(`${API_URL}/${eventId}`, authConfig());
const createEvent = (payload) => axios.post(API_URL, payload, authConfig());

const EventService = {
    listEvents,
    getEvent,
    createEvent,
};

export default EventService;
