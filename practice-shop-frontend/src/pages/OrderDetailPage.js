import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import OrderService from '../services/order.service';
import './OrderDetailPage.css';

const OrderDetailPage = () => {
    const { orderId } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const resolveImageUrl = (relativeUrl) => {
        if (!relativeUrl) {
            return '/placeholder.png';
        }
        const encodedPath = encodeURI(relativeUrl);
        if (encodedPath.startsWith('http://') || encodedPath.startsWith('https://')) {
            return encodedPath;
        }
        const apiBase = process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8084';
        return `${apiBase}${encodedPath}`;
    };

    useEffect(() => {
        setLoading(true);
        OrderService.getOrderDetail(orderId)
            .then((response) => {
                setOrder(response.data);
                setError(null);
            })
            .catch((err) => {
                const status = err.response?.status;
                const resMessage =
                    status === 401
                        ? '주문 상세는 로그인 후 확인할 수 있습니다.'
                        : (err.response && err.response.data && err.response.data.message) ||
                          err.message ||
                          err.toString();
                setError(resMessage);
            })
            .finally(() => setLoading(false));
    }, [orderId]);

    if (loading) {
        return (
            <div className="order-detail-container">
                <h1>주문 상세</h1>
                <p>주문 정보를 불러오는 중입니다...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="order-detail-container">
                <h1>주문 상세</h1>
                <p className="order-detail-error">{error}</p>
                {error.includes('로그인') && (
                    <button className="order-detail-button" onClick={() => navigate('/login')}>
                        로그인하기
                    </button>
                )}
            </div>
        );
    }

    if (!order) {
        return (
            <div className="order-detail-container">
                <h1>주문 상세</h1>
                <p>주문 정보를 찾을 수 없습니다.</p>
            </div>
        );
    }

    return (
        <div className="order-detail-container">
            <h1>주문 상세</h1>
            <section className="order-info">
                <p><strong>주문번호:</strong> {order.orderId}</p>
                <p><strong>주문일:</strong> {order.createdAt ? new Date(order.createdAt).toLocaleString() : '-'}</p>
                <p><strong>주문 상태:</strong> {order.orderStatus}</p>
                <p><strong>결제 상태:</strong> {order.paymentStatus}</p>
                <p><strong>결제 수단:</strong> {order.paymentMethod}</p>
            </section>

            <section className="order-shipping">
                <h2>배송 정보</h2>
                <p><strong>수령인:</strong> {order.recipientName}</p>
                <p><strong>연락처:</strong> {order.contactNumber}</p>
                <p><strong>주소:</strong> {order.shippingAddress}</p>
                {order.postalCode && <p><strong>우편번호:</strong> {order.postalCode}</p>}
                {order.deliveryInstructions && <p><strong>요청 사항:</strong> {order.deliveryInstructions}</p>}
            </section>

            <section className="order-items">
                <h2>주문 상품</h2>
                <table>
                    <thead>
                        <tr>
                            <th>상품</th>
                            <th>단가</th>
                            <th>수량</th>
                            <th>합계</th>
                        </tr>
                    </thead>
                    <tbody>
                        {order.items?.map((item) => (
                            <tr key={item.orderItemId}>
                                <td className="order-item-info">
                                    <img src={resolveImageUrl(item.imageUrl)} alt={item.productName} />
                                    <span>{item.productName}</span>
                                </td>
                                <td>{item.price.toLocaleString()}원</td>
                                <td>{item.quantity}</td>
                                <td>{item.lineTotal.toLocaleString()}원</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                <p className="order-total">총 합계: {Number(order.totalPrice ?? 0).toLocaleString()}원</p>
            </section>

            <button className="order-detail-button" onClick={() => navigate('/orders')}>
                주문 목록으로 돌아가기
            </button>
        </div>
    );
};

export default OrderDetailPage;
