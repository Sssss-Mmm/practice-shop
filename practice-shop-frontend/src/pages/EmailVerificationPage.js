import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import AuthService from '../services/auth.service';

const EmailVerificationPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [status, setStatus] = useState('pending'); // pending | success | error
    const [message, setMessage] = useState('이메일 인증을 확인하는 중입니다...');

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

    return (
        <div className="col-md-12">
            <div className="card card-container text-center">
                <h2>이메일 인증</h2>
                <p className="mt-3">{message}</p>

                <div className="mt-4 d-flex gap-2 justify-content-center">
                    <button className="btn btn-primary" onClick={handleGoToLogin}>
                        로그인 페이지로 이동
                    </button>
                    {status !== 'success' && (
                        <Link className="btn btn-outline-secondary" to="/resend-verification">
                            인증 메일 다시 보내기
                        </Link>
                    )}
                </div>
            </div>
        </div>
    );
};

export default EmailVerificationPage;
