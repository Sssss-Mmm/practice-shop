import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ProductService from '../services/product.service';
import './HomePage.css';

const HomePage = () => {
    const [products, setProducts] = useState([]);

    useEffect(() => {
        ProductService.getProducts().then(
            (response) => {
                setProducts(response.data);
            },
            (error) => {
                console.log(error);
            }
        );
    }, []);

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
                            <img src={`http://localhost:8084${product.imageUrl}`} alt={product.productName} className="product-image" />
                            <div className="product-info">
                                <h5 className="product-name">{product.productName}</h5>
                                <p className="product-price">{product.price}원</p>
                            </div>
                        </div>
                    </Link>
                ))}
            </div>
        </div>
    );
};

export default HomePage;