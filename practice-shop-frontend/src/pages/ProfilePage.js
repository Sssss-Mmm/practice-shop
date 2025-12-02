import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/auth.service';
import './AuthPages.css';

const ProfilePage = () => {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        AuthService.getUserProfile()
            .then(response => {
                setProfile(response.data);
                setLoading(false);
            })
            .catch(err => {
                const resMessage =
                    (err.response && err.response.data && err.response.data.message) ||
                    err.message ||
                    err.toString();
                setError(resMessage);
                setLoading(false);
                if (err.response && err.response.status === 401) {
                    navigate('/login');
                }
            });
    }, [navigate]);

    if (loading) {
        return <div className="container my-5 text-center">프로필 정보를 불러오는 중입니다...</div>;
    }

    if (error) {
        return <div className="container my-5 alert alert-danger">{error}</div>;
    }

    if (!profile) {
        return <div className="container my-5 alert alert-warning">프로필 정보를 찾을 수 없습니다.</div>;
    }

    return (
        <div className="ticket-auth-container">
            <div className="ticket-auth-card" style={{ maxWidth: '600px' }}>
                <h2 className="text-center mb-4">마이페이지</h2>
                <div className="card">
                    <div className="card-body">
                        <p><strong>이메일:</strong> {profile.email}</p>
                        <p><strong>이름:</strong> {profile.name}</p>
                        <p><strong>닉네임:</strong> {profile.nickname}</p>
                        <p><strong>전화번호:</strong> {profile.phoneNumber}</p>
                        <p><strong>지역:</strong> {profile.region}</p>
                        <p><strong>상세주소:</strong> {profile.address}</p>
                        <p><strong>성별:</strong> {profile.gender === 'MALE' ? '남성' : '여성'}</p>
                        <p><strong>생년월일:</strong> {profile.birthDate}</p>
                        <p><strong>가입일:</strong> {new Date(profile.createdAt).toLocaleDateString()}</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProfilePage;