import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/events';
const authConfig = () => ({ headers: AuthService.authHeader() });

const listEvents = (params) => {
    const config = authConfig();
    if (typeof params === 'string') {
        config.params = { status: params };
    } else if (params) {
        config.params = params;
    }
    return axios.get(API_URL, config);
};

const getEvent = (eventId) => axios.get(`${API_URL}/${eventId}`, authConfig());
const createEvent = (payload) => axios.post(API_URL, payload, authConfig());

const EventService = {
    listEvents,
    getEvent,
    createEvent,
};

export default EventService;
