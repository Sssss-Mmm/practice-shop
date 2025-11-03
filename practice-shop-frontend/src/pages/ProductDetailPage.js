import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ProductService from '../services/product.service';
import AuthService from '../services/auth.service';
import './ProductDetailPage.css';

const ProductDetailPage = () => {
    const { productId } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [error, setError] = useState(null);
    const [isSellerOrAdmin, setIsSellerOrAdmin] = useState(false);

    useEffect(() => {
        const user = AuthService.getCurrentUser();
        ProductService.getProductById(productId).then(
            (response) => {
                setProduct(response.data);
                if (
                    user &&
                    (user.email === response.data.sellerEmail ||
                        (user.roles && user.roles.includes('ADMIN')))
                ) {
                    setIsSellerOrAdmin(true);
                }
            },
            (error) => {
                setError(error.response ? error.response.data.message : error.message);
            }
        );
    }, [productId]);

    const handleEdit = () => {
        navigate(`/products/${productId}/edit`);
    };

    if (error) {
        return <div className="error-message">Error: {error}</div>;
    }

    if (!product) {
        return <div>Loading...</div>;
    }

    return (
        <div className="product-detail-container">
            <div className="product-image-section">
                <img src={`http://localhost:8084${product.imageUrl}`} alt={product.productName} className="product-detail-image" />
            </div>
            <div className="product-info-section">
                <h1 className="product-detail-name">{product.productName}</h1>
                <p className="product-detail-description">{product.description}</p>
                <p className="product-detail-price">{product.price}원</p>
                <button className="add-to-cart-button">Add to Cart</button>
                {isSellerOrAdmin && (
                    <button className="edit-product-button" onClick={handleEdit}>Edit Product</button>
                )}
            </div>
        </div>
    );
};

export default ProductDetailPage;
