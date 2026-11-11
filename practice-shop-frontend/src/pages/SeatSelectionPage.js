import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import SeatService from '../services/seat.service';
import ShowtimeService from '../services/showtime.service';
import WebSocketService from '../services/websocket.service';
import './SeatSelectionPage.css';

const SeatSelectionPage = () => {
    const { showtimeId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    // 대기열에서 받은 입장 토큰. 예매 API가 Queue-Token 헤더로 요구한다.
    const queueToken = location.state?.queueToken;

    const [showtime, setShowtime] = useState(null);
    const [seats, setSeats] = useState([]);
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [error, setError] = useState(null);

    // Initial Data Fetch
    useEffect(() => {
        ShowtimeService.getShowtime(showtimeId)
            .then(res => {
                setShowtime(res.data);
                // 공연장 좌석 목록이 아니라 이 회차의 좌석 상태를 받아야 이미 예매된 좌석이 처음부터 보인다
                return SeatService.listByShowtime(showtimeId);
            })
            .then(res => {
                setSeats(res.data.map(s => ({ ...s, basePrice: Number(s.price) })));
            })
            .catch(err => {
                console.error("Failed to load seat info:", err);
                setError(err.response?.data?.message || '좌석 정보를 불러오는 데 실패했습니다.');
            });
    }, [showtimeId]);

    // WebSocket Connection
    useEffect(() => {
        if (!showtimeId) return;

        const onConnected = () => {
             // Subscribe to seat updates for this showtime
             WebSocketService.subscribe(`/topic/seat/${showtimeId}`, (message) => {
                 // message is SeatStatusMessage
                 // message.seats is a list of SeatStatusItem
                 const updatedInfos = message.seats;
                 
                 setSeats(prevSeats => {
                     // Create a map for quick lookup
                     const updatesMap = new Map();
                     updatedInfos.forEach(info => updatesMap.set(info.seatId, info.status));

                     return prevSeats.map(seat => {
                         if (updatesMap.has(seat.seatId)) {
                             const newStatus = updatesMap.get(seat.seatId);
                             // If the seat I selected is now reserved by someone else (and it wasn't me), I should deselect it
                             // But here we rely on the user seeing it turn red.
                             return { ...seat, status: newStatus };
                         }
                         return seat;
                     });
                 });
             });
        };

        const onError = (err) => {
            console.error("WebSocket Error:", err);
        };

        WebSocketService.connect(onConnected, onError);

        return () => {
            WebSocketService.disconnect();
        };
    }, [showtimeId]);


    const handleSeatClick = (seat) => {
        if (seat.status !== 'AVAILABLE') return;

        setSelectedSeats(prev => {
            const isSelected = prev.find(s => s.seatId === seat.seatId);
            if (isSelected) {
                return prev.filter(s => s.seatId !== seat.seatId);
            } else {
                return [...prev, { seatId: seat.seatId, section: seat.sectionName, row: seat.rowLabel, number: seat.seatNumber, price: seat.basePrice }];
            }
        });
    };
    
    const totalPrice = useMemo(() => {
        return selectedSeats.reduce((total, seat) => total + seat.price, 0);
    }, [selectedSeats]);

    const seatMap = useMemo(() => {
        const sections = {};
        seats.forEach(seat => {
            const section = seat.sectionName || 'default';
            if (!sections[section]) sections[section] = {};
            const row = seat.rowLabel || 'A';
            if (!sections[section][row]) sections[section][row] = [];
            sections[section][row].push(seat);
        });
         // Sort rows
        for (const section in sections) {
            sections[section] = Object.entries(sections[section])
                .sort(([rowA], [rowB]) => rowA.localeCompare(rowB))
                .reduce((acc, [row, seats]) => {
                    acc[row] = seats.sort((a, b) => a.seatNumber - b.seatNumber);
                    return acc;
                }, {});
        }
        return sections;
    }, [seats]);

    const handleBookingConfirmation = () => {
        if (selectedSeats.length === 0) {
            alert("좌석을 선택해주세요.");
            return;
        }
        // Navigate to checkout page with selected seats info
        navigate('/checkout', { state: { selectedSeats, showtimeId, showtimeInfo: showtime, queueToken } });
    };

    if (error) return <div className="seat-selection-container error-container">{error}</div>;
    if (!showtime) return <div className="seat-selection-container loading-container">좌석 정보를 불러오는 중입니다...</div>;

    return (
        <div className="seat-selection-container">
            <div className="seat-map-wrapper">
                <div className="screen">SCREEN</div>
                <div className="seat-map">
                    {Object.entries(seatMap).map(([sectionName, rows]) => (
                        <div key={sectionName} className="seat-section">
                            <h3 className="section-name">{sectionName !== 'default' && sectionName}</h3>
                            {Object.entries(rows).map(([rowLabel, rowSeats]) => (
                                <div key={rowLabel} className="seat-row">
                                    <div className="row-label">{rowLabel}</div>
                                    {rowSeats.map(seat => {
                                        const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
                                        return (
                                            <div
                                                key={seat.seatId}
                                                className={`seat ${seat.status.toLowerCase()} ${isSelected ? 'selected' : ''}`}
                                                onClick={() => handleSeatClick(seat)}
                                            >
                                                {seat.seatNumber}
                                            </div>
                                        );
                                    })}
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
                 <div className="seat-legend">
                    <div className="legend-item"><div className="seat available"></div><span>선택 가능</span></div>
                    <div className="legend-item"><div className="seat reserved"></div><span>예매 완료</span></div>
                    <div className="legend-item"><div className="seat selected"></div><span>선택됨</span></div>
                </div>
            </div>

            <div className="booking-summary-wrapper">
                <div className="booking-summary">
                    <h2>선택 좌석</h2>
                    <div className="selected-seats-list">
                        {selectedSeats.length > 0 ? (
                            selectedSeats.map(s => (
                                <div key={s.seatId} className="selected-seat-item">
                                    <span>{s.section} {s.row}열 {s.number}번</span>
                                    <span>{s.price.toLocaleString()}원</span>
                                </div>
                            ))
                        ) : (
                            <p>좌석을 선택해주세요.</p>
                        )}
                    </div>
                    <div className="total-price-section">
                        <h3>총 결제 금액</h3>
                        <p className="total-price">{totalPrice.toLocaleString()}원</p>
                    </div>
                    <button onClick={handleBookingConfirmation} className="confirm-booking-btn" disabled={selectedSeats.length === 0}>
                        결제하기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default SeatSelectionPage;
