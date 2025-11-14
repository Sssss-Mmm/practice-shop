import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/payments';
const authConfig = () => ({ headers: AuthService.authHeader() });

const confirmTossPayment = (payload) => {
    return axios.post(`${API_URL}/toss/confirm`, payload, authConfig());
};

const PaymentService = {
    confirmTossPayment,
};

export default PaymentService;
