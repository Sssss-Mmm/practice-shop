import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import EventService from '../services/event.service';
import './EventDetailPage.css';

const EventDetailPage = () => {
    const { eventId } = useParams();
    const navigate = useNavigate();
    const [event, setEvent] = useState(null);
    const [error, setError] = useState(null);
    const [selectedDate, setSelectedDate] = useState(null);
    const [selectedShowtime, setSelectedShowtime] = useState(null);
    const mapContainerRef = useRef(null);
    const mapRef = useRef(null);
    const [mapError, setMapError] = useState(null);

    useEffect(() => {
        EventService.getEvent(eventId).then(
            (response) => {
                const data = response.data;
                setEvent(data);
                if (data.showtimes?.length) {
                    const firstDate = data.showtimes[0].startDateTime?.slice(0, 10);
                    setSelectedDate(firstDate);
                    setSelectedShowtime(data.showtimes[0]);
                }
            },
            (err) => setError(err.response ? err.response.data.message : err.message)
        );
    }, [eventId]);

    const groupedByDate = useMemo(() => {
        if (!event?.showtimes) return {};
        return event.showtimes.reduce((acc, st) => {
            const date = st.startDateTime?.slice(0, 10);
            if (!date) return acc;
            acc[date] = acc[date] ? [...acc[date], st] : [st];
            return acc;
        }, {});
    }, [event]);

    const dates = useMemo(() => Object.keys(groupedByDate).sort(), [groupedByDate]);

    const handleShowtimeSelect = (showtime) => {
        setSelectedShowtime(showtime);
        setSelectedDate(showtime.startDateTime?.slice(0, 10) || null);
    };

    const handleBooking = () => {
        if (selectedShowtime) {
            navigate(`/book/showtime/${selectedShowtime.id}/seats`);
        } else {
            alert('먼저 공연 회차를 선택해주세요.');
        }
    };

    const loadKakao = () => {
        if (window.kakao && window.kakao.maps) {
            return Promise.resolve(window.kakao);
        }
        const appKey = process.env.REACT_APP_KAKAO_MAP_KEY;
        if (!appKey) {
            return Promise.reject(new Error('Kakao 지도 API 키가 없습니다. .env에 REACT_APP_KAKAO_MAP_KEY를 설정하세요.'));
        }
        return new Promise((resolve, reject) => {
            const existing = document.querySelector('script[src*="dapi.kakao.com/v2/maps/sdk.js"]');
            if (existing) {
                existing.onload = () => window.kakao.maps.load(() => resolve(window.kakao));
                return;
            }
            const script = document.createElement('script');
            script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${appKey}&autoload=false&libraries=services`;
            script.async = true;
            script.onload = () => window.kakao.maps.load(() => resolve(window.kakao));
            script.onerror = () => reject(new Error('Kakao 지도 스크립트를 불러오지 못했습니다.'));
            document.head.appendChild(script);
        });
    };

    useEffect(() => {
        if (!event?.venue?.addressLine1 || !mapContainerRef.current) return;
        loadKakao()
            .then((kakao) => {
                const geocoder = new kakao.maps.services.Geocoder();
                geocoder.addressSearch(event.venue.addressLine1, (result, status) => {
                    if (status === kakao.maps.services.Status.OK) {
                        const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                        const map = new kakao.maps.Map(mapContainerRef.current, {
                            center: coords,
                            level: 3,
                        });
                        new kakao.maps.Marker({ map, position: coords });
                        mapRef.current = map;
                        setMapError(null);
                    } else {
                        setMapError('주소로 위치를 찾지 못했습니다.');
                    }
                });
            })
            .catch((err) => setMapError(err.message));
    }, [event]);

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
        <div className="page-container event-detail">
            <div className="event-hero">
                <div className="hero-left">
                    <img src={resolveImageUrl(event.posterImageUrl)} alt={event.title} className="hero-poster" />
                </div>
                <div className="hero-right">
                    <h1 className="hero-title">{event.title}</h1>
                    <div className="hero-meta">
                        <div><strong>장소</strong> {event.venue?.name || '-'}</div>
                        <div><strong>관람시간</strong> {event.runningTime || '정보 없음'}</div>
                        <div><strong>기간</strong> {formatDate(event.salesStartDate, false)} ~ {formatDate(event.salesEndDate, false)}</div>
                        <div><strong>관람등급</strong> {event.ageRestriction || '전체관람가'}</div>
                        <div><strong>주최/주관</strong> {event.organizerName || '-'}</div>
                    </div>
                    <div className="hero-prices">
                        <p>VIP석 <strong>가격 미정</strong></p>
                        <p>R석 <strong>가격 미정</strong></p>
                        <p>S석 <strong>가격 미정</strong></p>
                        <p>A석 <strong>가격 미정</strong></p>
                    </div>
                    <div className="hero-actions">
                        <button className="btn btn-outline-secondary">할인안내 보기</button>
                        <button className="btn btn-danger" onClick={handleBooking} disabled={!selectedShowtime}>예매하기</button>
                    </div>
                </div>
            </div>

            <div className="booking-module">
                <div className="booking-step">
                    <div className="step-title">STEP1 날짜 선택</div>
                    <div className="date-grid">
                        {dates.length === 0 ? <p className="muted">예매 가능한 날짜가 없습니다.</p> : dates.map((d) => (
                            <button
                                key={d}
                                className={`date-chip ${selectedDate === d ? 'active' : ''}`}
                                onClick={() => setSelectedDate(d)}
                            >
                                {formatDate(d, false)}
                            </button>
                        ))}
                    </div>
                </div>
                <div className="booking-step">
                    <div className="step-title">STEP2 회차 선택</div>
                    <div className="showtime-grid">
                        {selectedDate && groupedByDate[selectedDate]?.length ? groupedByDate[selectedDate].map((st) => (
                            <button
                                key={st.id}
                                className={`showtime-card ${selectedShowtime?.id === st.id ? 'active' : ''}`}
                                onClick={() => handleShowtimeSelect(st)}
                            >
                                <div className="showtime-time">{formatDate(st.startDateTime)}</div>
                                <div className="showtime-status">{st.status}</div>
                            </button>
                        )) : <p className="muted">선택한 날짜에 회차가 없습니다.</p>}
                    </div>
                </div>
            </div>

            <div className="booking-actions bottom-actions">
                <button className="btn btn-light">GLOBAL BOOKING</button>
                <button className="btn btn-danger" onClick={handleBooking} disabled={!selectedShowtime}>예매하기</button>
            </div>

            <div className="cast-section">
                <div className="section-header">
                    <h3>출연진</h3>
                </div>
                <div className="cast-row">
                    {Array.from({ length: 8 }).map((_, idx) => (
                        <div className="cast-chip" key={idx}>
                            <div className="cast-avatar">{event.title?.[0] || 'C'}</div>
                            <div className="cast-name">캐스트 {idx + 1}</div>
                            <div className="cast-role">역할 미정</div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="tabs-section">
                <div className="tabs">
                    <button className="tab active">공연정보</button>
                    <button className="tab">캐스팅</button>
                    <button className="tab">기획/제작</button>
                    <button className="tab">관람후기</button>
                    <button className="tab">예매/취소 안내</button>
                </div>
                <div className="tab-panel">
                    <p>{event.description || '공연 상세 정보가 준비 중입니다.'}</p>
                </div>
            </div>

            <div className="venue-map">
                <h3>장소정보</h3>
                <p>{event.venue?.name}</p>
                <p>{event.venue?.addressLine1}</p>
                <div ref={mapContainerRef} className="map-placeholder" aria-label="지도 영역" />
                {mapError && <p className="text-danger small mt-2">{mapError}</p>}
            </div>
        </div>
    );
};

export default EventDetailPage;
