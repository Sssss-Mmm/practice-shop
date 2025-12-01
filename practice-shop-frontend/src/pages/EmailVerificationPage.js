import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { FaCheckCircle, FaExclamationCircle, FaSpinner } from 'react-icons/fa';
import './AuthPages.css';
import AuthService from '../services/auth.service';

/**
 * 이메일 인증을 처리하고 결과를 보여주는 페이지 컴포넌트입니다.
 * @returns {JSX.Element} EmailVerificationPage 컴포넌트
 */
const EmailVerificationPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [status, setStatus] = useState('pending'); // pending | success | error
    const [message, setMessage] = useState('이메일 인증을 확인하는 중입니다...');

    /**
     * 컴포넌트가 마운트될 때 URL 쿼리 파라미터에서 인증 토큰을 가져와 서버에 인증을 요청합니다.
     * @listens searchParams
     */
    useEffect(() => {
        const token = searchParams.get('token');

        if (!token) {
            setStatus('error');
            setMessage('인증 토큰이 없습니다. 다시 시도해 주세요.');
            return;
        }

        AuthService.verifyEmail(token)
            .then(() => {
                setStatus('success');
                setMessage('이메일 인증이 완료되었습니다. 이제 로그인할 수 있습니다.');
            })
            .catch((error) => {
                const resMessage =
                    (error.response && error.response.data && error.response.data.message) ||
                    error.message ||
                    error.toString();
                setStatus('error');
                setMessage(resMessage);
            });
    }, [searchParams]);

    /**
     * '로그인 페이지로 이동' 버튼 클릭 시 호출됩니다.
     * 인증 성공 여부에 따라 적절한 메시지와 함께 로그인 페이지로 이동시킵니다.
     */
    const handleGoToLogin = () => {
        const isSuccess = status === 'success';

        navigate('/login', {
            replace: true,
            state: {
                message: isSuccess
                    ? '이메일 인증이 완료되었습니다. 로그인해 주세요.'
                    : '이메일 인증이 실패했습니다. 다시 시도해 주세요.',
                variant: isSuccess ? 'success' : 'danger',
            },
        });
    };

    /**
     * 현재 인증 상태(status)에 따라 적절한 아이콘을 반환합니다.
     * @returns {JSX.Element} 상태에 맞는 아이콘 컴포넌트
     */
    const getStatusIcon = () => {
        if (status === 'success') return <FaCheckCircle className="auth-icon success" />;
        if (status === 'error') return <FaExclamationCircle className="auth-icon error" />;
        return <FaSpinner className="auth-icon pending" />;
    };

    return (
        <div className="ticket-auth-container">
            <div className="ticket-auth-card text-center">
                {getStatusIcon()}
                <h2 className="mt-3">이메일 인증</h2>
                <p className="mt-2">{message}</p>

                <div className="mt-4 d-grid gap-2">
                    <button className="btn btn-primary btn-block" onClick={handleGoToLogin}>
                        로그인 페이지로 이동
                    </button>
                    {status !== 'success' && (
                        <Link className="btn btn-secondary btn-block" to="/resend-verification">
                            인증 메일 다시 보내기
                        </Link>
                    )}
                </div>
            </div>
        </div>
    );
};

export default EmailVerificationPage;
