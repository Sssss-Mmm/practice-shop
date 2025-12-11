import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from '../services/auth.service';
import TicketingService from '../services/ticketing.service';
import './MyReservationsPage.css';

const MyReservationsPage = () => {
    const navigate = useNavigate();
    const [reservations, setReservations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const user = AuthService.getCurrentUser();
        if (!user) {
            navigate('/login');
            return;
        }

        fetchReservations();
    }, [navigate]);

    const fetchReservations = async () => {
        setLoading(true);
        try {
            const response = await TicketingService.getMyReservations();
            setReservations(response.data);
            setError(null);
        } catch (err) {
            console.error("Failed to fetch reservations:", err);
            setError("예매 내역을 불러오는 데 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = async (reservationId) => {
        if (!window.confirm("정말로 예매를 취소하시겠습니까?")) {
            return;
        }

        try {
            await TicketingService.cancelReservation(reservationId);
            alert("예매가 취소되었습니다.");
            fetchReservations(); // Refresh list
        } catch (err) {
            console.error("Failed to cancel reservation:", err);
            alert(err.response?.data?.message || "예매 취소에 실패했습니다.");
        }
    };

    if (loading) return <div className="container mt-5"><div>로딩 중...</div></div>;
    if (error) return <div className="container mt-5"><div className="alert alert-danger">{error}</div></div>;

    return (
        <div className="container mt-5">
            <h2 className="mb-4">나의 예매 내역</h2>
            {reservations.length === 0 ? (
                <div className="text-center py-5">
                    <p>예매 내역이 없습니다.</p>
                    <button className="btn btn-primary" onClick={() => navigate('/')}>공연 보러 가기</button>
                </div>
            ) : (
                <div className="row">
                    {reservations.map(reservation => (
                        <div key={reservation.reservationId} className="col-md-6 mb-4">
                            <div className="card h-100 shadow-sm reservation-card">
                                <div className="card-header d-flex justify-content-between align-items-center">
                                    <span className={`badge ${reservation.status === 'CANCELLED' ? 'bg-secondary' : 'bg-success'}`}>
                                        {reservation.status === 'CANCELLED' ? '취소됨' : 
                                         reservation.status === 'PENDING_PAYMENT' ? '결제 대기' : '예매 완료'}
                                    </span>
                                    <small className="text-muted">{new Date(reservation.reservedAt).toLocaleDateString()}</small>
                                </div>
                                <div className="card-body">
                                    <h5 className="card-title">{reservation.eventName}</h5>
                                    <h6 className="card-subtitle mb-2 text-muted">{reservation.venueName}</h6>
                                    <p className="card-text">
                                        <strong>일시:</strong> {new Date(reservation.showtime).toLocaleString()}<br/>
                                        <strong>총 금액:</strong> {reservation.totalPrice.toLocaleString()}원
                                    </p>
                                    <hr />
                                    <h6>좌석 정보</h6>
                                    <ul className="list-unstyled">
                                        {reservation.seatDetails.map((seat, idx) => (
                                            <li key={idx}>- {seat}</li>
                                        ))}
                                    </ul>
                                </div>
                                <div className="card-footer bg-transparent border-top-0 d-flex justify-content-end">
                                    {reservation.status !== 'CANCELLED' && (
                                        <button 
                                            className="btn btn-outline-danger btn-sm"
                                            onClick={() => handleCancel(reservation.reservationId)}
                                        >
                                            예매 취소
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default MyReservationsPage;
