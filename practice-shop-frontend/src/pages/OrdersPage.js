import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import OrderService from '../services/order.service';
import './OrdersPage.css';

const OrdersPage = () => {
    const [orders, setOrders] = useState([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const pageSize = 10;

    const loadOrders = (targetPage) => {
        setLoading(true);
        OrderService.getOrders(targetPage, pageSize)
            .then((response) => {
                const data = response.data;
                if (!data) {
                    setOrders([]);
                    setPage(0);
                    setTotalPages(1);
                    return;
                }
                if (Array.isArray(data)) {
                    setOrders(data);
                    setPage(0);
                    setTotalPages(1);
                } else {
                    setOrders(data.content ?? []);
                    setPage(data.page ?? targetPage);
                    const pages = data.totalPages ?? 1;
                    setTotalPages(pages > 0 ? pages : 1);
                }
                setError(null);
            })
            .catch((err) => {
                const status = err.response?.status;
                const resMessage =
                    status === 401
                        ? '주문 내역은 로그인 후 확인할 수 있습니다.'
                        : (err.response && err.response.data && err.response.data.message) ||
                          err.message ||
                          err.toString();
                setError(resMessage);
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        loadOrders(0);
    }, []);

    const handlePageChange = (nextPage) => {
        if (nextPage < 0 || nextPage >= totalPages) {
            return;
        }
        loadOrders(nextPage);
    };

    if (loading) {
        return (
            <div className="orders-container">
                <h1>주문 내역</h1>
                <p>주문 목록을 불러오는 중입니다...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="orders-container">
                <h1>주문 내역</h1>
                <p className="orders-error">{error}</p>
                {error.includes('로그인') && (
                    <button className="orders-button" onClick={() => navigate('/login')}>
                        로그인하기
                    </button>
                )}
            </div>
        );
    }

    if (!orders.length) {
        return (
            <div className="orders-container">
                <h1>주문 내역</h1>
                <p>주문 내역이 없습니다.</p>
                <button className="orders-button" onClick={() => navigate('/')}>쇼핑 계속하기</button>
            </div>
        );
    }

    return (
        <div className="orders-container">
            <h1>주문 내역</h1>
            <table className="orders-table">
                <thead>
                    <tr>
                        <th>주문번호</th>
                        <th>주문일</th>
                        <th>상태</th>
                        <th>결제상태</th>
                        <th>총금액</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    {orders.map((order) => (
                        <tr key={order.orderId}>
                            <td>{order.orderId}</td>
                            <td>{order.createdAt ? new Date(order.createdAt).toLocaleString() : '-'}</td>
                            <td>{order.orderStatus}</td>
                            <td>{order.paymentStatus}</td>
                            <td>{order.totalPrice ? Number(order.totalPrice).toLocaleString() : 0}원</td>
                            <td>
                                <Link className="orders-link" to={`/orders/${order.orderId}`}>
                                    상세보기
                                </Link>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            {totalPages > 1 && (
                <div className="orders-pagination">
                    <button disabled={page === 0} onClick={() => handlePageChange(page - 1)}>
                        이전
                    </button>
                    <span>{page + 1} / {totalPages}</span>
                    <button disabled={page >= totalPages - 1} onClick={() => handlePageChange(page + 1)}>
                        다음
                    </button>
                </div>
            )}
        </div>
    );
};

export default OrdersPage;
