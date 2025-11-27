import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from '../services/auth.service';
import CartService from '../services/cart.service';

const Header = () => {
    const [currentUser, setCurrentUser] = useState(() => AuthService.getCurrentUser());
    const [showAdminBoard, setShowAdminBoard] = useState(() =>
        !!AuthService.getCurrentUser()?.roles?.includes('ADMIN')
    );
    const [cartCount, setCartCount] = useState(0);
    const navigate = useNavigate();

    useEffect(() => {
        const applyAuthState = () => {
            const user = AuthService.getCurrentUser();
            setCurrentUser(user);
            setShowAdminBoard(!!user?.roles?.includes('ADMIN'));
        };

        const updateCartCountFromResponse = (data) => {
            if (data && typeof data.totalItems === 'number') {
                setCartCount(data.totalItems);
            } else if (data && Array.isArray(data.items)) {
                setCartCount(data.items.reduce((sum, item) => sum + (item.quantity ?? 0), 0));
            } else {
                setCartCount(0);
            }
        };

        const fetchInitialCart = () => {
            const user = AuthService.getCurrentUser();
            if (!user) {
                setCartCount(0);
                return;
            }
            CartService.getCart().then((response) => {
                updateCartCountFromResponse(response.data);
            }).catch(() => setCartCount(0));
        };

        applyAuthState();
        fetchInitialCart();

        const unsubscribeAuth = AuthService.onAuthChange(() => {
            applyAuthState();
            fetchInitialCart();
        });

        const unsubscribeCart = CartService.onChange((payload) => {
            if (payload) {
                updateCartCountFromResponse(payload);
            } else {
                fetchInitialCart();
            }
        });

        return () => {
            if (unsubscribeAuth) {
                unsubscribeAuth();
            }
            if (unsubscribeCart) {
                unsubscribeCart();
            }
        };
    }, []);

    const logOut = async (event) => {
        event.preventDefault();
        await AuthService.logout();
        setCartCount(0);
        navigate('/login', { replace: true });
    };

    return (
        <header>
            <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
                <div className="container">
                    <Link className="navbar-brand" to="/">Practice Shop</Link>
                    <div className="collapse navbar-collapse">
                        <ul className="navbar-nav ms-auto mb-2 mb-lg-0">
                            <li className="nav-item">
                                <Link className="nav-link" to="/">Home</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link" to="/cart">
                                    Cart{cartCount > 0 ? ` (${cartCount})` : ''}
                                </Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link" to="/orders">
                                    Orders
                                </Link>
                            </li>
                            {showAdminBoard && (
                                <li className="nav-item dropdown">
                                    <a className="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                        Admin
                                    </a>
                                    <ul className="dropdown-menu dropdown-menu-end">
                                        <li><Link className="dropdown-item" to="/product-registration">상품 등록</Link></li>
                                        <li><Link className="dropdown-item" to="/admin/venues">공연장 관리</Link></li>
                                        <li><Link className="dropdown-item" to="/admin/events">공연 관리</Link></li>
                                        <li><Link className="dropdown-item" to="/admin/showtimes">회차 관리</Link></li>
                                        <li><Link className="dropdown-item" to="/admin/seats">좌석 관리</Link></li>
                                    </ul>
                                </li>
                            )}

                            {currentUser ? (
                                <>
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/profile">{currentUser.username}</Link>
                                    </li>
                                    <li className="nav-item">
                                        <a href="/login" className="nav-link" onClick={logOut}>
                                            Logout
                                        </a>
                                    </li>
                                </>
                            ) : (
                                <>
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/login">Login</Link>
                                    </li>
                                    <li className="nav-item">
                                        <Link className="nav-link" to="/signup">Signup</Link>
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
