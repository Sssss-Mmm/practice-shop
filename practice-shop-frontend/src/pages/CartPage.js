import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import CartService from '../services/cart.service';
import './CartPage.css';

const CartPage = () => {
    const [cart, setCart] = useState({ items: [], totalItems: 0, totalPrice: 0 });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

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

    const loadCart = () => {
        setLoading(true);
        CartService.getCart()
            .then((response) => {
                setCart(response.data ?? { items: [], totalItems: 0, totalPrice: 0 });
                setError(null);
            })
            .catch((err) => {
                const status = err.response?.status;
                const resMessage =
                    status === 401
                        ? '장바구니는 로그인 후 이용할 수 있습니다.'
                        : (err.response && err.response.data && err.response.data.message) ||
                          err.message ||
                          err.toString();
                setError(resMessage);
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        loadCart();
        const unsubscribe = CartService.onChange((payload) => {
            if (payload) {
                setCart(payload);
            } else {
                loadCart();
            }
        });
        return () => {
            if (typeof unsubscribe === 'function') {
                unsubscribe();
            }
        };
    }, []);

    const handleQuantityChange = (cartItemId, value) => {
        const qty = Math.max(1, Number(value) || 1);
        CartService.updateItem(cartItemId, qty).catch(() => loadCart());
    };

    const handleRemove = (cartItemId) => {
        CartService.removeItem(cartItemId).catch(() => loadCart());
    };

    const handleClear = () => {
        if (window.confirm('장바구니를 비우시겠습니까?')) {
            CartService.clearCart().catch(() => loadCart());
        }
    };

    if (loading) {
        return (
            <div className="cart-container">
                <h1>장바구니</h1>
                <p>장바구니 정보를 불러오는 중입니다...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="cart-container">
                <h1>장바구니</h1>
                <p className="cart-error">{error}</p>
                {error.includes('로그인') && (
                    <button className="cart-button" onClick={() => navigate('/login')}>
                        로그인하기
                    </button>
                )}
            </div>
        );
    }

    if (!cart.items || cart.items.length === 0) {
        return (
            <div className="cart-container">
                <h1>장바구니</h1>
                <p>장바구니가 비어 있습니다.</p>
                <button className="cart-button" onClick={() => navigate('/')}>쇼핑 계속하기</button>
            </div>
        );
    }

    return (
        <div className="cart-container">
            <h1>장바구니</h1>
            <table className="cart-table">
                <thead>
                    <tr>
                        <th>상품</th>
                        <th>가격</th>
                        <th>수량</th>
                        <th>합계</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    {cart.items.map((item) => (
                        <tr key={item.cartItemId}>
                            <td className="cart-product-info">
                                <img src={resolveImageUrl(item.imageUrl)} alt={item.productName} />
                                <span>{item.productName}</span>
                            </td>
                            <td>{item.price.toLocaleString()}원</td>
                            <td>
                                <input
                                    type="number"
                                    min="1"
                                    value={item.quantity}
                                    onChange={(e) => handleQuantityChange(item.cartItemId, e.target.value)}
                                />
                            </td>
                            <td>{Number(item.subtotal ?? item.price * item.quantity).toLocaleString()}원</td>
                            <td>
                                <button className="cart-remove-button" onClick={() => handleRemove(item.cartItemId)}>
                                    삭제
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            <div className="cart-summary">
                <p>
                    총 합계: <strong>{Number(cart.totalPrice ?? 0).toLocaleString()}원</strong>
                </p>
                <div className="cart-actions">
                    <button className="cart-button secondary" onClick={handleClear}>
                        장바구니 비우기
                    </button>
                    <button className="cart-button primary" onClick={() => navigate('/checkout')}>
                        주문하기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CartPage;
