import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import PaymentService from '../services/payment.service';
import CartService from '../services/cart.service';
import './CheckoutPage.css';

const TossSuccessPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const paymentKey = searchParams.get('paymentKey');
    const orderId = searchParams.get('orderId');
    const amountParam = searchParams.get('amount');
    const amount = amountParam ? Number(amountParam) : null;
    const [state, setState] = useState({
        loading: true,
        error: null,
        orderId: null,
    });

    useEffect(() => {
        if (!paymentKey || !orderId || !amount) {
            setState({
                loading: false,
                error: '결제 정보가 올바르지 않습니다.',
                orderId: null,
            });
            return;
        }

        PaymentService.confirmTossPayment({ paymentKey, orderId, amount })
            .then((response) => {
                setState({
                    loading: false,
                    error: null,
                    orderId: response.data?.orderId ?? null,
                });
                CartService.getCart().catch(() => {});
            })
            .catch((err) => {
                const resMessage =
                    err.response?.data?.message ||
                    err.message ||
                    '결제 승인 처리에 실패했습니다.';
                setState({
                    loading: false,
                    error: resMessage,
                    orderId: null,
                });
            });
    }, [amount, orderId, paymentKey]);

    if (state.loading) {
        return (
            <div className="checkout-container">
                <h1>결제 확인 중...</h1>
                <p>토스 결제 결과를 확인하고 있습니다.</p>
            </div>
        );
    }

    return (
        <div className="checkout-container">
            <h1>결제가 완료되었습니다.</h1>
            {state.error ? (
                <>
                    <p className="checkout-error">{state.error}</p>
                    <button className="checkout-button" onClick={() => navigate('/checkout')}>
                        다시 결제하기
                    </button>
                </>
            ) : (
                <>
                    <p className="checkout-message">주문이 성공적으로 완료되었습니다.</p>
                    <div className="checkout-actions">
                        <button
                            className="checkout-button primary"
                            onClick={() =>
                                state.orderId ? navigate(`/orders/${state.orderId}`) : navigate('/orders')
                            }
                        >
                            주문 상세 보기
                        </button>
                        <button className="checkout-button" onClick={() => navigate('/')}>
                            쇼핑 계속하기
                        </button>
                    </div>
                </>
            )}
        </div>
    );
};

export default TossSuccessPage;
