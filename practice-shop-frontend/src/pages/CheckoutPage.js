import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import CartService from '../services/cart.service';
import OrderService from '../services/order.service';
import AuthService from '../services/auth.service';
import TicketingService from '../services/ticketing.service';
import './CheckoutPage.css';

/**
 * 주문/결제 페이지.
 * - 일반 상품(장바구니) 및 티켓 예매 결제 지원
 */
const CheckoutPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    
    // Ticketing State
    const ticketingState = location.state?.selectedSeats ? location.state : null;
    const isTicketing = !!ticketingState;

    const [cart, setCart] = useState({ items: [], totalItems: 0, totalPrice: 0 });
    const [loading, setLoading] = useState(!isTicketing); // Ticketing doesn't need to load cart
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

    // Load Cart (only if not ticketing)
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
            recipientName: user.username || user.name || '', // Use name if available
        }));

        if (!isTicketing) {
            setLoading(true);
            CartService.getCart()
                .then((response) => {
                    setCart(response.data ?? { items: [], totalItems: 0, totalPrice: 0 });
                    setError(null);
                })
                .catch((err) => {
                    const status = err.response?.status;
                    if (status === 401) {
                        navigate('/login', { replace: true });
                        return;
                    }
                    setError(err.response?.data?.message || '장바구니를 불러오지 못했습니다.');
                })
                .finally(() => setLoading(false));
        }
    }, [navigate, isTicketing]);

    // Load Toss Script
    useEffect(() => {
        if (!tossClientKey) {
            setError('결제 설정이 올바르지 않습니다.');
            return;
        }
        const script = document.createElement('script');
        script.src = 'https://js.tosspayments.com/v1';
        script.async = true;
        script.onload = () => setTossReady(true);
        document.head.appendChild(script);

        return () => {
             // Cleanup if needed
             if (document.head.contains(script)) {
                 document.head.removeChild(script);
             }
        };
    }, [tossClientKey]);

    const totalPrice = useMemo(() => {
        if (isTicketing) {
            return ticketingState.selectedSeats.reduce((sum, seat) => sum + seat.price, 0);
        }
        if (cart?.totalPrice) return Number(cart.totalPrice);
        if (!cart?.items) return 0;
        return cart.items.reduce((sum, item) => sum + (item.subtotal ?? item.price * item.quantity), 0);
    }, [cart, isTicketing, ticketingState]);

    const orderName = useMemo(() => {
        if (isTicketing) {
            return `[예매] ${ticketingState.showtimeInfo?.eventTitle || '공연 티켓'}`;
        }
        if (!cart?.items?.length) return '주문';
        if (cart.items.length === 1) return cart.items[0].productName;
        return `${cart.items[0].productName} 외 ${cart.items.length - 1}건`;
    }, [cart, isTicketing, ticketingState]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const requestTossPayment = (orderId, amount) => {
        if (!window.TossPayments || !tossClientKey) {
            throw new Error('토스 결제 모듈이 준비되지 않았습니다.');
        }
        const tossPayments = window.TossPayments(tossClientKey);

        return tossPayments.requestPayment(form.paymentMethod === 'BANK_TRANSFER' ? '계좌이체' : '카드', {
            amount,
            orderId: isTicketing ? orderId : `ORD-${orderId}`, // Ticketing uses UUID string, Order uses Long ID
            orderName,
            customerName: form.recipientName,
            customerMobilePhone: form.contactNumber,
            successUrl: `${window.location.origin}/payments/toss/success?type=${isTicketing ? 'ticket' : 'order'}`,
            failUrl: `${window.location.origin}/payments/toss/fail`,
        });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (!tossReady) {
            setError('결제 모듈이 준비되지 않았습니다.');
            return;
        }

        setSubmitting(true);
        setMessage('주문 정보를 생성하고 있습니다...');
        setError(null);

        try {
            if (isTicketing) {
                // Ticket Reservation
                const payload = {
                    showtimeId: ticketingState.showtimeId,
                    seatIds: ticketingState.selectedSeats.map(s => s.seatId)
                };
                const res = await TicketingService.reserveSeats(payload);
                setMessage('결제창으로 이동합니다...');
                // res.data is ReservationResponse
                await requestTossPayment(res.data.orderId, res.data.totalPrice);
            } else {
                // General Order
                if (!cart?.items?.length) throw new Error('장바구니가 비어 있습니다.');
                
                const res = await OrderService.createOrder(form);
                setMessage('결제창으로 이동합니다...');
                await requestTossPayment(res.data.orderId, res.data.totalPrice);
            }
        } catch (err) {
            console.error(err);
            const status = err.response?.status;
            if (status === 401) {
                navigate('/login', { replace: true });
                return;
            }
            setError(err.response?.data?.message || err.message || '오류가 발생했습니다.');
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div className="checkout-container"><p>로딩 중...</p></div>;

    return (
        <div className="checkout-container">
            <h1>{isTicketing ? '예매 결제' : '주문/결제'}</h1>
            {error && <p className="checkout-error">{error}</p>}
            <div className="checkout-content">
                <div className="ticket-order-summary">
                    <h2>{isTicketing ? '예매 정보' : '주문 상품'}</h2>
                    
                    {isTicketing ? (
                        <div className="ticketing-summary-details">
                            <h3 className="event-title">{ticketingState.showtimeInfo?.eventTitle}</h3>
                            <p className="event-venue">{ticketingState.showtimeInfo?.venueName}</p>
                            <p className="event-time">{new Date(ticketingState.showtimeInfo?.startDateTime).toLocaleString()}</p>
                            
                            <ul className="selected-seats-review">
                                {ticketingState.selectedSeats.map((seat) => (
                                    <li key={seat.seatId} className="checkout-item">
                                        <div className="checkout-item-info">
                                            <div>
                                                <strong>{seat.section} {seat.row}열 {seat.number}번</strong>
                                                <p className="checkout-item-meta">{seat.price.toLocaleString()}원</p>
                                            </div>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                            <div className="final-price">
                                <strong>총 결제 금액</strong>
                                <strong>{totalPrice.toLocaleString()}원</strong>
                            </div>
                        </div>
                    ) : (
                        (!cart.items || cart.items.length === 0) ? (
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
                                                <img src={item.imageUrl || '/placeholder.png'} alt={item.productName} />
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
                        )
                    )}
                </div>

                <form className="checkout-form" onSubmit={handleSubmit}>
                    <h2>결제 정보 입력</h2>
                    <label htmlFor="recipientName">이름</label>
                    <input id="recipientName" name="recipientName" value={form.recipientName} onChange={handleChange} required />

                    <label htmlFor="contactNumber">연락처</label>
                    <input id="contactNumber" name="contactNumber" value={form.contactNumber} onChange={handleChange} placeholder="'-' 없이 숫자만 입력" required />

                    {/* Show shipping address only for general orders */}
                    {!isTicketing && (
                        <>
                            <label htmlFor="shippingAddress">배송지</label>
                            <input id="shippingAddress" name="shippingAddress" value={form.shippingAddress} onChange={handleChange} required />

                            <label htmlFor="postalCode">우편번호</label>
                            <input id="postalCode" name="postalCode" value={form.postalCode} onChange={handleChange} />

                            <label htmlFor="deliveryInstructions">요청 사항</label>
                            <textarea id="deliveryInstructions" name="deliveryInstructions" value={form.deliveryInstructions} onChange={handleChange} rows="2" />
                        </>
                    )}

                    <h2>결제 수단</h2>
                    <select id="paymentMethod" name="paymentMethod" value={form.paymentMethod} onChange={handleChange} required >
                        <option value="CARD">카드 결제</option>
                        <option value="BANK_TRANSFER">계좌 이체</option>
                    </select>

                    <button className="checkout-button primary" type="submit" disabled={submitting || !tossReady || (!isTicketing && !cart.items?.length)}>
                        {submitting ? '처리 중...' : `${Number(totalPrice || 0).toLocaleString()}원 결제하기`}
                    </button>
                    {message && <p className="checkout-message">{message}</p>}
                </form>
            </div>
        </div>
    );
};

export default CheckoutPage;
