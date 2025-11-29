import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ProductService from '../services/product.service';
import './HomePage.css';
import { FaSearch } from 'react-icons/fa';

const HomePage = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        setLoading(true);
        ProductService.getAllProducts()
            .then((response) => {
                // 백엔드에서 받은 데이터가 페이지네이션 객체일 경우, content 배열을 사용합니다.
                // 그렇지 않고 단순 배열일 경우를 대비하여 response.data가 배열인지 확인합니다.
                const productData = Array.isArray(response.data) ? response.data : response.data.content;
                setProducts(productData || []);
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
    }, []);

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

    const filteredProducts = products.filter(product =>
        product.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="homepage-container">
            <section className="hero-section">
                <div className="search-container">
                    <h1>어떤 공연을 찾으시나요?</h1>
                    <div className="search-bar">
                        <FaSearch className="search-icon" />
                        <input
                            type="text"
                            placeholder="공연, 아티스트 또는 장소를 검색하세요."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                        <button>검색</button>
                    </div>
                </div>
            </section>

            <main className="container my-5">
                <h2 className="section-title">진행중인 공연</h2>

                {loading && (
                    <div className="text-center">
                        <div className="spinner-border text-primary" role="status">
                            <span className="visually-hidden">Loading...</span>
                        </div>
                    </div>
                )}
                {error && <div className="alert alert-danger">{error}</div>}

                {!loading && !error && (
                    <div className="row">
                        {filteredProducts.map((product) => (
                            <div className="col-md-4 col-lg-3 mb-4" key={product.id}>
                                <Link to={`/products/${product.id}`} className="card-link">
                                    <div className="card h-100 product-card">
                                        <img src={resolveImageUrl(product.imageUrls[0])} className="card-img-top" alt={product.name} />
                                        <div className="card-body">
                                            <h5 className="card-title">{product.name}</h5>
                                            <p className="card-text">{product.venueName || '장소 정보 없음'}</p>
                                            <p className="card-text price">{product.price.toLocaleString()}원</p>
                                        </div>
                                    </div>
                                </Link>
                            </div>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
};

export default HomePage;