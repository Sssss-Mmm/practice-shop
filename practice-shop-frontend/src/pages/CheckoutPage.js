import React, { useEffect, useState, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import OrderService from '../services/order.service';
import AuthService from '../services/auth.service';
import './CheckoutPage.css';

const CheckoutPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    
    // Data from SeatSelectionPage
    const { selectedSeats, showtimeInfo } = location.state || {};

    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState(null);
    const [tossReady, setTossReady] = useState(false);
    
    const [form, setForm] = useState({
        customerName: '',
        contactNumber: '',
        paymentMethod: 'CARD',
    });
    
    const tossClientKey = process.env.REACT_APP_TOSS_CLIENT_KEY;

    useEffect(() => {
        // If required data is not present, redirect to home
        if (!selectedSeats || !showtimeInfo || selectedSeats.length === 0) {
            alert("잘못된 접근입니다. 예매를 다시 시작해주세요.");
            navigate('/');
            return;
        }

        const currentUser = AuthService.getCurrentUser();
        if (currentUser) {
            setForm(prev => ({ ...prev, customerName: currentUser.username, contactNumber: '' }));
        }

        setLoading(false);
    }, [selectedSeats, showtimeInfo, navigate]);

    // Toss Payments setup
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
            document.head.removeChild(script);
        };
    }, [tossClientKey]);
    
    const totalPrice = useMemo(() => {
        return selectedSeats?.reduce((total, seat) => total + seat.price, 0) || 0;
    }, [selectedSeats]);

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
        
        return tossPayments.requestPayment(mapTossPaymentType(form.paymentMethod), {
            amount: order.totalPrice,
            orderId: `TICKET-${order.orderId}`,
            orderName: showtimeInfo.title,
            customerName: form.customerName,
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
        setSubmitting(true);
        setMessage('주문 정보를 생성하고 있습니다...');
        setError(null);

        const orderPayload = {
            ...form,
            showtimeId: showtimeInfo.id,
            seatIds: selectedSeats.map(s => s.seatId),
            totalAmount: totalPrice,
        };

        OrderService.createOrder(orderPayload)
            .then((response) => {
                setMessage('토스 결제창으로 이동합니다.');
                return requestTossPayment(response.data);
            })
            .catch((err) => {
                const resMessage = err.response?.data?.message || err.message || '알 수 없는 오류가 발생했습니다.';
                setError(resMessage);
            })
            .finally(() => setSubmitting(false));
    };
    
    const formatDate = (dateString) => {
        if (!dateString) return '';
        const options = { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'short', hour: '2-digit', minute: '2-digit' };
        return new Date(dateString).toLocaleString('ko-KR', options);
    };

    if (loading) {
        return <div className="checkout-container"><p>주문 정보를 구성 중입니다...</p></div>;
    }
    
    if (error) {
        return <div className="checkout-container error-page"><p className="checkout-error">{error}</p></div>;
    }

    return (
        <div className="checkout-container">
            <h1>주문/결제</h1>
            <div className="checkout-content">
                <div className="ticket-order-summary">
                    <h2>예매 정보 확인</h2>
                    <div className="show-info">
                        <img src={showtimeInfo.posterImageUrl || '/placeholder.png'} alt={showtimeInfo.title} />
                        <div>
                            <h3>{showtimeInfo.title}</h3>
                            <p>{showtimeInfo.venue.name}</p>
                            <p>{formatDate(showtimeInfo.startDateTime)}</p>
                        </div>
                    </div>
                    
                    <h3>선택 좌석</h3>
                    <ul className="selected-seats-review">
                        {selectedSeats.map((seat) => (
                            <li key={seat.seatId}>
                                <span>{seat.section} {seat.row}열 {seat.number}번</span>
                                <span>{seat.price.toLocaleString()}원</span>
                            </li>
                        ))}
                    </ul>

                    <div className="final-price">
                        <strong>총 결제 금액</strong>
                        <strong>{totalPrice.toLocaleString()}원</strong>
                    </div>
                </div>

                <form className="checkout-form" onSubmit={handleSubmit}>
                    <h2>예매자 정보</h2>
                    <label htmlFor="customerName">이름</label>
                    <input id="customerName" name="customerName" value={form.customerName} onChange={handleChange} required />

                    <label htmlFor="contactNumber">연락처</label>
                    <input id="contactNumber" name="contactNumber" value={form.contactNumber} onChange={handleChange} placeholder="'-' 없이 숫자만 입력" required />

                    <h2>결제 수단</h2>
                    <select id="paymentMethod" name="paymentMethod" value={form.paymentMethod} onChange={handleChange} required >
                        <option value="CARD">카드 결제</option>
                        <option value="BANK_TRANSFER">계좌 이체</option>
                    </select>

                    <button className="checkout-button primary" type="submit" disabled={submitting || !tossReady}>
                        {submitting ? '결제 요청 중...' : `${totalPrice.toLocaleString()}원 결제하기`}
                    </button>
                    {message && <p className="checkout-message">{message}</p>}
                </form>
            </div>
        </div>
    );
};

export default CheckoutPage;
