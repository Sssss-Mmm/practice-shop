import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ProductService from '../services/product.service';
import CartService from '../services/cart.service';
import './ProductDetailPage.css';
import { FaCalendarAlt, FaMapMarkerAlt, FaClock, FaWonSign, FaInfoCircle } from 'react-icons/fa';

const ProductDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeTab, setActiveTab] = useState('info');

    useEffect(() => {
        setLoading(true);
        ProductService.getProductById(id)
            .then((response) => {
                setProduct(response.data);
                setError(null);
            })
            .catch((err) => {
                const resMessage =
                    (err.response && err.response.data && err.response.data.message) ||
                    err.message ||
                    err.toString();
                setError(resMessage);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [id]);

    const handleAddToCart = () => {
        CartService.addToCart(product.id, 1)
            .then(() => {
                navigate('/cart');
            })
            .catch((err) => {
                setError(err.response?.data?.message || '장바구니에 추가하는 중 오류가 발생했습니다.');
            });
    };

    const resolveImageUrl = (relativeUrl) => {
        if (!relativeUrl) return '/placeholder.png';
        const encodedPath = encodeURI(relativeUrl);
        if (encodedPath.startsWith('http://') || encodedPath.startsWith('https://')) {
            return encodedPath;
        }
        const apiBase = process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8084';
        return `${apiBase}${encodedPath}`;
    };

    if (loading) {
        return (
            <div className="container my-5 text-center">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
                <p className="mt-2">공연 정보를 불러오는 중입니다...</p>
            </div>
        );
    }

    if (error) {
        return <div className="container my-5 alert alert-danger">{error}</div>;
    }

    if (!product) {
        return <div className="container my-5 alert alert-warning">공연 정보를 찾을 수 없습니다.</div>;
    }

    return (
        <div className="product-detail-container container my-5">
            <div className="row">
                <div className="col-md-5">
                    <img src={resolveImageUrl(product.imageUrls[0])} alt={product.name} className="product-poster" />
                </div>
                <div className="col-md-7">
                    <div className="product-info-header">
                        <span className="badge bg-primary product-category">{product.category || '공연'}</span>
                        <h1>{product.name}</h1>
                    </div>
                    <div className="product-info-list">
                        <p><FaMapMarkerAlt className="info-icon" /> <strong>장소:</strong> {product.venueName || '미정'}</p>
                        <p><FaCalendarAlt className="info-icon" /> <strong>기간:</strong> {product.performancePeriod || '상시'}</p>
                        <p><FaClock className="info-icon" /> <strong>관람시간:</strong> {product.runningTime || '미정'}</p>
                        <p><FaInfoCircle className="info-icon" /> <strong>관람연령:</strong> {product.ageRating || '전체관람가'}</p>
                        <p className="price"><FaWonSign className="info-icon" /> <strong>가격:</strong> {product.price.toLocaleString()}원</p>
                    </div>
                    <div className="d-grid gap-2">
                        <button onClick={handleAddToCart} className="btn btn-danger btn-lg btn-booking">예매하기</button>
                    </div>
                </div>
            </div>

            <div className="product-detail-tabs mt-5">
                <ul className="nav nav-tabs">
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === 'info' ? 'active' : ''}`} onClick={() => setActiveTab('info')}>공연정보</button>
                    </li>
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === 'casting' ? 'active' : ''}`} onClick={() => setActiveTab('casting')}>캐스팅</button>
                    </li>
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === 'venue' ? 'active' : ''}`} onClick={() => setActiveTab('venue')}>장소/교통</button>
                    </li>
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === 'notice' ? 'active' : ''}`} onClick={() => setActiveTab('notice')}>예매/취소 안내</button>
                    </li>
                </ul>
                <div className="tab-content p-3">
                    {activeTab === 'info' && (
                        <div className="tab-pane-content">
                            <h3>공연 상세 정보</h3>
                            <p>{product.description}</p>
                            {product.imageUrls.slice(1).map((url, index) => (
                                <img key={index} src={resolveImageUrl(url)} alt={`Detail ${index + 1}`} className="img-fluid my-3" />
                            ))}
                        </div>
                    )}
                    {activeTab === 'casting' && (
                        <div className="tab-pane-content">
                            <h3>캐스팅 정보</h3>
                            <p>캐스팅 정보가 준비 중입니다.</p>
                        </div>
                    )}
                    {activeTab === 'venue' && (
                        <div className="tab-pane-content">
                            <h3>장소 정보</h3>
                            <p>{product.venueName || '장소 정보가 없습니다.'}</p>
                            {/* 여기에 지도 등을 추가할 수 있습니다. */}
                        </div>
                    )}
                    {activeTab === 'notice' && (
                        <div className="tab-pane-content">
                            <h3>예매/취소 안내</h3>
                            <p>예매 및 취소 관련 규정은 각 판매처의 정책에 따릅니다. 예매 전 반드시 확인해주시기 바랍니다.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ProductDetailPage;