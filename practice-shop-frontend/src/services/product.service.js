import axios from "axios";
import AuthService from "./auth.service";

const API_URL = "/api/products";

const getProducts = () => {
    return axios.get(API_URL, { headers: AuthService.authHeader() });
};

const getProductById = (productId) => {
    return axios.get(`${API_URL}/${productId}`, { headers: AuthService.authHeader() });
};

const registerProduct = (formData) => {
    return axios.post(API_URL, formData, {
        headers: {
            ...AuthService.authHeader(),
            "Content-Type": "multipart/form-data",
        },
    });
};

const updateProduct = (productId, formData) => {
    return axios.put(`${API_URL}/${productId}`, formData, {
        headers: {
            ...AuthService.authHeader(),
            "Content-Type": "multipart/form-data",
        },
    });
};

const ProductService = {
    getProducts,
    getProductById,
    registerProduct,
    updateProduct,
};

export default ProductService;
