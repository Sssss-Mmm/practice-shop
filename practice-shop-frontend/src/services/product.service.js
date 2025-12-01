import axios from "axios";
import AuthService from "./auth.service";

const API_URL = "/api/products";

/**
 * 모든 상품(공연) 목록을 서버에서 가져옵니다.
 * @returns {Promise<AxiosResponse<any>>} Axios 응답 프로미스
 */
const getAllProducts = () => {
    return axios.get(`${API_URL}`, { headers: AuthService.authHeader() });
};

/**
 * 특정 ID를 가진 상품(공연)의 상세 정보를 서버에서 가져옵니다.
 * @param {number|string} productId - 조회할 상품의 ID
 * @returns {Promise<AxiosResponse<any>>} Axios 응답 프로미스
 */
const getProductById = (productId) => {
    return axios.get(`${API_URL}/${productId}`, { headers: AuthService.authHeader() });
};

/**
 * 새로운 상품(공연)을 서버에 등록합니다.
 * @param {FormData} formData - 상품 정보와 이미지 파일이 포함된 FormData 객체
 * @returns {Promise<AxiosResponse<any>>} Axios 응답 프로미스
 */
const registerProduct = (formData) => {
    return axios.post(API_URL, formData, {
        headers: {
            ...AuthService.authHeader(),
            "Content-Type": "multipart/form-data",
        },
    });
};

/**
 * 기존 상품(공연) 정보를 업데이트합니다.
 * @param {number|string} productId - 수정할 상품의 ID
 * @param {FormData} formData - 수정할 상품 정보가 포함된 FormData 객체
 * @returns {Promise<AxiosResponse<any>>} Axios 응답 프로미스
 */
const updateProduct = (productId, formData) => {
    return axios.put(`${API_URL}/${productId}`, formData, {
        headers: {
            ...AuthService.authHeader(),
            "Content-Type": "multipart/form-data",
        },
    });
};

/**
 * 특정 ID를 가진 상품(공연)을 서버에서 삭제합니다.
 * @param {number|string} productId - 삭제할 상품의 ID
 * @returns {Promise<AxiosResponse<any>>} Axios 응답 프로미스
 */
const deleteProduct = (productId) => {
    return axios.delete(`${API_URL}/${productId}`, {
        headers: AuthService.authHeader(),
    });
};

// ProductService 객체 정의
const ProductService = {
    getProductById,
    registerProduct,
    updateProduct,
    deleteProduct,
    getAllProducts
};

export default ProductService;
