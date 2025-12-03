import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ProductService from '../services/product.service';
import { useAuth } from '../context/AuthContext';
import './HomePage.css';
import { FaSearch } from 'react-icons/fa';

/**
 * 메인 홈페이지 컴포넌트입니다.
 * 공연 검색 기능과 현재 진행중인 공연 목록을 보여줍니다.
 * @returns {JSX.Element} HomePage 컴포넌트
 */
const HomePage = () => {
    const { currentUser } = useAuth();
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');

    /**
     * 컴포넌트가 마운트될 때 모든 공연(상품) 목록을 서버에서 가져옵니다.
     * 성공 시 products 상태를 업데이트하고, 실패 시 에러 상태를 설정합니다.
     */
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

    /**
     * 상대적인 이미지 URL을 절대 URL로 변환합니다.
     * 환경 변수에 설정된 API 기본 URL을 사용하며, URL이 없는 경우 플레이스홀더 이미지를 반환합니다.
     * @param {string} relativeUrl - 변환할 상대 이미지 URL
     * @returns {string} 절대 이미지 URL
     */
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

    /**
     * 'products' 배열에서 'searchTerm'을 기준으로 공연 이름을 필터링합니다.
     */
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
                    filteredProducts.length > 0 ? (
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
                    ) : (
                        <div className="text-center py-5">
                            <p className="text-muted mb-3">현재 표시할 공연/상품이 없습니다.</p>
                            {currentUser?.roles?.includes('ADMIN') || currentUser?.roles?.includes('ROLE_ADMIN') ? (
                                <Link to="/product-registration" className="btn btn-primary">공연/상품 등록하기</Link>
                            ) : (
                                <Link to="/" className="btn btn-outline-secondary">새로고침</Link>
                            )}
                        </div>
                    )
                )}
            </main>
        </div>
    );
};

export default HomePage;
