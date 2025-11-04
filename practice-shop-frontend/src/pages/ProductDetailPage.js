import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ProductService from '../services/product.service';
import AuthService from '../services/auth.service';
import CartService from '../services/cart.service';
import './ProductDetailPage.css';

const ProductDetailPage = () => {
    const { productId } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [error, setError] = useState(null);
    const [quantity, setQuantity] = useState(1);
    const [isSellerOrAdmin, setIsSellerOrAdmin] = useState(false);
    const [message, setMessage] = useState('');
    const [actionPending, setActionPending] = useState(false);

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

    const handleDelete = async () => {
        if (!window.confirm('정말로 이 상품을 삭제하시겠습니까?')) {
            return;
        }

        setActionPending(true);
        setMessage('');
        try {
            await ProductService.deleteProduct(productId);
            setMessage('상품이 삭제되었습니다.');
            navigate('/', { replace: true });
        } catch (err) {
            const resMessage =
                (err.response && err.response.data && err.response.data.message) ||
                err.message ||
                err.toString();
            setMessage(resMessage);
        } finally {
            setActionPending(false);
        }
    };

    const handleAddToCart = () => {
        if (!product) {
            return;
        }
        const sanitizedQty = Math.max(1, Number(quantity) || 1);
        setQuantity(sanitizedQty);
        setActionPending(true);
        setMessage('');
        CartService.addItem(product.id, sanitizedQty)
            .then(() => {
                setMessage('장바구니에 담았습니다.');
            })
            .catch((err) => {
                const status = err.response?.status;
                const resMessage =
                    status === 401
                        ? '로그인이 필요합니다.'
                        : (err.response && err.response.data && err.response.data.message) ||
                          err.message ||
                          err.toString();
                setMessage(resMessage);
            })
            .finally(() => setActionPending(false));
    };
    if (error) {
        return <div className="error-message">Error: {error}</div>;
    }

    if (!product) {
        return <div>Loading...</div>;
    }

    const resolveImageUrl = (relativeUrl) => {
        if (!relativeUrl) {
            return '/placeholder.png';
        }

        const encodedPath = encodeURI(relativeUrl);
        if (encodedPath.startsWith('http://') || encodedPath.startsWith('https://')) {
            return encodedPath;
        }

        const apiBase = process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8084';
        return `${apiBase}${encodedPath}`;
    };

    return (
        <div className="product-detail-container">
            <div className="product-image-section">
                <img
                    src={resolveImageUrl(product.imageUrl)}
                    alt={product.productName}
                    className="product-detail-image"
                />
            </div>
            <div className="product-info-section">
                <h1 className="product-detail-name">{product.productName}</h1>
                <p className="product-detail-description">{product.description}</p>
                <p className="product-detail-price">{product.price}원</p>
                <div className="product-quantity">
                    <label htmlFor="quantity">수량</label>
                    <input
                        id="quantity"
                        type="number"
                        min="1"
                        value={quantity}
                        onChange={(e) => setQuantity(Math.max(1, Number(e.target.value) || 1))}
                        disabled={actionPending}
                    />
                </div>
                <button className="add-to-cart-button" onClick={handleAddToCart} disabled={actionPending}>
                    장바구니에 담기
                </button>
                {isSellerOrAdmin && (
                    <div className="product-admin-actions">
                        <button className="edit-product-button" onClick={handleEdit} disabled={actionPending}>
                            Edit Product
                        </button>
                        <button className="delete-product-button" onClick={handleDelete} disabled={actionPending}>
                            Delete Product
                        </button>
                    </div>
                )}
                {message && (
                    <div className="product-message">
                        {message}
                    </div>
                )}
            </div>
        </div>
    );
};

export default ProductDetailPage;
