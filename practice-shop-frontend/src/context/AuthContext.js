import React, { createContext, useState, useContext, useEffect } from 'react';
import AuthService from '../services/auth.service';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [currentUser, setCurrentUser] = useState(undefined);

    useEffect(() => {
        // 앱 시작 시 localStorage에서 사용자 정보를 가져와 상태를 설정합니다.
        const user = AuthService.getCurrentUser();
        if (user) {
            setCurrentUser(user);
        } else {
            setCurrentUser(null); // 사용자가 없으면 null로 명시적 설정
        }
    }, []);

    const login = async (username, password) => {
        // 1. AuthService.login을 호출하여 서버에 로그인 요청 및 localStorage에 토큰 저장
        await AuthService.login(username, password);
        // 2. localStorage에서 최신 사용자 정보를 가져와 상태를 업데이트
        const updatedUser = AuthService.getCurrentUser();
        setCurrentUser(updatedUser);
        return updatedUser;
    };

    const logout = () => {
        AuthService.logout();
        setCurrentUser(null);
    };

    const value = { currentUser, login, logout, setCurrentUser };

    // currentUser가 undefined일 때 (초기 로딩 중)는 아무것도 렌더링하지 않아 깜빡임을 방지합니다.
    return (
        <AuthContext.Provider value={value}>
            {currentUser !== undefined && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    return useContext(AuthContext);
};