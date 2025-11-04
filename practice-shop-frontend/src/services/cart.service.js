import axios from 'axios';
import AuthService from './auth.service';

const API_URL = '/api/cart';
const CART_EVENT = 'cart-change';

const authConfig = () => ({ headers: AuthService.authHeader() });

const notify = (payload) => {
    window.dispatchEvent(new CustomEvent(CART_EVENT, { detail: payload }));
};

const getCart = () => {
    return axios.get(API_URL, authConfig()).then((response) => {
        notify(response.data);
        return response;
    });
};

const addItem = (productId, quantity) => {
    return axios.post(API_URL, { productId, quantity }, authConfig()).then((response) => {
        notify(response.data);
        return response;
    });
};

const updateItem = (cartItemId, quantity) => {
    return axios.put(`${API_URL}/items/${cartItemId}`, { quantity }, authConfig()).then((response) => {
        notify(response.data);
        return response;
    });
};

const removeItem = (cartItemId) => {
    return axios.delete(`${API_URL}/items/${cartItemId}`, authConfig()).then((response) => {
        notify(response.data);
        return response;
    });
};

const clearCart = () => {
    return axios.delete(API_URL, authConfig()).then((response) => {
        notify(response.data);
        return response;
    });
};

const onChange = (callback) => {
    if (typeof callback !== 'function') {
        return () => {};
    }
    const handler = (event) => callback(event.detail);
    window.addEventListener(CART_EVENT, handler);
    return () => window.removeEventListener(CART_EVENT, handler);
};

const CartService = {
    getCart,
    addItem,
    updateItem,
    removeItem,
    clearCart,
    onChange,
    CART_EVENT,
};

export default CartService;
