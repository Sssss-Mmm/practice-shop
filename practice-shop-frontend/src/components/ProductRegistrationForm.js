import React, { useState } from "react";
import ProductService from "../services/product.service";

const ProductRegistrationForm = () => {
    const [productName, setProductName] = useState("");
    const [price, setPrice] = useState(0);
    const [description, setDescription] = useState("");
    const [status, setStatus] = useState("AVAILABLE"); // Default status
    const [imageFile, setImageFile] = useState(null);
    const [message, setMessage] = useState("");

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

        ProductService.registerProduct(formData).then(
            () => {
                setProductName("");
                setPrice(0);
                setDescription("");
                setStatus("AVAILABLE");
                setImageFile(null);
                setMessage("상품이 성공적으로 등록되었습니다.");
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

    return (
        <form onSubmit={handleSubmit}>
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
                <label htmlFor="imageFile">이미지</label>
                <input
                    type="file"
                    id="imageFile"
                    onChange={(e) => setImageFile(e.target.files[0])}
                />
            </div>
            <button type="submit">등록</button>

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

export default ProductRegistrationForm;
