import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import AuthService from '../services/auth.service';
import './AuthPages.css';

const SignupPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [nickname, setNickname] = useState('');
    const [region, setRegion] = useState('');
    const [address, setAddress] = useState('');
    const [gender, setGender] = useState('');
    const [birthDate, setBirthDate] = useState('');
    const [message, setMessage] = useState('');
    const [success, setSuccess] = useState(false);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const resetForm = () => {
        setPassword('');
        setName('');
        setPhoneNumber('');
        setNickname('');
        setRegion('');
        setAddress('');
        setGender('');
        setBirthDate('');
    };

    const handleSignup = async (e) => {
        e.preventDefault();
        setMessage('');
        setSuccess(false);
        setLoading(true);

        try {
            await AuthService.signup({
                email,
                password,
                name,
                phoneNumber,
                nickname,
                region,
                address,
                gender,
                birthDate,
            });
            setSuccess(true);
            setMessage('회원가입이 완료되었습니다. 이메일의 인증 링크를 확인해 주세요.');
            resetForm();
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
                <h2 className="text-center mb-4">회원가입</h2>

                <form onSubmit={handleSignup} noValidate>
                    <div className="form-group mb-3">
                        <label htmlFor="email">이메일</label>
                        <input
                            type="email"
                            className="form-control"
                            name="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            disabled={success}
                            required
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
                            disabled={success}
                            required
                        />
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="name">이름</label>
                        <input
                            type="text"
                            className="form-control"
                            name="name"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            disabled={success}
                            required
                        />
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="phoneNumber">전화번호</label>
                        <input
                            type="text"
                            className="form-control"
                            name="phoneNumber"
                            value={phoneNumber}
                            onChange={(e) => setPhoneNumber(e.target.value)}
                            disabled={success}
                            required
                        />
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="nickname">닉네임</label>
                        <input
                            type="text"
                            className="form-control"
                            name="nickname"
                            value={nickname}
                            onChange={(e) => setNickname(e.target.value)}
                            disabled={success}
                            required
                        />
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="region">지역</label>
                        <input
                            type="text"
                            className="form-control"
                            name="region"
                            value={region}
                            onChange={(e) => setRegion(e.target.value)}
                            disabled={success}
                            required
                        />
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="address">상세주소</label>
                        <input
                            type="text"
                            className="form-control"
                            name="address"
                            value={address}
                            onChange={(e) => setAddress(e.target.value)}
                            disabled={success}
                            required
                        />
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="gender">성별</label>
                        <select
                            className="form-control"
                            name="gender"
                            value={gender}
                            onChange={(e) => setGender(e.target.value)}
                            disabled={success}
                            required
                        >
                            <option value="">성별 선택</option>
                            <option value="MALE">남성</option>
                            <option value="FEMALE">여성</option>
                            <option value="OTHER">기타</option>
                        </select>
                    </div>

                    <div className="form-group mb-3">
                        <label htmlFor="birthDate">생년월일</label>
                        <input
                            type="date"
                            className="form-control"
                            name="birthDate"
                            value={birthDate}
                            onChange={(e) => setBirthDate(e.target.value)}
                            required
                            disabled={success}
                        />
                    </div>

                    <div className="d-grid gap-2 mt-4">
                        <button className="btn btn-primary btn-block" disabled={loading || success}>
                            {loading ? '가입 처리 중...' : '회원가입'}
                        </button>
                    </div>

                    {message && (
                        <div className="form-group mt-3">
                            <div
                                className={`alert ${success ? 'alert-success' : 'alert-danger'}`}
                                role="alert"
                            >
                                {message}
                                {success && (
                                    <button
                                        type="button"
                                        className="btn btn-sm btn-success float-end"
                                        onClick={() => navigate('/login')}
                                    >
                                        로그인
                                    </button>
                                )}
                            </div>
                        </div>
                    )}
                </form>
                <div className="text-center mt-3">
                    <p className="mb-0">이미 계정이 있으신가요? <Link to="/login" className="btn-link">로그인</Link></p>
                </div>
            </div>
        </div>
    );
};

export default SignupPage;
