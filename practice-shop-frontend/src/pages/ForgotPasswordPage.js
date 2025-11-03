import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthService from '../services/auth.service';

const ForgotPasswordPage = () => {
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
            await AuthService.requestPasswordReset(email);
            setSuccess(true);
            setMessage('비밀번호 재설정 안내 이메일을 발송했습니다.');
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
                <h2 className="text-center mb-4">비밀번호 재설정</h2>
                <p className="text-muted text-center">
                    가입하신 이메일을 입력하시면 비밀번호 재설정 링크를 보내드립니다.
                </p>

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
                            {loading ? '전송 중...' : '재설정 메일 보내기'}
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

export default ForgotPasswordPage;
