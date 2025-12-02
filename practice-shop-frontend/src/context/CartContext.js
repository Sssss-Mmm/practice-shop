import React, { createContext, useState, useContext, useEffect } from 'react';
import CartService from '../services/cart.service';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export const CartProvider = ({ children }) => {
    const { currentUser } = useAuth();
    const [cart, setCart] = useState({ items: [], totalItems: 0, totalPrice: 0 });

    const fetchCart = async () => {
        if (!currentUser) {
            setCart({ items: [], totalItems: 0, totalPrice: 0 });
            return;
        }
        try {
            const response = await CartService.getCart();
            setCart(response.data ?? { items: [], totalItems: 0, totalPrice: 0 });
        } catch (error) {
            // 401 (Unauthorized) 등의 에러 발생 시 장바구니 초기화
            setCart({ items: [], totalItems: 0, totalPrice: 0 });
        }
    };

    useEffect(() => {
        fetchCart();

        // CartService에서 발생하는 변경 이벤트를 구독
        const unsubscribe = CartService.onChange((newCartData) => {
            if (newCartData) {
                setCart(newCartData);
            } else {
                fetchCart();
            }
        });

        return () => unsubscribe(); // 컴포넌트 언마운트 시 구독 해제
    }, [currentUser]); // currentUser가 변경될 때마다 (로그인/로그아웃) 장바구니를 다시 불러옵니다.

    const value = { cart, fetchCart };

    return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export const useCart = () => {
    return useContext(CartContext);
};