import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import './CheckoutPage.css';

const TossFailPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const code = searchParams.get('code');
    const message = searchParams.get('message');

    return (
        <div className="checkout-container">
            <h1>결제에 실패했습니다</h1>
            <p className="checkout-error">
                {message || '결제를 완료할 수 없습니다. 다시 시도해 주세요.'}
            </p>
            {code && <p>오류 코드: {code}</p>}
            <div className="checkout-actions">
                <button className="checkout-button primary" onClick={() => navigate('/checkout')}>
                    다시 결제하기
                </button>
                <button className="checkout-button" onClick={() => navigate('/cart')}>
                    장바구니로 이동
                </button>
            </div>
        </div>
    );
};

export default TossFailPage;
