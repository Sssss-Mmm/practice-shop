import React, { useState, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import SeatService from '../services/seat.service';
import ShowtimeService from '../services/showtime.service';
import './SeatSelectionPage.css';

const SeatSelectionPage = () => {
    const { showtimeId } = useParams();
    const navigate = useNavigate();
    
    const [showtime, setShowtime] = useState(null);
    const [seats, setSeats] = useState([]);
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        // Fetch showtime details to get venue info
        ShowtimeService.getShowtime(showtimeId)
            .then(res => {
                setShowtime(res.data);
                // Fetch seats for the venue of this showtime
                // In a real app, you'd fetch seats with their real-time status for the showtime
                return SeatService.listByVenue(res.data.venue.venueId);
            })
            .then(res => {
                 // Simulate some seats being already booked
                const simulatedSeats = res.data.map((seat, index) => ({
                    ...seat,
                    status: index % 7 === 0 || index % 11 === 0 ? 'RESERVED' : 'AVAILABLE'
                }));
                setSeats(simulatedSeats);
            })
            .catch(err => {
                setError(err.response?.data?.message || '좌석 정보를 불러오는 데 실패했습니다.');
            });
    }, [showtimeId]);

    const handleSeatClick = (seat) => {
        if (seat.status !== 'AVAILABLE') return;

        setSelectedSeats(prev => {
            const isSelected = prev.find(s => s.seatId === seat.seatId);
            if (isSelected) {
                return prev.filter(s => s.seatId !== seat.seatId);
            } else {
                // Add seat with its price info
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
        navigate('/checkout', { state: { selectedSeats, showtimeId, showtimeInfo: showtime } });
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
