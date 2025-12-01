import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import OrderService from '../services/order.service';
import './OrderDetailPage.css';

/**
 * 특정 주문의 상세 내역을 보여주는 페이지 컴포넌트입니다.
 * @returns {JSX.Element} OrderDetailPage 컴포넌트
 */
const OrderDetailPage = () => {
    const { orderId } = useParams();
    const navigate = useNavigate();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    /**
     * 상대적인 이미지 URL을 절대 URL로 변환합니다.
     * 환경 변수에 설정된 API 기본 URL을 사용하며, URL이 없는 경우 플레이스홀더 이미지를 반환합니다.
     * @param {string} relativeUrl - 변환할 상대 이미지 URL
     * @returns {string} 절대 이미지 URL
     */
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

    /**
     * 컴포넌트가 마운트되거나 'orderId'가 변경될 때, 해당 ID의 주문 상세 정보를 서버에서 가져옵니다.
     * 성공 시 order 상태를 업데이트하고, 실패 시 에러 상태를 설정합니다.
     * @listens orderId
     */
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
            <div className="container my-5 text-center">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
                <p className="mt-2">주문 정보를 불러오는 중입니다...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="container my-5">
                <div className="alert alert-danger text-center">
                    <p className="mb-0">{error}</p>
                </div>
                {error.includes('로그인') && (
                    <div className="text-center mt-3">
                    <button className="btn btn-primary" onClick={() => navigate('/login')}>
                        로그인하기
                    </button>
                    </div>
                )}
            </div>
        );
    }

    if (!order) {
        return (
            <div className="container my-5">
                <div className="alert alert-warning text-center">
                    <p className="mb-0">주문 정보를 찾을 수 없습니다.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="container my-5 order-detail-container">
            <h1 className="mb-4 text-center">주문 상세 내역</h1>
            <div className="card mb-4 shadow-sm">
                <div className="card-header"><h3>주문 정보</h3></div>
                <div className="card-body">
                    <p><strong>주문번호:</strong> {order.orderId}</p>
                    <p><strong>주문일:</strong> {order.createdAt ? new Date(order.createdAt).toLocaleString('ko-KR') : '-'}</p>
                    <p><strong>주문 상태:</strong> <span className="badge bg-info text-dark">{order.orderStatus}</span></p>
                    <p><strong>결제 상태:</strong> <span className="badge bg-success">{order.paymentStatus}</span></p>
                    <p><strong>결제 수단:</strong> {order.paymentMethod}</p>
                </div>
            </div>

            <div className="card mb-4 shadow-sm">
                <div className="card-header"><h3>배송 정보</h3></div>
                <div className="card-body">
                    <p><strong>수령인:</strong> {order.recipientName}</p>
                    <p><strong>연락처:</strong> {order.contactNumber}</p>
                    <p><strong>주소:</strong> {order.shippingAddress} {order.postalCode && `(${order.postalCode})`}</p>
                    {order.deliveryInstructions && <p><strong>요청 사항:</strong> {order.deliveryInstructions}</p>}
                </div>
            </div>

            <div className="card shadow-sm">
                <div className="card-header"><h3>주문 상품</h3></div>
                <div className="card-body">
                    <div className="table-responsive">
                        <table className="table table-hover align-middle">
                            <thead className="table-light">
                                <tr>
                                    <th scope="col" style={{ width: '50%' }}>상품</th>
                                    <th scope="col" className="text-end">단가</th>
                                    <th scope="col" className="text-center">수량</th>
                                    <th scope="col" className="text-end">합계</th>
                                </tr>
                            </thead>
                            <tbody>
                                {order.items?.map((item) => (
                                    <tr key={item.orderItemId}>
                                        <td>
                                            <div className="d-flex align-items-center">
                                                <img src={resolveImageUrl(item.imageUrl)} alt={item.productName} className="order-item-image me-3" />
                                                <span>{item.productName}</span>
                                            </div>
                                        </td>
                                        <td className="text-end">{item.price.toLocaleString()}원</td>
                                        <td className="text-center">{item.quantity}</td>
                                        <td className="text-end">{item.lineTotal.toLocaleString()}원</td>
                                    </tr>
                                ))}
                            </tbody>
                            <tfoot>
                                <tr className="table-light">
                                    <td colSpan="3" className="text-end"><strong>총 합계</strong></td>
                                    <td className="text-end"><strong>{Number(order.totalPrice ?? 0).toLocaleString()}원</strong></td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>
            </div>

            <div className="text-center mt-4">
            <button className="btn btn-primary btn-lg" onClick={() => navigate('/orders')}>
                주문 목록으로 돌아가기
            </button>
            </div>
        </div>
    );
};

export default OrderDetailPage;
