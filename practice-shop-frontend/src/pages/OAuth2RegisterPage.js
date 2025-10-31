import React, { useState, useEffect } from 'react';
import AuthService from '../services/auth.service';
import { useNavigate } from 'react-router-dom';

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
    const navigate = useNavigate();

    useEffect(() => {
        console.log('OAuth2RegisterPage mounted.');
        const fragment = window.location.hash.substring(1);
        const params = new URLSearchParams(fragment);
        const token = params.get('token');

        if (token) {
            console.log('Temporary token found:', token);
            setTemporaryToken(token);
        } else {
            console.log('Temporary token not found in fragment.');
            setMessage('Temporary token not found. Please try logging in again.');
            // Optionally redirect to login page after a delay
            // navigate('/login');
        }
    }, []);

    const handleCompleteRegistration = (e) => {
        e.preventDefault();
        setMessage('');

        AuthService.completeOAuth2Registration(
            temporaryToken,
            name,
            phoneNumber,
            nickname,
            region,
            address,
            gender,
            birthDate
        ).then(
            (response) => {
                // Assuming response contains accessToken and refreshToken
                if (response.accessToken) {
                    localStorage.setItem('user', JSON.stringify(response));
                    navigate('/');
                    window.location.reload();
                }
            },
            (error) => {
                const resMessage =
                    (error.response &&
                        error.response.data &&
                        error.response.data.message) ||
                    error.message ||
                    error.toString();
                setMessage(resMessage);
            }
        );
    };

    return (
        <div className="col-md-12">
            <div className="card card-container">
                <img
                    src="//ssl.gstatic.com/accounts/ui/avatar_2x.png"
                    alt="profile-img"
                    className="profile-img-card"
                />

                <form onSubmit={handleCompleteRegistration}>
                    <p>Please provide additional information to complete your registration.</p>

                    <div className="form-group">
                        <label htmlFor="name">Name</label>
                        <input
                            type="text"
                            className="form-control"
                            name="name"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="phoneNumber">Phone Number</label>
                        <input
                            type="text"
                            className="form-control"
                            name="phoneNumber"
                            value={phoneNumber}
                            onChange={(e) => setPhoneNumber(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="nickname">Nickname</label>
                        <input
                            type="text"
                            className="form-control"
                            name="nickname"
                            value={nickname}
                            onChange={(e) => setNickname(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="region">Region</label>
                        <input
                            type="text"
                            className="form-control"
                            name="region"
                            value={region}
                            onChange={(e) => setRegion(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="address">Address</label>
                        <input
                            type="text"
                            className="form-control"
                            name="address"
                            value={address}
                            onChange={(e) => setAddress(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="gender">Gender</label>
                        <select
                            className="form-control"
                            name="gender"
                            value={gender}
                            onChange={(e) => setGender(e.target.value)}
                            required
                        >
                            <option value="">Select Gender</option>
                            <option value="MALE">Male</option>
                            <option value="FEMALE">Female</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="birthDate">Birth Date</label>
                        <input
                            type="date"
                            className="form-control"
                            name="birthDate"
                            value={birthDate}
                            onChange={(e) => setBirthDate(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <button className="btn btn-primary btn-block">Complete Registration</button>
                    </div>

                    {message && (
                        <div className="form-group">
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
