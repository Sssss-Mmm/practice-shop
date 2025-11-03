import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthService from '../services/auth.service';

const ResendVerificationPage = () => {
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');
    const [success, setSuccess] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setMessage('');
        setSuccess(false);
        setLoading(true);

        try {
            await AuthService.resendVerificationEmail(email);
            setSuccess(true);
            setMessage('인증 이메일을 다시 발송했습니다. 받은 편지함을 확인해 주세요.');
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
        <div className="col-md-12">
            <div className="card card-container">
                <h2 className="text-center mb-4">이메일 인증 재발송</h2>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="email">이메일</label>
                        <input
                            type="email"
                            className="form-control"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group mt-3">
                        <button className="btn btn-primary btn-block" disabled={loading}>
                            {loading ? '전송 중...' : '인증 메일 재발송'}
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

                <div className="text-center mt-3">
                    <Link to="/login">로그인 페이지로 돌아가기</Link>
                </div>
            </div>
        </div>
    );
};

export default ResendVerificationPage;
