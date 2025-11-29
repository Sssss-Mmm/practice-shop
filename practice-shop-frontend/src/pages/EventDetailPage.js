import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import EventService from '../services/event.service';
import './EventDetailPage.css';

const EventDetailPage = () => {
    const { eventId } = useParams();
    const navigate = useNavigate();
    const [event, setEvent] = useState(null);
    const [error, setError] = useState(null);
    const [selectedShowtime, setSelectedShowtime] = useState(null);

    useEffect(() => {
        EventService.getEvent(eventId).then(
            (response) => {
                setEvent(response.data);
                // Automatically select the first showtime if available
                if (response.data.showtimes && response.data.showtimes.length > 0) {
                    setSelectedShowtime(response.data.showtimes[0]);
                }
            },
            (error) => {
                setError(error.response ? error.response.data.message : error.message);
            }
        );
    }, [eventId]);

    const handleShowtimeSelect = (showtime) => {
        setSelectedShowtime(showtime);
    };

    const handleBooking = () => {
        if (selectedShowtime) {
            navigate(`/book/showtime/${selectedShowtime.id}/seats`);
        } else {
            alert('먼저 공연 회차를 선택해주세요.');
        }
    };

    const resolveImageUrl = (relativeUrl) => {
        if (!relativeUrl) return '/placeholder.png';
        if (relativeUrl.startsWith('http')) return relativeUrl;
        const apiBase = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8084';
        return `${apiBase}${encodeURI(relativeUrl)}`;
    };

    const formatDate = (dateString, withTime = true) => {
        if (!dateString) return '';
        const options = withTime
            ? { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'short', hour: '2-digit', minute: '2-digit' }
            : { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'short' };
        return new Date(dateString).toLocaleString('ko-KR', options);
    };

    if (error) {
        return <div className="page-container"><div className="error-box">Error: {error}</div></div>;
    }

    if (!event) {
        return (
            <div className="page-container">
                <div className="loading-box">Loading...</div>
            </div>
        );
    }

    return (
        <div className="page-container">
            <div className="booking-detail-card">
                <div className="booking-detail-header">
                    <div className="booking-image-wrapper">
                        <img
                            src={resolveImageUrl(event.posterImageUrl)}
                            alt={event.title}
                            className="booking-event-image"
                        />
                    </div>
                    <div className="booking-info-wrapper">
                        <h1 className="booking-event-title">{event.title}</h1>
                        <p className="booking-event-meta"><strong>장소:</strong> {event.venue.name}</p>
                        <p className="booking-event-meta"><strong>관람 연령:</strong> {event.ageRestriction}</p>
                        <p className="booking-event-meta"><strong>판매 기간:</strong> {formatDate(event.salesStartDate, false)} ~ {formatDate(event.salesEndDate, false)}</p>
                    </div>
                </div>

                <div className="showtime-selection-section">
                    <h2>회차 선택</h2>
                    <div className="showtime-list">
                        {event.showtimes && event.showtimes.length > 0 ? (
                            event.showtimes.map(st => (
                                <button
                                    key={st.id}
                                    className={`showtime-btn ${selectedShowtime && selectedShowtime.id === st.id ? 'selected' : ''}`}
                                    onClick={() => handleShowtimeSelect(st)}
                                >
                                    {formatDate(st.startDateTime)}
                                </button>
                            ))
                        ) : (
                            <p className="no-showtimes-msg">예매 가능한 회차가 없습니다.</p>
                        )}
                    </div>
                </div>

                <div className="booking-actions">
                     <button
                        className="next-step-btn"
                        onClick={handleBooking}
                        disabled={!selectedShowtime}
                    >
                        좌석 선택하기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EventDetailPage;
