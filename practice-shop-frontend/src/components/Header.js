import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './Header.css';

const Header = () => {
    const { currentUser, logout } = useAuth();
    const { cart } = useCart();
    const navigate = useNavigate();
    const [searchInput, setSearchInput] = useState('');

    const isAdmin = (currentUser?.roles || []).some((role) =>
        String(role).toUpperCase() === 'ADMIN' || String(role).toUpperCase() === 'ROLE_ADMIN'
    );
    const cartItemCount = cart?.totalItems ?? 0;

    const handleLogout = () => {
        logout();
        navigate('/login', { replace: true });
    };

    const handleCategorySelect = (category) => {
        const params = new URLSearchParams();
        if (category && category !== 'ALL') {
            params.set('category', category);
        }
        navigate(`/?${params.toString()}`);
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        const params = new URLSearchParams();
        if (searchInput.trim()) params.set('q', searchInput.trim());
        navigate(`/?${params.toString()}`);
    };

    return (
        <header className="app-header">
            <div className="header-topbar">
                <div className="container d-flex justify-content-end gap-3">
                    {currentUser ? (
                        <>
                            <Link to="/my-tickets" className="toplink">예매확인</Link>
                            <Link to="/orders" className="toplink">주문내역</Link>
                            <button className="toplink-btn" onClick={handleLogout}>로그아웃</button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="toplink">로그인</Link>
                            <Link to="/signup" className="toplink">회원가입</Link>
                        </>
                    )}
                </div>
            </div>

            <nav className="navbar navbar-expand-lg bg-white main-nav">
                <div className="container align-items-center">
                    <Link to="/" className="navbar-brand ticket-brand text-dark">TICKET ZONE</Link>
                    <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                        <span className="navbar-toggler-icon"></span>
                    </button>
                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav me-auto mb-2 mb-lg-0 align-items-center main-tabs">
                            <li className="nav-item">
                                <button className="tab-link active" onClick={() => handleCategorySelect('STAGE')}>공연/전시</button>
                            </li>
                            <li className="nav-item">
                                <button className="tab-link" onClick={() => handleCategorySelect('SPORTS')}>스포츠</button>
                            </li>
                        </ul>
                        <form className="search-bar ms-lg-3" onSubmit={handleSearchSubmit}>
                            <input
                                type="search"
                                placeholder="검색어를 입력해 주세요"
                                value={searchInput}
                                onChange={(e) => setSearchInput(e.target.value)}
                            />
                            <button type="submit">검색</button>
                        </form>
                        <ul className="navbar-nav align-items-center ms-3">
                            {currentUser && (
                                <>
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/cart">
                                            장바구니 {cartItemCount > 0 && <span className="badge bg-danger ms-1">{cartItemCount}</span>}
                                        </Link>
                                    </li>
                                    {isAdmin && (
                                        <li className="nav-item dropdown">
                                            <a className="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                Admin
                                            </a>
                                            <ul className="dropdown-menu dropdown-menu-end">
                                                <li><Link className="dropdown-item" to="/admin/wizard">올인원 등록(위저드)</Link></li>
                                                <li><Link className="dropdown-item" to="/admin/seat-mapper">좌석 매퍼</Link></li>
                                                <li><Link className="dropdown-item" to="/admin/events">공연 등록/관리</Link></li>
                                                <li><Link className="dropdown-item" to="/admin/venues">공연장 관리</Link></li>
                                                <li><Link className="dropdown-item" to="/admin/showtimes">회차 관리</Link></li>
                                                <li><Link className="dropdown-item" to="/admin/seats">좌석 관리</Link></li>
                                            </ul>
                                        </li>
                                    )}
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
