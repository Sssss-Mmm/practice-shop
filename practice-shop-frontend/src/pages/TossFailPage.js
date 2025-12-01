import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { FaExclamationCircle } from 'react-icons/fa';
import './CheckoutPage.css';

/**
 * 토스(Toss) 결제 실패 시 보여주는 페이지 컴포넌트입니다.
 * URL 쿼리 파라미터로부터 에러 코드와 메시지를 받아와 사용자에게 안내합니다.
 * @returns {JSX.Element} TossFailPage 컴포넌트
 */
const TossFailPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const code = searchParams.get('code');
    const message = searchParams.get('message');

    return (
        <div className="checkout-container text-center">
            <FaExclamationCircle className="checkout-icon-fail" />
            <h1 className="mt-3">결제에 실패했습니다</h1>
            <p className="checkout-message error-message">
                {message || '결제를 완료할 수 없습니다. 다시 시도해 주세요.'}
            </p>
            {code && <p className="checkout-code">오류 코드: {code}</p>}
            <div className="checkout-actions mt-4">
                <button className="btn btn-primary btn-lg" onClick={() => navigate('/checkout')}>
                    다시 결제하기
                </button>
                <button className="btn btn-secondary btn-lg mt-2" onClick={() => navigate('/cart')}>
                    장바구니로 이동
                </button>
            </div>
        </div>
    );
};

export default TossFailPage;
