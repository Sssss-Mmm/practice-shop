import React from 'react';
import { Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import AdminSidebar from './AdminSidebar';
import './AdminLayout.css';

const AdminLayout = () => {
    const { currentUser } = useAuth();

    return (
        <div className="admin-container">
            <AdminSidebar />
            <div className="admin-main">
                <header className="admin-header">
                    <div className="admin-user-info">
                        관리자: <strong>{currentUser?.email || 'Admin'}</strong>
                    </div>
                </header>
                <main className="admin-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
};

export default AdminLayout;
