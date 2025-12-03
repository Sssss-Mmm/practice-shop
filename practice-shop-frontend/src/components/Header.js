import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './Header.css';

const Header = () => {
    const { currentUser, logout } = useAuth();
    const { cart } = useCart();
    const navigate = useNavigate();

    const isAdmin = (currentUser?.roles || []).some((role) =>
        String(role).toUpperCase() === 'ADMIN' || String(role).toUpperCase() === 'ROLE_ADMIN'
    );
    const cartItemCount = cart?.totalItems ?? 0;

    const handleLogout = () => {
        logout();
        navigate('/login', { replace: true });
    };

    return (
        <header className="app-header">
            <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
                <div className="container">
                    <Link to="/" className="navbar-brand ticket-brand">TICKET ZONE</Link>
                    <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                        <span className="navbar-toggler-icon"></span>
                    </button>
                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                            <li className="nav-item">
                                <Link className="nav-link" to="/">공연/상품 보기</Link>
                            </li>
                        </ul>
                        <ul className="navbar-nav align-items-center">
                            {currentUser ? (
                                <>
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/cart">
                                            장바구니 {cartItemCount > 0 && <span className="badge bg-danger ms-1">{cartItemCount}</span>}
                                        </Link>
                                    </li>
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/orders">주문내역</Link>
                                    </li>
                                    {isAdmin && (
                                        <li className="nav-item dropdown">
                                            <a className="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                Admin
                                            </a>
                                            <ul className="dropdown-menu dropdown-menu-dark dropdown-menu-end">
                                                <li><Link className="dropdown-item" to="/product-registration">공연 등록</Link></li>
                                                <li><Link className="dropdown-item" to="/admin/events">공연 관리</Link></li>
                                                <li><Link className="dropdown-item" to="/admin/venues">공연장 관리</Link></li>
                                                <li><Link className="dropdown-item" to="/admin/showtimes">회차 관리</Link></li>
                                                <li><Link className="dropdown-item" to="/admin/seats">좌석 관리</Link></li>
                                            </ul>
                                        </li>
                                    )}
                                    <li className="nav-item dropdown">
                                        <a className="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            {currentUser.username}
                                        </a>
                                        <ul className="dropdown-menu dropdown-menu-dark dropdown-menu-end">
                                            <li><Link className="dropdown-item" to="/profile">마이페이지</Link></li>
                                            <li><hr className="dropdown-divider" /></li>
                                            <li><button onClick={handleLogout} className="dropdown-item">로그아웃</button></li>
                                        </ul>
                                    </li>
                                </>
                            ) : (
                                <>
                                    <li className="nav-item">
                                        <Link to="/login" className="nav-link">로그인</Link>
                                    </li>
                                    <li className="nav-item ms-2">
                                        <Link to="/signup" className="btn btn-primary btn-sm">회원가입</Link>
                                    </li>
                                </>
                            )}
                        </ul>
                    </div>
                </div>
            </nav>
        </header>
    );
};

export default Header;
