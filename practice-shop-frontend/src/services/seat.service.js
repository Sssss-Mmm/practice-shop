import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/seats';
const authConfig = () => ({ headers: AuthService.authHeader() });

const listByVenue = (venueId) => axios.get(`${API_URL}/venue/${venueId}`, authConfig());
// 회차별 현재 좌석 상태 (예매됨 여부 포함). 이후 변경은 WebSocket으로 받는다.
const listByShowtime = (showtimeId) => axios.get(`/api/showtimes/${showtimeId}/seats/realtime`, authConfig());
const createSeat = (payload) => axios.post(API_URL, payload, authConfig());

const SeatService = {
    listByVenue,
    listByShowtime,
    createSeat,
};

export default SeatService;
