import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import AuthService from '../services/auth.service';

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [messageVariant, setMessageVariant] = useState('danger');
    const location = useLocation();
    const navigate = useNavigate();

    React.useEffect(() => {
        if (location.state?.message) {
            setMessage(location.state.message);
            setMessageVariant(location.state.variant || 'success');
        }
    }, [location.state]);

    const handleLogin = (e) => {
        e.preventDefault();

        setMessage('');
        setMessageVariant('danger');
        setLoading(true);

        AuthService.login(email, password)
            .then(() => {
                setLoading(false);
                navigate('/');
            })
            .catch((error) => {
                const resMessage =
                    (error.response && error.response.data && error.response.data.message) ||
                    (typeof error.response?.data === 'string' ? error.response.data : null) ||
                    error.message ||
                    error.toString();

                setLoading(false);
                setMessage(resMessage);
                setMessageVariant('danger');
            });
    };

    return (
        <div className="col-md-12">
            <div className="card card-container">
                <img
                    src="//ssl.gstatic.com/accounts/ui/avatar_2x.png"
                    alt="profile-img"
                    className="profile-img-card"
                />

                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label htmlFor="email">Email</label>
                        <input
                            type="email"
                            className="form-control"
                            name="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input
                            type="password"
                            className="form-control"
                            name="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <button className="btn btn-primary btn-block" disabled={loading}>
                            {loading && (
                                <span className="spinner-border spinner-border-sm"></span>
                            )}
                            <span>Login</span>
                        </button>
                    </div>

                    {message && (
                        <div className="form-group">
                            <div className={`alert alert-${messageVariant}`} role="alert">
                                {message}
                            </div>
                        </div>
                    )}
                </form>

                <div className="d-flex justify-content-between mt-3 w-100">
                    <Link to="/forgot-password" className="small">
                        비밀번호를 잊으셨나요?
                    </Link>
                    <Link to="/resend-verification" className="small">
                        인증 메일 다시 보내기
                    </Link>
                </div>

                <div className="form-group mt-3">
                    <a href="http://localhost:8084/oauth2/authorization/google" className="btn btn-danger btn-block">
                        Login with Google
                    </a>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
