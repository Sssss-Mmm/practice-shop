import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ProductService from '../services/product.service';
import AuthService from '../services/auth.service';
import './ProductDetailPage.css';

const ProductDetailPage = () => {
    const { productId } = useParams();
    const navigate = useNavigate();
    const [event, setEvent] = useState(null);
    const [error, setError] = useState(null);
    const [isSellerOrAdmin, setIsSellerOrAdmin] = useState(false);
    const [message, setMessage] = useState('');
    const [actionPending, setActionPending] = useState(false);

    useEffect(() => {
        const user = AuthService.getCurrentUser();
        ProductService.getProductById(productId).then(
            (response) => {
                setEvent(response.data);
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
    
    const handleBooking = () => {
        navigate(`/events/${productId}`);
    };

    if (error) {
        return <div className="error-message">Error: {error}</div>;
    }

    if (!event) {
        return <div className="loading-container"><div>Loading...</div></div>;
    }

    const resolveImageUrl = (relativeUrl) => {
        if (!relativeUrl) {
            return '/placeholder.png'; // A default placeholder image
        }

        const encodedPath = encodeURI(relativeUrl);
        if (encodedPath.startsWith('http://') || encodedPath.startsWith('https://')) {
            return encodedPath;
        }
        
        const apiBase = process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8084';
        return `${apiBase}${encodedPath}`;
    };

    return (
        <div className="event-detail-page">
            <div className="event-main-container">
                <div className="event-image-wrapper">
                    <img
                        src={resolveImageUrl(event.imageUrl)}
                        alt={event.productName}
                        className="event-detail-image"
                    />
                </div>
                <div className="event-info-wrapper">
                    <h1 className="event-detail-title">{event.productName}</h1>
                    <div className="event-detail-meta">
                        <p><strong>장소:</strong> {event.venue || '미정'}</p>
                        <p><strong>기간:</strong> {event.startDate || '미정'} ~ {event.endDate || '미정'}</p>
                        <p className="event-detail-price">
                            <strong>가격:</strong> <span>{event.price.toLocaleString()}원</span>
                        </p>
                    </div>
                    <button className="booking-button" onClick={handleBooking} disabled={actionPending}>
                        예매하기
                    </button>
                    {isSellerOrAdmin && (
                        <div className="admin-actions">
                            <button className="edit-button" onClick={handleEdit} disabled={actionPending}>
                                수정
                            </button>
                            <button className="delete-button" onClick={handleDelete} disabled={actionPending}>
                                삭제
                            </button>
                        </div>
                    )}
                    {message && <div className="action-message">{message}</div>}
                </div>
            </div>

            <div className="event-description-container">
                <h2>공연 상세 정보</h2>
                <p className="event-detail-description">{event.description}</p>
            </div>
        </div>
    );
};

export default ProductDetailPage;
