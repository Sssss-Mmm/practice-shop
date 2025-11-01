import React, { useState, useEffect } from 'react';
import AuthService from '../services/auth.service';

const ProfilePage = () => {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [message, setMessage] = useState('');

    useEffect(() => {
        AuthService.getUserProfile().then(
            response => {
                setProfile(response.data);
                setLoading(false);
            },
            error => {
                const _content = (error.response && error.response.data && error.response.data.message) || error.message || error.toString();
                setProfile(null);
                setLoading(false);
                setMessage(_content);
            }
        );
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProfile({ ...profile, [name]: value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        AuthService.updateUserProfile(profile).then(
            response => {
                setMessage('Profile updated successfully!');
            },
            error => {
                const _content = (error.response && error.response.data && error.response.data.message) || error.message || error.toString();
                setMessage(_content);
            }
        );
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    if (!profile) {
        return <div>{message}</div>;
    }

    return (
        <div className="container">
            <header className="jumbotron">
                <h3>
                    <strong>{profile.name}</strong> Profile
                </h3>
            </header>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="email">Email</label>
                    <input type="text" className="form-control" id="email" value={profile.email} disabled />
                </div>
                <div className="form-group">
                    <label htmlFor="name">Name</label>
                    <input type="text" className="form-control" id="name" value={profile.name} disabled />
                </div>
                <div className="form-group">
                    <label htmlFor="nickname">Nickname</label>
                    <input type="text" className="form-control" id="nickname" name="nickname" value={profile.nickname} onChange={handleChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="phoneNumber">Phone Number</label>
                    <input type="text" className="form-control" id="phoneNumber" name="phoneNumber" value={profile.phoneNumber} onChange={handleChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="region">Region</label>
                    <input type="text" className="form-control" id="region" name="region" value={profile.region} onChange={handleChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="address">Address</label>
                    <input type="text" className="form-control" id="address" name="address" value={profile.address} onChange={handleChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="gender">Gender</label>
                    <input type="text" className="form-control" id="gender" name="gender" value={profile.gender} onChange={handleChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="birthDate">Birth Date</label>
                    <input type="text" className="form-control" id="birthDate" name="birthDate" value={profile.birthDate} onChange={handleChange} />
                </div>
                <div className="form-group">
                    <button type="submit" className="btn btn-primary btn-block">Update Profile</button>
                </div>
                {message && (
                    <div className="form-group">
                        <div className="alert alert-info" role="alert">
                            {message}
                        </div>
                    </div>
                )}
            </form>
        </div>
    );
};

export default ProfilePage;
