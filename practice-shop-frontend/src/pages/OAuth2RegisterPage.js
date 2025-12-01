import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/auth.service';
import './AuthPages.css';

/**
 * OAuth2 소셜 로그인 후 추가 정보를 입력받는 페이지 컴포넌트입니다.
 * @returns {JSX.Element} OAuth2RegisterPage 컴포넌트
 */
const OAuth2RegisterPage = () => {
    const [temporaryToken, setTemporaryToken] = useState('');
    const [name, setName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [nickname, setNickname] = useState('');
    const [region, setRegion] = useState('');
    const [address, setAddress] = useState('');
    const [gender, setGender] = useState('');
    const [birthDate, setBirthDate] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    /**
     * 컴포넌트가 마운트될 때 URL의 해시(#)에서 임시 토큰을 추출하여 상태에 저장합니다.
     * 토큰이 없으면 에러 메시지를 설정합니다.
     */
    useEffect(() => {
        const fragment = window.location.hash.substring(1);
        const params = new URLSearchParams(fragment);
        const token = params.get('token');

        if (token) {
            setTemporaryToken(token);
        } else {
            setMessage('임시 토큰을 찾을 수 없습니다. 다시 로그인해 주세요.');
        }
    }, []);

    /**
     * '가입 완료' 버튼 클릭 시 호출되는 폼 제출 핸들러입니다.
     * 사용자가 입력한 추가 정보를 임시 토큰과 함께 서버로 전송하여 회원가입을 완료합니다.
     * @param {React.FormEvent<HTMLFormElement>} e - 폼 이벤트 객체
     */
    const handleCompleteRegistration = async (e) => {
        e.preventDefault();
        setMessage('');
        setLoading(true);

        try {
            await AuthService.completeOAuth2Registration(temporaryToken, {
                name,
                phoneNumber,
                nickname,
                region,
                address,
                gender,
                birthDate,
            });
            navigate('/', { replace: true });
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
                <h2 className="text-center mb-4">추가 정보 입력</h2>

                <form onSubmit={handleCompleteRegistration}>
                    <p className="text-center text-muted mb-4">회원가입을 완료하려면 추가 정보를 입력해주세요.</p>

                    <div className="form-group mb-3">
                        <label htmlFor="name">이름</label>
                        <input
                            type="text"
                            className="form-control"
                            name="name"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
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
                        />
                    </div>

                    <div className="d-grid gap-2 mt-4">
                        <button className="btn btn-primary btn-block" disabled={loading || !temporaryToken}>
                            {loading ? '가입 처리 중...' : '가입 완료'}
                        </button>
                    </div>

                    {message && (
                        <div className="form-group mt-3">
                            <div className="alert alert-danger" role="alert">
                                {message}
                            </div>
                        </div>
                    )}
                </form>
            </div>
        </div>
    );
};

export default OAuth2RegisterPage;
