import React, { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

const OAuth2RedirectHandler = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        const token = searchParams.get('token');

        if (token) {
            const decodedToken = jwtDecode(token);
            const user = {
                accessToken: token,
                email: decodedToken.sub,
                username: decodedToken.username
            };
            localStorage.setItem('user', JSON.stringify(user));
            navigate('/');
        } else {
            navigate('/login');
        }
    }, [navigate, searchParams]);

    return <div>Loading...</div>;
};

export default OAuth2RedirectHandler;
