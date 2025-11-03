import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import ProductService from "../services/product.service";

const ProductEditPage = () => {
    const { productId } = useParams();
    const navigate = useNavigate();
    const [productName, setProductName] = useState("");
    const [price, setPrice] = useState(0);
    const [description, setDescription] = useState("");
    const [status, setStatus] = useState("AVAILABLE");
    const [imageFile, setImageFile] = useState(null);
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        ProductService.getProductById(productId).then(
            (response) => {
                const product = response.data;
                setProductName(product.productName);
                setPrice(product.price);
                setDescription(product.description);
                setStatus(product.status);
                setLoading(false);
            },
            (error) => {
                setError(error.response ? error.response.data.message : error.message);
                setLoading(false);
            }
        );
    }, [productId]);

    const handleSubmit = (e) => {
        e.preventDefault();
        setMessage("");

        const formData = new FormData();
        formData.append("request", new Blob([JSON.stringify({
            productName,
            price,
            description,
            status,
        })], { type: "application/json" }));
        if (imageFile) {
            formData.append("imageFile", imageFile);
        }

        ProductService.updateProduct(productId, formData).then(
            () => {
                setMessage("상품이 성공적으로 수정되었습니다.");
                navigate(`/products/${productId}`);
            },
            (error) => {
                const resMessage =
                    (error.response &&
                        error.response.data &&
                        error.response.data.message) ||
                    error.message ||
                    error.toString();

                setMessage(resMessage);
            }
        );
    };

    if (loading) {
        return <div>Loading product for edit...</div>;
    }

    if (error) {
        return <div className="error-message">Error: {error}</div>;
    }

    return (
        <form onSubmit={handleSubmit}>
            <h1>상품 수정</h1>
            <div>
                <label htmlFor="productName">상품명</label>
                <input
                    type="text"
                    id="productName"
                    value={productName}
                    onChange={(e) => setProductName(e.target.value)}
                />
            </div>
            <div>
                <label htmlFor="price">가격</label>
                <input
                    type="number"
                    id="price"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                />
            </div>
            <div>
                <label htmlFor="description">설명</label>
                <textarea
                    id="description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                ></textarea>
            </div>
            <div>
                <label htmlFor="status">상태</label>
                <select
                    id="status"
                    value={status}
                    onChange={(e) => setStatus(e.target.value)}
                >
                    <option value="AVAILABLE">판매중</option>
                    <option value="OUT_OF_STOCK">품절</option>
                    <option value="DISCONTINUED">단종</option>
                </select>
            </div>
            <div>
                <label htmlFor="imageFile">이미지 (변경 시)</label>
                <input
                    type="file"
                    id="imageFile"
                    onChange={(e) => setImageFile(e.target.files[0])}
                />
            </div>
            <button type="submit">수정</button>

            {message && (
                <div className="form-group">
                    <div className="alert alert-info" role="alert">
                        {message}
                    </div>
                </div>
            )}
        </form>
    );
};

export default ProductEditPage;
