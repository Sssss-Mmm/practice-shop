import React, { useMemo } from 'react';
import { Link } from 'react-router-dom';
import ProductRegistrationForm from '../components/ProductRegistrationForm';
import AuthService from '../services/auth.service';

const ProductRegistrationPage = () => {
    const user = AuthService.getCurrentUser();
    const isAdmin = useMemo(() => {
        const roles = user?.roles?.map((role) => role.replace(/^ROLE_/, ''));
        return roles?.includes('ADMIN');
    }, [user]);

    if (!isAdmin) {
        return (
            <div className="alert alert-warning">
                <h2 className="h5">접근 권한이 없습니다.</h2>
                <p>상품 등록은 관리자만 이용할 수 있습니다.</p>
                <Link className="btn btn-outline-primary mt-3" to="/">
                    홈으로 이동
                </Link>
            </div>
        );
    }

    return (
        <div>
            <h1>상품 등록</h1>
            <ProductRegistrationForm />
        </div>
    );
};

export default ProductRegistrationPage;
