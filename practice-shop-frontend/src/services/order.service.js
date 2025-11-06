import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/orders';

const authConfig = () => ({ headers: AuthService.authHeader() });

const createOrder = (payload) => {
    return axios.post(API_URL, payload, authConfig());
};

const getOrders = (page = 0, size = 10) => {
    const params = new URLSearchParams({ page, size });
    return axios.get(`${API_URL}?${params.toString()}`, authConfig());
};

const getOrderDetail = (orderId) => {
    return axios.get(`${API_URL}/${orderId}`, authConfig());
};

const OrderService = {
    createOrder,
    getOrders,
    getOrderDetail,
};

export default OrderService;
