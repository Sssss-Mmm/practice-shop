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
    
    // UI States
    const [activeTab, setActiveTab] = useState('INFO'); // INFO, CAST, REVIEW, VENUE
    const mapContainerRef = useRef(null);
    const mapRef = useRef(null);
    const [mapError, setMapError] = useState(null);

    // Helper to normalize date from array [y,m,d,h,min] to ISO string or keep string
    const normalizeDate = (d) => {
        if (!d) return null;
        if (Array.isArray(d)) {
            const [y, m, day, h, min, s] = d;
            const pad = (n) => String(n).padStart(2, '0');
            return `${y}-${pad(m)}-${pad(day)}T${pad(h||0)}:${pad(min||0)}:${pad(s||0)}`;
        }
        return d;
    };

    useEffect(() => {
        EventService.getEvent(eventId).then(
            (response) => {
                const data = response.data;
                // Normalize showtimes immediately
                if (data.showtimes) {
                    data.showtimes = data.showtimes.map(st => ({
                        ...st,
                        startDateTime: normalizeDate(st.startDateTime),
                        endDateTime: normalizeDate(st.endDateTime)
                    }));
                }
                setEvent(data);
                if (data.showtimes?.length) {
                    const firstDate = data.showtimes[0].startDateTime?.slice(0, 10);
                    setSelectedDate(firstDate);
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
            navigate(`/book/event/${eventId}/showtime/${selectedShowtime.id}/queue`);
        } else {
            alert('먼저 공연 회차를 선택해주세요.');
        }
    };

    const loadKakao = () => {
        if (window.kakao && window.kakao.maps) return Promise.resolve(window.kakao);
        const appKey = process.env.REACT_APP_KAKAO_MAP_KEY;
        if (!appKey) return Promise.reject(new Error('Kakao 지도 API 키가 없습니다.'));
        
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
            script.onerror = () => reject(new Error('Kakao 지도 스크립트 로드 실패'));
            document.head.appendChild(script);
        });
    };

    useEffect(() => {
        if (activeTab === 'VENUE' && event?.venue?.addressLine1 && mapContainerRef.current) {
            loadKakao().then((kakao) => {
                const geocoder = new kakao.maps.services.Geocoder();
                geocoder.addressSearch(event.venue.addressLine1, (result, status) => {
                    if (status === kakao.maps.services.Status.OK) {
                        const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                        const map = new kakao.maps.Map(mapContainerRef.current, { center: coords, level: 3 });
                        new kakao.maps.Marker({ map, position: coords });
                        mapRef.current = map;
                        setMapError(null);
                    } else {
                        setMapError('위치를 찾을 수 없습니다.');
                    }
                });
            }).catch(e => setMapError(e.message));
        }
    }, [event, activeTab]);

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

    if (error) return <div className="page-container"><div className="error-box">Error: {error}</div></div>;
    if (!event) return <div className="page-container"><div className="loading-box">Loading...</div></div>;

    return (
        <div className="page-container event-detail">
            {/* 1. Upper Header: Poster and Essential Info */}
            <div className="event-product-header">
                <div className="product-poster-wrapper">
                    <img src={resolveImageUrl(event.posterImageUrl)} alt={event.title} className="product-poster" />
                </div>
                <div className="product-info-wrapper">
                    <div className="product-title-area">
                        <span className="badge-status">{event.status || '판매중'}</span>
                        <h1 className="product-title">{event.title}</h1>
                        <div className="product-subtitle">{event.description?.slice(0, 50)}...</div>
                    </div>
                    <table className="info-table">
                        <tbody>
                            <tr><th>장소</th><td>{event.venue?.name || '-'}</td></tr>
                            <tr><th>기간</th><td>{formatDate(event.salesStartDate, false)} ~ {formatDate(event.salesEndDate, false)}</td></tr>
                            <tr><th>관람시간</th><td>{event.runningTime || '정보 없음'}</td></tr>
                            <tr><th>관람등급</th><td>{event.ageRestriction || '전체관람가'}</td></tr>
                            <tr className="info-price-row"><th>가격</th><td>
                                <div>VIP석 <strong>150,000</strong>원</div>
                                <div>R석 <strong>120,000</strong>원</div>
                            </td></tr>
                        </tbody>
                    </table>
                </div>
            </div>

            {/* 2. Body: Tabs + Sidebar */}
            <div className="event-content-body">
                {/* 2-1 Left Column */}
                <div className="main-content-column">
                    <div className="sticky-tabs">
                        <button className={`tab-btn ${activeTab === 'INFO' ? 'active' : ''}`} onClick={() => setActiveTab('INFO')}>상세정보</button>
                        <button className={`tab-btn ${activeTab === 'CAST' ? 'active' : ''}`} onClick={() => setActiveTab('CAST')}>캐스팅</button>
                        <button className={`tab-btn ${activeTab === 'VENUE' ? 'active' : ''}`} onClick={() => setActiveTab('VENUE')}>장소/교통</button>
                        <button className={`tab-btn ${activeTab === 'QNA' ? 'active' : ''}`} onClick={() => setActiveTab('QNA')}>관람후기/Q&A</button>
                    </div>

                    <div className="tab-pane">
                        {activeTab === 'INFO' && (
                            <div className="detail-info-pane">
                                <h3>공연 소개</h3>
                                <p style={{whiteSpace: 'pre-wrap'}}>{event.description || '상세 정보가 없습니다.'}</p>
                                <img src="/placeholder_detail.jpg" alt="상세이미지 예시" style={{width: '100%', marginTop: '20px', borderRadius: '8px', opacity: 0.5}} />
                            </div>
                        )}
                        {activeTab === 'CAST' && (
                            <div className="cast-grid">
                                {Array.from({length: 6}).map((_, i) => (
                                    <div className="cast-item" key={i}>
                                        <div className="cast-img">{String.fromCharCode(65+i)}</div>
                                        <div className="cast-name">배우 {i+1}</div>
                                        <div className="cast-role">주연</div>
                                    </div>
                                ))}
                            </div>
                        )}
                        {activeTab === 'VENUE' && (
                            <div className="venue-pane">
                                <h3>{event.venue?.name}</h3>
                                <p>{event.venue?.addressLine1} {event.venue?.addressLine2}</p>
                                <div ref={mapContainerRef} className="map-placeholder" />
                                {mapError && <p className="text-danger small">{mapError}</p>}
                            </div>
                        )}
                        {activeTab === 'QNA' && (
                            <div className="qna-pane">
                                <p className="muted">등록된 관람후기가 없습니다.</p>
                            </div>
                        )}
                    </div>
                </div>

                {/* 2-2 Right Sidebar (Booking Widget) */}
                <aside className="side-booking-column">
                    <div className="booking-widget">
                        <div className="widget-title">날짜/회차 선택</div>
                        
                        <div className="widget-section">
                            <h4>날짜</h4>
                            <div className="widget-date-list">
                                {dates.map(d => (
                                    <button 
                                        key={d} 
                                        className={`date-btn ${selectedDate === d ? 'active' : ''}`}
                                        onClick={() => setSelectedDate(d)}
                                    >
                                        {d.slice(5)}
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div className="widget-section">
                            <h4>회차</h4>
                            <div className="widget-time-list">
                                {selectedDate && groupedByDate[selectedDate]?.map(st => (
                                    <button
                                        key={st.id}
                                        className={`time-btn ${selectedShowtime?.id === st.id ? 'active' : ''}`}
                                        onClick={() => handleShowtimeSelect(st)}
                                    >
                                        {st.startDateTime.slice(11, 16)}
                                    </button>
                                ))}
                                {(!selectedDate || !groupedByDate[selectedDate]?.length) && <span className="muted small">날짜를 먼저 선택하세요.</span>}
                            </div>
                        </div>

                        <div className="widget-summary" style={{marginTop: '20px', borderTop: '1px solid #eee', paddingTop: '15px'}}>
                            {selectedShowtime ? (
                                <>
                                    <div style={{fontWeight: '700'}}>{selectedDate} {selectedShowtime.startDateTime.slice(11, 16)}</div>
                                    <div style={{fontSize: '14px', color: '#666'}}>잔여석: 여유</div>
                                </>
                            ) : (
                                <div style={{color: '#999'}}>일정을 선택해주세요.</div>
                            )}
                        </div>

                        <button className="booking-btn" disabled={!selectedShowtime} onClick={handleBooking}>
                            예매하기
                        </button>
                    </div>
                </aside>
            </div>
        </div>
    );
};

export default EventDetailPage;
