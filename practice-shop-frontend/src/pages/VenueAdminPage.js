import React, { useEffect, useRef, useState } from 'react';
import VenueService from '../services/venue.service';
import './TicketingAdmin.css';

const initialForm = {
    name: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    postalCode: '',
    seatingChartUrl: '',
    description: '',
    contactPhone: '',
    homepage: '',
    capacity: '',
    facilities: {
        parking: false,
        wheelchair: false,
        sound: false,
        lighting: false,
    },
};

const VenueAdminPage = () => {
    const [venues, setVenues] = useState([]);
    const [form, setForm] = useState(initialForm);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');
    const mapContainerRef = useRef(null);
    const mapRef = useRef(null);
    const [mapError, setMapError] = useState(null);
    const [viewMode, setViewMode] = useState('LIST'); // 'LIST' or 'FORM'

    const loadVenues = () => {
        setLoading(true);
        VenueService.listVenues()
            .then((res) => {
                setVenues(res.data || []);
                setError(null);
            })
            .catch((err) => {
                setError(err.response?.data?.message || err.message || '공연장을 불러오지 못했습니다.');
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        loadVenues();
    }, []);

    useEffect(() => {
        const addr = form.addressLine1;
        if (!addr || !mapContainerRef.current) return;

        const loadKakao = () => {
            if (window.kakao && window.kakao.maps) return Promise.resolve(window.kakao);
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

        loadKakao()
            .then((kakao) => {
                const geocoder = new kakao.maps.services.Geocoder();
                geocoder.addressSearch(addr, (result, status) => {
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
    }, [form.addressLine1]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        setMessage('');
        setError(null);
        // 백엔드 스키마에 맞춰 전송 가능한 필드만 보냅니다.
        const payload = {
            name: form.name,
            addressLine1: form.addressLine1,
            addressLine2: form.addressLine2,
            city: form.city,
            state: form.state,
            postalCode: form.postalCode,
            seatingChartUrl: form.seatingChartUrl,
            description: form.description,
        };

        VenueService.createVenue(payload)
            .then((res) => {
                setMessage('공연장을 등록했습니다.');
                setForm(initialForm);
                loadVenues();
                setTimeout(() => {
                    setViewMode('LIST');
                    setMessage('');
                }, 1500);
            })
            .catch((err) => {
                setError(err.response?.data?.message || err.message || '등록에 실패했습니다.');
            });
    };

    if (viewMode === 'FORM') {
        return (
            <div className="ticketing-admin">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h1>공연장 등록</h1>
                    <button className="btn-secondary" onClick={() => setViewMode('LIST')}>목록으로 돌아가기</button>
                </div>

                <form className="ticketing-form" onSubmit={handleSubmit}>
                    <h2>공연장 기본 정보</h2>
                    <label>이름
                        <input name="name" value={form.name} onChange={handleChange} required />
                    </label>
                    <label>연락처
                        <input name="contactPhone" value={form.contactPhone} onChange={handleChange} placeholder="예: 02-123-4567" />
                    </label>
                    <label>홈페이지
                        <input name="homepage" value={form.homepage} onChange={handleChange} placeholder="https://example.com" />
                    </label>
                    <label>총수용인원/규모
                        <input type="number" name="capacity" value={form.capacity} onChange={handleChange} placeholder="예: 1200" />
                    </label>

                    <h2>주소</h2>
                    <label>주소 1
                        <input name="addressLine1" value={form.addressLine1} onChange={handleChange} placeholder="도로명 주소" />
                    </label>
                    <label>주소 2
                        <input name="addressLine2" value={form.addressLine2} onChange={handleChange} placeholder="상세 주소" />
                    </label>
                    <label>도시
                        <input name="city" value={form.city} onChange={handleChange} />
                    </label>
                    <label>주/도
                        <input name="state" value={form.state} onChange={handleChange} />
                    </label>
                    <label>우편번호
                        <input name="postalCode" value={form.postalCode} onChange={handleChange} />
                    </label>

                    <h2>시설 정보</h2>
                    <div className="facility-grid">
                        {Object.entries(form.facilities).map(([key, val]) => (
                            <label key={key} className="facility-check">
                                <input
                                    type="checkbox"
                                    checked={val}
                                    onChange={(e) => setForm({
                                        ...form,
                                        facilities: { ...form.facilities, [key]: e.target.checked },
                                    })}
                                />
                                {key === 'parking' && '주차 가능'}
                                {key === 'wheelchair' && '장애인석/편의시설'}
                                {key === 'sound' && '음향 장비'}
                                {key === 'lighting' && '조명 장비'}
                            </label>
                        ))}
                    </div>

                    <h2>대표 이미지 / 좌석도</h2>
                    <label>대표 이미지 URL
                        <input
                            name="seatingChartUrl"
                            value={form.seatingChartUrl}
                            onChange={handleChange}
                            placeholder="이미지 URL을 입력하면 미리보기 표시"
                        />
                    </label>
                    {form.seatingChartUrl && (
                        <img src={form.seatingChartUrl} alt="대표 이미지" className="venue-thumb" />
                    )}

                    <h2>설명</h2>
                    <textarea name="description" value={form.description} onChange={handleChange} rows="3" placeholder="공연장 소개, 접근성, 주차 안내 등" />

                    <button type="submit">저장</button>
                    {message && <p className="ticketing-success">{message}</p>}
                    {error && <p className="ticketing-error">{error}</p>}
                    <div className="map-preview" ref={mapContainerRef} aria-label="공연장 위치 미리보기" />
                    {mapError && <p className="ticketing-error">{mapError}</p>}
                </form>
            </div>
        );
    }

    return (
        <div className="ticketing-admin">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1>공연장 관리</h1>
                <button className="btn-primary" onClick={() => setViewMode('FORM')}>+ 신규 공연장 등록</button>
            </div>
            
            <div className="ticketing-list">
                <h2>공연장 목록</h2>
                {loading ? (
                    <p>불러오는 중...</p>
                ) : (
                    <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '20px' }}>
                        <thead>
                            <tr style={{ background: '#f5f5f5', borderBottom: '2px solid #333' }}>
                                <th style={{ padding: '12px', textAlign: 'left' }}>이름</th>
                                <th style={{ padding: '12px', textAlign: 'left' }}>주소</th>
                                <th style={{ padding: '12px', textAlign: 'left' }}>도시</th>
                            </tr>
                        </thead>
                        <tbody>
                            {venues.map((v) => (
                                <tr key={v.venueId} style={{ borderBottom: '1px solid #eee' }}>
                                    <td style={{ padding: '12px', fontWeight: 'bold' }}>{v.name}</td>
                                    <td style={{ padding: '12px' }}>{v.addressLine1}</td>
                                    <td style={{ padding: '12px' }}>{v.city}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default VenueAdminPage;
