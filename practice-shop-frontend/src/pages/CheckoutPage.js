import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import CartService from '../services/cart.service';
import OrderService from '../services/order.service';
import AuthService from '../services/auth.service';
import './CheckoutPage.css';

/**
 * 장바구니 기반 주문/결제 페이지.
 * - 로그인 사용자만 접근
 * - 장바구니 요약, 배송/수령 정보, 결제수단 입력
 * - 주문 생성 후 토스 결제창 호출
 */
const CheckoutPage = () => {
    const navigate = useNavigate();
    const [cart, setCart] = useState({ items: [], totalItems: 0, totalPrice: 0 });
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState(null);
    const [tossReady, setTossReady] = useState(false);

    const [form, setForm] = useState({
        recipientName: '',
        contactNumber: '',
        shippingAddress: '',
        postalCode: '',
        deliveryInstructions: '',
        paymentMethod: 'CARD',
    });

    const tossClientKey = process.env.REACT_APP_TOSS_CLIENT_KEY;

    const loadCart = () => {
        setLoading(true);
        CartService.getCart()
            .then((response) => {
                setCart(response.data ?? { items: [], totalItems: 0, totalPrice: 0 });
                setError(null);
            })
            .catch((err) => {
                const status = err.response?.status;
                if (status === 401) {
                    navigate('/login', {
                        replace: true,
                        state: { message: '로그인 후 결제를 진행해 주세요.', variant: 'warning' },
                    });
                    return;
                }
                setError(err.response?.data?.message || '장바구니를 불러오지 못했습니다.');
            })
            .finally(() => setLoading(false));
    };

    // 초기 데이터: 로그인 여부 확인 후 장바구니와 토스 스크립트 로드
    useEffect(() => {
        const user = AuthService.getCurrentUser();
        if (!user) {
            navigate('/login', {
                replace: true,
                state: { message: '로그인 후 결제를 진행해 주세요.', variant: 'warning' },
            });
            return;
        }
        setForm((prev) => ({
            ...prev,
            recipientName: user.username || user.email || '',
        }));
        loadCart();
    }, [navigate]);

    useEffect(() => {
        if (!tossClientKey) {
            setError('결제 설정이 올바르지 않습니다. 관리자에게 문의해 주세요.');
            return;
        }
        const script = document.createElement('script');
        script.src = 'https://js.tosspayments.com/v1';
        script.async = true;
        script.onload = () => setTossReady(true);
        document.head.appendChild(script);

        return () => {
            document.head.removeChild(script);
        };
    }, [tossClientKey]);

    const totalPrice = useMemo(() => {
        if (cart?.totalPrice) return Number(cart.totalPrice);
        if (!cart?.items) return 0;
        return cart.items.reduce((sum, item) => sum + (item.subtotal ?? item.price * item.quantity), 0);
    }, [cart]);

    const orderName = useMemo(() => {
        if (!cart?.items?.length) return '주문';
        if (cart.items.length === 1) return cart.items[0].productName;
        return `${cart.items[0].productName} 외 ${cart.items.length - 1}건`;
    }, [cart]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const mapTossPaymentType = (method) => (method === 'BANK_TRANSFER' ? '계좌이체' : '카드');

    const requestTossPayment = (order) => {
        if (!window.TossPayments || !tossClientKey) {
            throw new Error('토스 결제 모듈이 준비되지 않았습니다.');
        }
        const tossPayments = window.TossPayments(tossClientKey);
        const amount = Number(order.totalPrice ?? totalPrice ?? 0);

        return tossPayments.requestPayment(mapTossPaymentType(form.paymentMethod), {
            amount,
            orderId: `ORD-${order.orderId}`,
            orderName,
            customerName: form.recipientName,
            customerMobilePhone: form.contactNumber,
            successUrl: `${window.location.origin}/payments/toss/success`,
            failUrl: `${window.location.origin}/payments/toss/fail`,
        });
    };

    const handleSubmit = (event) => {
        event.preventDefault();
        if (!tossReady) {
            setError('결제 모듈이 아직 준비되지 않았습니다.');
            return;
        }
        if (!cart?.items?.length) {
            setError('장바구니가 비어 있습니다.');
            return;
        }

        setSubmitting(true);
        setMessage('주문 정보를 생성하고 있습니다...');
        setError(null);

        OrderService.createOrder(form)
            .then((response) => {
                setMessage('결제창으로 이동합니다...');
                return requestTossPayment(response.data);
            })
            .catch((err) => {
                const status = err.response?.status;
                if (status === 401) {
                    navigate('/login', {
                        replace: true,
                        state: { message: '세션이 만료되었습니다. 다시 로그인해 주세요.', variant: 'warning' },
                    });
                    return;
                }
                const resMessage = err.response?.data?.message || err.message || '알 수 없는 오류가 발생했습니다.';
                setError(resMessage);
            })
            .finally(() => setSubmitting(false));
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
        return <div className="checkout-container"><p>주문 정보를 구성 중입니다...</p></div>;
    }

    return (
        <div className="checkout-container">
            <h1>주문/결제</h1>
            {error && <p className="checkout-error">{error}</p>}
            <div className="checkout-content">
                <div className="ticket-order-summary">
                    <h2>주문 상품</h2>
                    {(!cart.items || cart.items.length === 0) ? (
                        <div className="empty-cart">
                            <p>장바구니가 비어 있습니다.</p>
                            <button className="checkout-button" onClick={() => navigate('/')}>쇼핑 계속하기</button>
                        </div>
                    ) : (
                        <>
                            <ul className="selected-seats-review">
                                {cart.items.map((item) => (
                                    <li key={item.cartItemId} className="checkout-item">
                                        <div className="checkout-item-info">
                                            <img src={resolveImageUrl(item.imageUrl)} alt={item.productName} />
                                            <div>
                                                <strong>{item.productName}</strong>
                                                <p className="checkout-item-meta">{item.quantity}개 × {Number(item.price).toLocaleString()}원</p>
                                            </div>
                                        </div>
                                        <span>{Number(item.subtotal ?? item.price * item.quantity).toLocaleString()}원</span>
                                    </li>
                                ))}
                            </ul>
                            <div className="final-price">
                                <strong>총 결제 금액</strong>
                                <strong>{Number(totalPrice || 0).toLocaleString()}원</strong>
                            </div>
                        </>
                    )}
                </div>

                <form className="checkout-form" onSubmit={handleSubmit}>
                    <h2>수령자 정보</h2>
                    <label htmlFor="recipientName">이름</label>
                    <input id="recipientName" name="recipientName" value={form.recipientName} onChange={handleChange} required />

                    <label htmlFor="contactNumber">연락처</label>
                    <input id="contactNumber" name="contactNumber" value={form.contactNumber} onChange={handleChange} placeholder="'-' 없이 숫자만 입력" required />

                    <label htmlFor="shippingAddress">배송지</label>
                    <input id="shippingAddress" name="shippingAddress" value={form.shippingAddress} onChange={handleChange} required />

                    <label htmlFor="postalCode">우편번호</label>
                    <input id="postalCode" name="postalCode" value={form.postalCode} onChange={handleChange} />

                    <label htmlFor="deliveryInstructions">요청 사항</label>
                    <textarea id="deliveryInstructions" name="deliveryInstructions" value={form.deliveryInstructions} onChange={handleChange} rows="2" />

                    <h2>결제 수단</h2>
                    <select id="paymentMethod" name="paymentMethod" value={form.paymentMethod} onChange={handleChange} required >
                        <option value="CARD">카드 결제</option>
                        <option value="BANK_TRANSFER">계좌 이체</option>
                    </select>

                    <button className="checkout-button primary" type="submit" disabled={submitting || !tossReady || !cart.items?.length}>
                        {submitting ? '결제 요청 중...' : `${Number(totalPrice || 0).toLocaleString()}원 결제하기`}
                    </button>
                    {message && <p className="checkout-message">{message}</p>}
                </form>
            </div>
        </div>
    );
};

export default CheckoutPage;
