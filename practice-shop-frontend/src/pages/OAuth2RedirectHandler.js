import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import AuthService from '../services/auth.service'; // 누락된 import 구문 추가

/**
 * OAuth2 로그인 성공 후 리디렉션을 처리하는 핸들러 컴포넌트입니다.
 * URL에서 토큰을 추출하여 사용자를 로그인 상태로 만듭니다.
 */
const OAuth2RedirectHandler = () => {
    const { setCurrentUser } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const [error, setError] = useState(null);

    useEffect(() => {
        // 백엔드가 토큰을 URL fragment(#)에 담아 전달하므로, location.search 대신 location.hash를 파싱합니다.
        const fragment = location.hash.substring(1);
        const params = new URLSearchParams(fragment);
        // 백엔드 로그에 따르면 파라미터 이름이 'token'이므로 'accessToken' 대신 'token'을 사용합니다.
        const accessToken = params.get('token');
        // 백엔드가 refreshToken을 함께 보내주지 않으므로, 일단 null로 처리합니다.
        const refreshToken = params.get('refreshToken') || null;

        if (accessToken) { // accessToken만 있어도 로그인 처리 진행
            // 1. 받아온 토큰을 localStorage에 저장합니다.
            const user = AuthService.persistUser(accessToken, refreshToken);
            // 2. AuthContext의 상태를 업데이트하여 앱 전체에 로그인 상태를 알립니다.
            setCurrentUser(user);
            // 3. 사용자를 메인 페이지로 리디렉션합니다.
            navigate('/', { replace: true });
        } else {
            // 토큰이 없는 경우 에러 처리
            setError('로그인 정보를 받아오지 못했습니다. 다시 시도해 주세요.');
        }
    }, [location, navigate, setCurrentUser]);

    if (error) {
        return <div className="container my-5 alert alert-danger">{error}</div>;
    }

    return <div className="container my-5 text-center">로그인 중입니다...</div>;
};

export default OAuth2RedirectHandler;