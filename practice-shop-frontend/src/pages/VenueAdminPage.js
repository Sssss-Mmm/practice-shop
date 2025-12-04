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
        VenueService.createVenue(form)
            .then(() => {
                setMessage('공연장을 등록했습니다.');
                setForm(initialForm);
                loadVenues();
            })
            .catch((err) => {
                setError(err.response?.data?.message || err.message || '등록에 실패했습니다.');
            });
    };

    return (
        <div className="ticketing-admin">
            <h1>공연장 관리</h1>
            <div className="ticketing-admin__grid">
                <form className="ticketing-form" onSubmit={handleSubmit}>
                    <h2>공연장 등록</h2>
                    <label>이름</label>
                    <input name="name" value={form.name} onChange={handleChange} required />

                    <label>주소 1</label>
                    <input name="addressLine1" value={form.addressLine1} onChange={handleChange} />

                    <label>주소 2</label>
                    <input name="addressLine2" value={form.addressLine2} onChange={handleChange} />

                    <label>도시</label>
                    <input name="city" value={form.city} onChange={handleChange} />

                    <label>주/도</label>
                    <input name="state" value={form.state} onChange={handleChange} />

                    <label>우편번호</label>
                    <input name="postalCode" value={form.postalCode} onChange={handleChange} />

                    <label>좌석도 URL</label>
                    <input name="seatingChartUrl" value={form.seatingChartUrl} onChange={handleChange} />

                    <label>설명</label>
                    <textarea name="description" value={form.description} onChange={handleChange} />

                    <button type="submit">저장</button>
                    {message && <p className="ticketing-success">{message}</p>}
                    {error && <p className="ticketing-error">{error}</p>}
                    <div className="map-preview" ref={mapContainerRef} aria-label="공연장 위치 미리보기" />
                    {mapError && <p className="ticketing-error">{mapError}</p>}
                </form>

                <div className="ticketing-list">
                    <h2>공연장 목록</h2>
                    {loading ? (
                        <p>불러오는 중...</p>
                    ) : (
                        <ul>
                            {venues.map((v) => (
                                <li key={v.venueId}>
                                    <strong>{v.name}</strong>
                                    <div>{v.addressLine1}</div>
                                    <div>{v.city} {v.state} {v.postalCode}</div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );
};

export default VenueAdminPage;
