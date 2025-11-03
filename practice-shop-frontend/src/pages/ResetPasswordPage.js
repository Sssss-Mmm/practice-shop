import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import AuthService from '../services/auth.service';

const ResetPasswordPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [token, setToken] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [message, setMessage] = useState('');
    const [success, setSuccess] = useState(false);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const paramToken = searchParams.get('token');
        if (paramToken) {
            setToken(paramToken);
        }
    }, [searchParams]);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setMessage('');
        setSuccess(false);

        if (!token) {
            setMessage('재설정 토큰이 없습니다. 이메일 링크를 다시 확인해 주세요.');
            return;
        }

        if (newPassword !== confirmPassword) {
            setMessage('비밀번호와 확인 비밀번호가 일치하지 않습니다.');
            return;
        }

        if (newPassword.length < 8) {
            setMessage('비밀번호는 최소 8자 이상이어야 합니다.');
            return;
        }

        setLoading(true);

        try {
            await AuthService.resetPassword(token, newPassword);
            setSuccess(true);
            setMessage('비밀번호가 성공적으로 변경되었습니다. 이제 로그인할 수 있습니다.');
        } catch (error) {
            const resMessage =
                (error.response && error.response.data && error.response.data.message) ||
                error.message ||
                error.toString();
            setMessage(resMessage);
        } finally {
            setLoading(false);
        }
    };

    const handleGoToLogin = () => {
        navigate('/login', {
            replace: true,
            state: {
                message: '비밀번호가 변경되었습니다. 다시 로그인해 주세요.',
                variant: 'success',
            },
        });
    };

    return (
        <div className="col-md-12">
            <div className="card card-container">
                <h2 className="text-center mb-4">비밀번호 재설정</h2>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="token">재설정 토큰</label>
                        <input
                            type="text"
                            className="form-control"
                            id="token"
                            value={token}
                            onChange={(e) => setToken(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="newPassword">새 비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            id="newPassword"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirmPassword">새 비밀번호 확인</label>
                        <input
                            type="password"
                            className="form-control"
                            id="confirmPassword"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group mt-3">
                        <button className="btn btn-primary btn-block" disabled={loading}>
                            {loading ? '변경 중...' : '비밀번호 변경'}
                        </button>
                    </div>

                    {message && (
                        <div className="form-group">
                            <div
                                className={`alert ${success ? 'alert-success' : 'alert-danger'}`}
                                role="alert"
                            >
                                {message}
                            </div>
                        </div>
                    )}
                </form>

                <div className="d-flex justify-content-between mt-3">
                    <Link to="/forgot-password">다시 이메일 받기</Link>
                    {success && (
                        <button className="btn btn-link p-0" onClick={handleGoToLogin}>
                            로그인 페이지로 이동
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ResetPasswordPage;
