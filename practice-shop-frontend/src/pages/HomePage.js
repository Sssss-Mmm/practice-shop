import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ProductService from '../services/product.service';
import './HomePage.css';

const HomePage = () => {
    const [products, setProducts] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const pageSize = 12;

    useEffect(() => {
        loadProducts(0);
    }, []);

    const loadProducts = (targetPage) => {
        ProductService.getProducts(targetPage, pageSize).then(
            (response) => {
                const data = response.data;
                if (Array.isArray(data)) {
                    setProducts(data);
                    setPage(0);
                    setTotalPages(1);
                } else {
                    setProducts(data.content ?? []);
                    setPage(data.page ?? targetPage);
                    const total = data.totalPages ?? 1;
                    setTotalPages(total > 0 ? total : 1);
                }
            },
            (error) => {
                console.log(error);
            }
        );
    };

    const handlePageChange = (nextPage) => {
        if (nextPage < 0 || nextPage >= totalPages) {
            return;
        }
        loadProducts(nextPage);
    };

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
        <div className="homepage-container">
            <div className="promotional-banner">
                <h1>Welcome to Safian Shop</h1>
                <p>The best products for you and your family.</p>
            </div>

            <div className="product-grid">
                {products.map((product) => (
                    <Link to={`/products/${product.id}`} key={product.id} className="product-card-link">
                        <div className="product-card">
                            <img
                                src={resolveImageUrl(product.imageUrl)}
                                alt={product.productName}
                                className="product-image"
                            />
                            <div className="product-info">
                                <h5 className="product-name">{product.productName}</h5>
                                <p className="product-price">{product.price}원</p>
                            </div>
                        </div>
                    </Link>
                ))}
            </div>
            {totalPages > 1 && (
                <div className="pagination-controls">
                    <button
                        disabled={page === 0}
                        onClick={() => handlePageChange(page - 1)}
                    >
                        이전
                    </button>
                    <span>{page + 1} / {totalPages}</span>
                    <button
                        disabled={page >= totalPages - 1}
                        onClick={() => handlePageChange(page + 1)}
                    >
                        다음
                    </button>
                </div>
            )}
        </div>
    );
};

export default HomePage;
