import React, { useEffect, useRef, useState } from 'react';
import VenueService from '../services/venue.service';
import SeatService from '../services/seat.service';
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
    const [seatBatch, setSeatBatch] = useState({
        sectionName: '',
        rowLabel: '',
        seatFrom: 1,
        seatTo: 10,
        seatType: 'VIP',
        basePrice: '',
        status: 'AVAILABLE',
    });
    const [seatPreview, setSeatPreview] = useState([]);
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
                const newVenueId = res.data?.venueId || res.data?.id;
                setMessage('공연장을 등록했습니다.');
                setForm(initialForm);
                // 방금 등록한 공연장으로 좌석 범위 생성
                if (newVenueId && seatPreview.length > 0) {
                    Promise.all(seatPreview.map((s) =>
                        SeatService.createSeat({ ...s, venueId: newVenueId })
                    )).catch(() => {});
                }
                setSeatPreview([]);
                loadVenues();
            })
            .catch((err) => {
                setError(err.response?.data?.message || err.message || '등록에 실패했습니다.');
            });
    };

    const handleSeatPreview = () => {
        const start = Number(seatBatch.seatFrom);
        const end = Number(seatBatch.seatTo);
        if (!form.name) {
            setError('공연장 이름 등 기본 정보를 먼저 입력하세요.');
            return;
        }
        if (!start || !end || end < start) {
            setError('좌석 번호 범위를 올바르게 입력하세요.');
            return;
        }
        const rows = [];
        for (let num = start; num <= end; num += 1) {
            rows.push({
                sectionName: seatBatch.sectionName || 'MAIN',
                rowLabel: seatBatch.rowLabel || 'A',
                seatNumber: num,
                seatType: seatBatch.seatType,
                basePrice: seatBatch.basePrice ? Number(seatBatch.basePrice) : null,
                status: seatBatch.status,
            });
        }
        setSeatPreview(rows);
        setMessage(`좌석 미리보기 ${rows.length}개 생성 (공연장 저장 시 함께 생성)`);
        setError(null);
    };

    return (
        <div className="ticketing-admin">
            <h1>공연장 관리</h1>
            <div className="ticketing-admin__grid">
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

                    <h2>좌석 범위 생성 (간소화)</h2>
                    <div className="seat-batch-grid">
                        <label>구역
                            <input value={seatBatch.sectionName} onChange={(e) => setSeatBatch({ ...seatBatch, sectionName: e.target.value })} placeholder="예: VIP존" required />
                        </label>
                        <label>열
                            <input value={seatBatch.rowLabel} onChange={(e) => setSeatBatch({ ...seatBatch, rowLabel: e.target.value })} placeholder="예: A" required />
                        </label>
                        <label>시작 번호
                            <input type="number" value={seatBatch.seatFrom} onChange={(e) => setSeatBatch({ ...seatBatch, seatFrom: e.target.value })} required />
                        </label>
                        <label>끝 번호
                            <input type="number" value={seatBatch.seatTo} onChange={(e) => setSeatBatch({ ...seatBatch, seatTo: e.target.value })} required />
                        </label>
                        <label>좌석 유형
                            <select value={seatBatch.seatType} onChange={(e) => setSeatBatch({ ...seatBatch, seatType: e.target.value })}>
                                <option value="VIP">VIP</option>
                                <option value="R">R</option>
                                <option value="S">S</option>
                                <option value="A">A</option>
                                <option value="B">B</option>
                                <option value="C">C</option>
                                <option value="ETC">기타</option>
                            </select>
                        </label>
                        <label>기본 가격
                            <input type="number" value={seatBatch.basePrice} onChange={(e) => setSeatBatch({ ...seatBatch, basePrice: e.target.value })} required />
                        </label>
                        <label>상태
                            <select value={seatBatch.status} onChange={(e) => setSeatBatch({ ...seatBatch, status: e.target.value })}>
                                <option value="AVAILABLE">선택 가능</option>
                                <option value="HOLD">홀드</option>
                                <option value="RESERVED">예약됨</option>
                                <option value="SOLD">판매됨</option>
                                <option value="DISABLED">비활성</option>
                            </select>
                        </label>
                    </div>
                    <button type="button" className="btn btn-outline-secondary" onClick={handleSeatPreview}>좌석 범위 미리보기</button>
                    {seatPreview.length > 0 && (
                        <div className="wizard-list">
                            <h4>좌석 미리보기 ({seatPreview.length}개) - 공연장 저장 시 생성</h4>
                            <ul>
                                {seatPreview.slice(0, 12).map((s, idx) => (
                                    <li key={`${s.sectionName}-${s.rowLabel}-${s.seatNumber}-${idx}`}>
                                        [{s.sectionName}] {s.rowLabel}{s.seatNumber} ({s.seatType}) - {s.basePrice ? `${s.basePrice.toLocaleString()}원` : '가격없음'}
                                    </li>
                                ))}
                                {seatPreview.length > 12 && <li>...외 {seatPreview.length - 12}개</li>}
                            </ul>
                        </div>
                    )}
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
