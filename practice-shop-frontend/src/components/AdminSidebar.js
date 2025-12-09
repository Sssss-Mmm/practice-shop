import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AdminLayout.css';
import { FaChartBar, FaCalendarAlt, FaMapMarkerAlt, FaClock, FaChair, FaMagic, FaExchangeAlt } from 'react-icons/fa';

const AdminSidebar = () => {
    const { logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <aside className="admin-sidebar">
            <div className="sidebar-header">
                <NavLink to="/" className="sidebar-brand">TICKET ADMIN</NavLink>
            </div>
            
            <nav className="sidebar-nav">
                <NavLink to="/admin/wizard" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <FaMagic /> 올인원 등록 (Wizard)
                </NavLink>
                <NavLink to="/admin/events" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <FaCalendarAlt /> 공연 관리
                </NavLink>
                <NavLink to="/admin/venues" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <FaMapMarkerAlt /> 공연장 관리
                </NavLink>
                <NavLink to="/admin/showtimes" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <FaClock /> 회차 관리
                </NavLink>
                <NavLink to="/admin/seats" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <FaChair /> 좌석 관리
                </NavLink>
                <NavLink to="/admin/seat-mapper" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
                    <FaExchangeAlt /> 좌석 매퍼
                </NavLink>
                <div style={{height: '1px', background: '#374151', margin: '10px 24px'}}></div>
                <NavLink to="/" className="nav-item">
                    <FaExchangeAlt /> 사이트 바로가기
                </NavLink>
            </nav>

            <div className="sidebar-footer">
                <button onClick={handleLogout} className="logout-btn">로그아웃</button>
            </div>
        </aside>
    );
};

export default AdminSidebar;
