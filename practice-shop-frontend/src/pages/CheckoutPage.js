import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import CartService from '../services/cart.service';
import OrderService from '../services/order.service';
import './CheckoutPage.css';

const CheckoutPage = () => {
    const navigate = useNavigate();
    const [cart, setCart] = useState({ items: [], totalPrice: 0 });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [message, setMessage] = useState('');
    const [form, setForm] = useState({
        recipientName: '',
        contactNumber: '',
        shippingAddress: '',
        postalCode: '',
        deliveryInstructions: '',
        paymentMethod: 'CARD',
    });

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
                setCart(response.data ?? { items: [], totalPrice: 0 });
                setError(null);
            })
            .catch((err) => {
                const status = err.response?.status;
                const resMessage =
                    status === 401
                        ? '주문을 진행하려면 로그인해야 합니다.'
                        : (err.response && err.response.data && err.response.data.message) ||
                          err.message ||
                          err.toString();
                setError(resMessage);
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        loadCart();
    }, []);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        if (!cart.items || cart.items.length === 0) {
            setMessage('장바구니가 비어 있어 주문을 진행할 수 없습니다.');
            return;
        }
        setSubmitting(true);
        setMessage('');
        OrderService.createOrder(form)
            .then((response) => {
                const order = response.data;
                setMessage('주문이 완료되었습니다. 주문 내역 페이지로 이동합니다.');
                CartService.getCart();
                setTimeout(() => {
                    if (order?.orderId) {
                        navigate(`/orders/${order.orderId}`, { replace: true });
                    } else {
                        navigate('/orders', { replace: true });
                    }
                }, 1000);
            })
            .catch((err) => {
                const status = err.response?.status;
                const resMessage =
                    status === 401
                        ? '로그인이 필요합니다.'
                        : (err.response && err.response.data && err.response.data.message) ||
                          err.message ||
                          err.toString();
                setError(resMessage);
            })
            .finally(() => setSubmitting(false));
    };

    if (loading) {
        return (
            <div className="checkout-container">
                <h1>주문/결제</h1>
                <p>주문 정보를 불러오는 중입니다...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="checkout-container">
                <h1>주문/결제</h1>
                <p className="checkout-error">{error}</p>
                {error.includes('로그인') && (
                    <button className="checkout-button" onClick={() => navigate('/login')}>
                        로그인하기
                    </button>
                )}
            </div>
        );
    }

    if (!cart.items || cart.items.length === 0) {
        return (
            <div className="checkout-container">
                <h1>주문/결제</h1>
                <p>장바구니가 비어 있습니다.</p>
                <button className="checkout-button" onClick={() => navigate('/')}>쇼핑 계속하기</button>
            </div>
        );
    }

    return (
        <div className="checkout-container">
            <h1>주문/결제</h1>
            <div className="checkout-content">
                <div className="checkout-summary">
                    <h2>주문 상품</h2>
                    <ul>
                        {cart.items.map((item) => (
                            <li key={item.cartItemId}>
                                <img src={resolveImageUrl(item.imageUrl)} alt={item.productName} />
                                <div>
                                    <strong>{item.productName}</strong>
                                    <p>{item.quantity}개 · {item.price.toLocaleString()}원</p>
                                </div>
                            </li>
                        ))}
                    </ul>
                    <p className="checkout-total">총 합계: {Number(cart.totalPrice ?? 0).toLocaleString()}원</p>
                </div>

                <form className="checkout-form" onSubmit={handleSubmit}>
                    <h2>배송 정보</h2>
                    <label htmlFor="recipientName">수령인 이름</label>
                    <input
                        id="recipientName"
                        name="recipientName"
                        value={form.recipientName}
                        onChange={handleChange}
                        required
                    />

                    <label htmlFor="contactNumber">연락처</label>
                    <input
                        id="contactNumber"
                        name="contactNumber"
                        value={form.contactNumber}
                        onChange={handleChange}
                        required
                    />

                    <label htmlFor="shippingAddress">배송지</label>
                    <input
                        id="shippingAddress"
                        name="shippingAddress"
                        value={form.shippingAddress}
                        onChange={handleChange}
                        required
                    />

                    <label htmlFor="postalCode">우편번호</label>
                    <input
                        id="postalCode"
                        name="postalCode"
                        value={form.postalCode}
                        onChange={handleChange}
                    />

                    <label htmlFor="deliveryInstructions">배송 요청 사항</label>
                    <textarea
                        id="deliveryInstructions"
                        name="deliveryInstructions"
                        value={form.deliveryInstructions}
                        onChange={handleChange}
                    />

                    <label htmlFor="paymentMethod">결제 수단</label>
                    <select
                        id="paymentMethod"
                        name="paymentMethod"
                        value={form.paymentMethod}
                        onChange={handleChange}
                        required
                    >
                        <option value="CARD">카드 결제</option>
                        <option value="BANK_TRANSFER">계좌 이체</option>
                        <option value="CASH">무통장 입금</option>
                    </select>

                    <button className="checkout-button primary" type="submit" disabled={submitting}>
                        {submitting ? '주문 처리 중...' : '주문 완료'}
                    </button>

                    {message && <p className="checkout-message">{message}</p>}
                </form>
            </div>
        </div>
    );
};

export default CheckoutPage;
