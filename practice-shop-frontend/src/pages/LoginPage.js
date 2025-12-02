import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthPages.css';
import { FaGoogle } from 'react-icons/fa';

const LoginPage = () => {
    const { login } = useAuth();
    const [username, setUsername] = useState(''); // 'email'을 'username'으로 변경
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();
    const [message, setMessage] = useState(location.state?.message || '');
    const [messageVariant, setMessageVariant] = useState(location.state?.variant || 'danger');

    useEffect(() => {
        // 다른 페이지에서 전달된 메시지가 있다면 표시합니다.
        if (location.state?.message) {
            // 메시지를 표시한 후, state를 초기화하여 새로고침 시 메시지가 다시 나타나지 않도록 합니다.
            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [location, navigate]);

    const handleLogin = async (e) => {
        e.preventDefault();

        setMessage('');
        setLoading(true);

        try {
            await login(username, password); // Context의 login 함수 사용
            navigate('/');
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

    return (
        <div className="ticket-auth-container">
            <div className="ticket-auth-card">
                <h2 className="text-center mb-4">로그인</h2>

                <form onSubmit={handleLogin} noValidate>
                    <div className="form-group mb-3">
                        <label htmlFor="username">아이디 (이메일)</label>
                        <input
                            type="text"
                            className="form-control"
                            name="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="password">비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            name="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                    </div>

                    <div className="d-flex justify-content-between align-items-center mb-3">
                        <div className="form-check">
                            <input className="form-check-input" type="checkbox" value="" id="rememberMe" />
                            <label className="form-check-label" htmlFor="rememberMe">
                                아이디 저장
                            </label>
                        </div>
                        <div className="form-check">
                            <input className="form-check-input" type="checkbox" value="" id="secureConnection" defaultChecked />
                            <label className="form-check-label" htmlFor="secureConnection">
                                보안접속
                            </label>
                        </div>
                    </div>

                    <div className="d-grid gap-2">
                        <button className="btn btn-primary btn-block" disabled={loading}>
                            {loading && (
                                <span className="spinner-border spinner-border-sm"></span>
                            )}
                            <span>로그인</span>
                        </button>
                    </div>
                </form>

                {message && (
                    <div className={`alert alert-${messageVariant} mt-3`} role="alert">
                        {message}
                    </div>
                )}

                <div className="text-center my-3">
                    <Link to="/signup" className="btn btn-link">회원가입</Link>
                    <span className="text-muted mx-1">|</span>
                    <Link to="/forgot-password" className="btn btn-link">비밀번호 찾기</Link>
                </div>

                <div className="social-login-divider">
                    <span className="social-login-text">SNS 계정으로 로그인</span>
                </div>

                <div className="d-grid gap-2 mt-3">
                     <a href={`${process.env.REACT_APP_API_BASE_URL || 'http://localhost:8084'}/oauth2/authorization/google`} className="btn btn-social btn-google">
                        <FaGoogle className="social-icon" /> Google로 로그인
                    </a>
                    {/* 네이버, 카카오 로그인 버튼 추가 위치 */}
                    <button className="btn btn-social btn-naver" disabled>
                        Naver로 로그인
                    </button>
                    <button className="btn btn-social btn-kakao" disabled>
                        Kakao로 로그인
                    </button>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
