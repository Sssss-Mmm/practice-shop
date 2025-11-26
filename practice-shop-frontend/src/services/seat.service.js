import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/seats';
const authConfig = () => ({ headers: AuthService.authHeader() });

const listByVenue = (venueId) => axios.get(`${API_URL}/venue/${venueId}`, authConfig());
const createSeat = (payload) => axios.post(API_URL, payload, authConfig());

const SeatService = {
    listByVenue,
    createSeat,
};

export default SeatService;
