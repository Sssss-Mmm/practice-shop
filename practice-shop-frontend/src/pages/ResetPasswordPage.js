import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import './AuthPages.css';
import AuthService from '../services/auth.service';

/**
 * 비밀번호 재설정을 위한 페이지 컴포넌트입니다.
 * 사용자는 이메일로 받은 토큰과 새 비밀번호를 입력하여 비밀번호를 변경할 수 있습니다.
 * @returns {JSX.Element} ResetPasswordPage 컴포넌트
 */
const ResetPasswordPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [token, setToken] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [message, setMessage] = useState('');
    const [success, setSuccess] = useState(false);
    const [loading, setLoading] = useState(false);

    /**
     * 컴포넌트가 마운트될 때 URL 쿼리 파라미터에서 재설정 토큰을 가져와 상태에 저장합니다.
     * @listens searchParams
     */
    useEffect(() => {
        const paramToken = searchParams.get('token');
        if (paramToken) {
            setToken(paramToken);
        }
    }, [searchParams]);

    /**
     * '비밀번호 변경' 버튼 클릭 시 호출되는 폼 제출 핸들러입니다.
     * 입력된 정보의 유효성을 검사한 후, 서버에 비밀번호 재설정을 요청합니다.
     * @param {React.FormEvent<HTMLFormElement>} event - 폼 이벤트 객체
     */
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

    /**
     * 비밀번호 변경 성공 후 '로그인 페이지로 이동' 버튼 클릭 시 호출됩니다.
     * 적절한 메시지와 함께 로그인 페이지로 이동시킵니다.
     */
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
        <div className="ticket-auth-container">
            <div className="ticket-auth-card">
                <h2 className="text-center mb-4">비밀번호 재설정</h2>

                <form onSubmit={handleSubmit}>
                    <div className="form-group mb-3">
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

                    <div className="form-group mb-3">
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

                    <div className="form-group mb-3">
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

                    <div className="d-grid gap-2 mt-4">
                        <button className="btn btn-primary btn-block" disabled={loading}>
                            {loading ? '변경 중...' : '비밀번호 변경'}
                        </button>
                    </div>

                    {message && (
                        <div className="form-group mt-3">
                            <div
                                className={`alert ${success ? 'alert-success' : 'alert-danger'}`}
                                role="alert"
                            >
                                {message}
                            </div>
                        </div>
                    )}
                </form>

                <div className="text-center mt-3">
                    <Link to="/forgot-password" className="btn btn-link">다시 이메일 받기</Link>
                    {success && (
                        <button className="btn btn-link" onClick={handleGoToLogin}>
                            로그인 페이지로 이동
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ResetPasswordPage;
