import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();

    useEffect(() => {
        console.log('OAuth2RedirectHandler mounted.');
        const fragment = window.location.hash.substring(1); // '#' 제거
        console.log('Fragment:', fragment);
        let token = null;
        if (fragment.startsWith('token=')) {
            token = fragment.substring('token='.length);
        }

        if (token) {
            console.log('Token found in fragment:', token);
            try {
                const decodedToken = jwtDecode(token);
                const user = {
                    accessToken: token,
                    email: decodedToken.sub,
                    username: decodedToken.username
                };
                console.log('User object to store:', user);
                localStorage.setItem('user', JSON.stringify(user));
                console.log('User stored. Attempting delayed navigation to /...');
                // Temporarily remove immediate navigate to debug fragment stripping
                setTimeout(() => {
                    navigate('/');
                }, 100);
            } catch (error) {
                console.error('Error decoding token or storing user:', error);
                navigate('/login');
            }
        } else {
            console.log('Token not found in fragment. Navigating to /login...');
            navigate('/login');
        }
    }, [navigate]);

  return <div>로그인 중입니다...</div>;
};

export default OAuth2RedirectHandler;
