import React, { useState, useEffect } from 'react';
import AuthService from '../services/auth.service';

const HomePage = () => {
    const [currentUser, setCurrentUser] = useState(undefined);

    useEffect(() => {
        const user = AuthService.getCurrentUser();
        console.log(user);
        if (user) {
            setCurrentUser(user);
        }
    }, []);

    return (
        <div>
            {currentUser ? (
                <h1>Welcome, {currentUser.username}!</h1>
            ) : (
                <h1>Welcome to the Practice Shop</h1>
            )}
            <p>This is the homepage.</p>
        </div>
    );
};

export default HomePage;
