import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/ticketing';
const authConfig = () => ({ headers: AuthService.authHeader() });

const reserveSeats = (payload) => {
    return axios.post(`${API_URL}/reserve`, payload, authConfig());
};

const getMyReservations = () => {
    return axios.get(`${API_URL}/reservations`, authConfig());
};

const cancelReservation = (reservationId) => {
    return axios.post(`${API_URL}/reservations/${reservationId}/cancel`, {}, authConfig());
};

const TicketingService = {
    reserveSeats,
    getMyReservations,
    cancelReservation,
};

export default TicketingService;
