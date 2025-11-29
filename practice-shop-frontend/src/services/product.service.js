import axios from "axios";
import AuthService from "./auth.service";

const API_URL = "/api/products";

const getProducts = (page = 0, size = 12) => {
    const params = new URLSearchParams({ page, size });
    return axios.get(`${API_URL}?${params.toString()}`, { headers: AuthService.authHeader() });
};
const getAllProducts = () => {
    return axios.get(`${API_URL}`, { headers: AuthService.authHeader() });
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

const deleteProduct = (productId) => {
    return axios.delete(`${API_URL}/${productId}`, {
        headers: AuthService.authHeader(),
    });
};

const ProductService = {
    getProducts,
    getProductById,
    registerProduct,
    updateProduct,
    deleteProduct,
    getAllProducts
};

export default ProductService;
