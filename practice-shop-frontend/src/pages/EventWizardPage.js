import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import VenueService from '../services/venue.service';
import EventService from '../services/event.service';
import ShowtimeService from '../services/showtime.service';
import SeatService from '../services/seat.service';
import { useAuth } from '../context/AuthContext';
import './EventWizardPage.css';

const eventStatuses = [
    { value: 'DRAFT', label: '준비중' },
    { value: 'ON_SALE', label: '판매중' },
    { value: 'COMPLETED', label: '종료' },
    { value: 'CANCELED', label: '취소' },
];

const showtimeStatuses = [
    { value: 'SCHEDULED', label: '예정' },
    { value: 'ON_SALE', label: '판매중' },
    { value: 'SOLD_OUT', label: '매진' },
    { value: 'COMPLETED', label: '종료' },
    { value: 'CANCELED', label: '취소' },
];

const seatStatuses = [
    { value: 'AVAILABLE', label: '선택 가능' },
    { value: 'HOLD', label: '홀드' },
    { value: 'RESERVED', label: '예약됨' },
    { value: 'SOLD', label: '판매됨' },
    { value: 'DISABLED', label: '비활성' },
];

const seatTypes = ['VIP', 'R', 'S', 'A', 'B', 'C', 'ETC'];

const EventWizardPage = () => {
    const navigate = useNavigate();
    const { currentUser } = useAuth();
    const isAdmin = useMemo(() => (currentUser?.roles || []).some((r) => String(r).toUpperCase().includes('ADMIN')), [currentUser]);

    const [step, setStep] = useState(1);
    const [venueId, setVenueId] = useState(null);
    const [eventId, setEventId] = useState(null);

    const [venueForm, setVenueForm] = useState({
        name: '',
        addressLine1: '',
        addressLine2: '',
        city: '',
        state: '',
        postalCode: '',
        seatingChartUrl: '',
        description: '',
    });

    const [eventForm, setEventForm] = useState({
        title: '',
        description: '',
        category: '',
        organizerName: '',
        ageRestriction: '',
        salesStartDate: '',
        salesEndDate: '',
        posterImageUrl: '',
        status: 'ON_SALE',
    });

    const [showtimeForm, setShowtimeForm] = useState({
        startDateTime: '',
        endDateTime: '',
        salesOpenAt: '',
        salesCloseAt: '',
        capacity: '',
        status: 'SCHEDULED',
    });
    const [showtimes, setShowtimes] = useState([]);

    const [seatBatch, setSeatBatch] = useState({
        sectionName: '',
        rowLabel: '',
        seatFrom: 1,
        seatTo: 10,
        seatType: 'VIP',
        basePrice: '',
        status: 'AVAILABLE',
    });
    const [seats, setSeats] = useState([]);

    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const [busy, setBusy] = useState(false);

    if (!isAdmin) {
        return (
            <div className="wizard-container">
                <div className="alert alert-warning">관리자만 접근할 수 있습니다.</div>
            </div>
        );
    }

    const handleVenueSubmit = async (e) => {
        e.preventDefault();
        setBusy(true);
        setMessage('');
        setError('');
        try {
            const res = await VenueService.createVenue(venueForm);
            setVenueId(res.data?.venueId || res.data?.id);
            setStep(2);
            setMessage('공연장을 등록했습니다.');
        } catch (err) {
            setError(err.response?.data?.message || err.message || '공연장 등록에 실패했습니다.');
        } finally {
            setBusy(false);
        }
    };

    const handleEventSubmit = async (e) => {
        e.preventDefault();
        if (!venueId) {
            setError('공연장을 먼저 등록하세요.');
            return;
        }
        setBusy(true);
        setMessage('');
        setError('');
        try {
            const payload = { ...eventForm, venueId };
            const res = await EventService.createEvent(payload);
            setEventId(res.data?.eventId || res.data?.id);
            setStep(3);
            setMessage('공연을 등록했습니다.');
        } catch (err) {
            setError(err.response?.data?.message || err.message || '공연 등록에 실패했습니다.');
        } finally {
            setBusy(false);
        }
    };

    const handleShowtimeAdd = async (e) => {
        e.preventDefault();
        if (!eventId || !venueId) {
            setError('공연과 공연장을 먼저 등록하세요.');
            return;
        }
        setBusy(true);
        setMessage('');
        setError('');
        try {
            const payload = {
                ...showtimeForm,
                eventId,
                venueId,
                capacity: showtimeForm.capacity ? Number(showtimeForm.capacity) : null,
            };
            const res = await ShowtimeService.createShowtime(payload);
            setShowtimes((prev) => [...prev, res.data]);
            setShowtimeForm({
                startDateTime: '',
                endDateTime: '',
                salesOpenAt: '',
                salesCloseAt: '',
                capacity: '',
                status: 'SCHEDULED',
            });
            setMessage('회차를 추가했습니다.');
        } catch (err) {
            setError(err.response?.data?.message || err.message || '회차 추가에 실패했습니다.');
        } finally {
            setBusy(false);
        }
    };

    const handleSeatBatchAdd = async (e) => {
        e.preventDefault();
        if (!venueId) {
            setError('공연장을 먼저 등록하세요.');
            return;
        }
        const { seatFrom, seatTo } = seatBatch;
        const start = Number(seatFrom);
        const end = Number(seatTo);
        if (!start || !end || end < start) {
            setError('좌석 번호 범위를 올바르게 입력해주세요.');
            return;
        }

        setBusy(true);
        setMessage('');
        setError('');
        try {
            const created = [];
            for (let num = start; num <= end; num += 1) {
                const payload = {
                    venueId,
                    sectionName: seatBatch.sectionName || 'MAIN',
                    rowLabel: seatBatch.rowLabel || 'A',
                    seatNumber: num,
                    seatType: seatBatch.seatType,
                    basePrice: seatBatch.basePrice ? Number(seatBatch.basePrice) : null,
                    status: seatBatch.status,
                };
                const res = await SeatService.createSeat(payload);
                created.push(res.data);
            }
            setSeats((prev) => [...prev, ...created]);
            setMessage(`좌석 ${created.length}개를 추가했습니다.`);
        } catch (err) {
            setError(err.response?.data?.message || err.message || '좌석 추가에 실패했습니다.');
        } finally {
            setBusy(false);
        }
    };

    const resetAll = () => {
        setVenueId(null);
        setEventId(null);
        setStep(1);
        setShowtimes([]);
        setSeats([]);
        setVenueForm({
            name: '',
            addressLine1: '',
            addressLine2: '',
            city: '',
            state: '',
            postalCode: '',
            seatingChartUrl: '',
            description: '',
        });
        setEventForm({
            title: '',
            description: '',
            category: '',
            organizerName: '',
            ageRestriction: '',
            salesStartDate: '',
            salesEndDate: '',
            posterImageUrl: '',
            status: 'ON_SALE',
        });
        setShowtimeForm({
            startDateTime: '',
            endDateTime: '',
            salesOpenAt: '',
            salesCloseAt: '',
            capacity: '',
            status: 'SCHEDULED',
        });
        setSeatBatch({
            sectionName: '',
            rowLabel: '',
            seatFrom: 1,
            seatTo: 10,
            seatType: 'VIP',
            basePrice: '',
            status: 'AVAILABLE',
        });
        setMessage('');
        setError('');
    };

    return (
        <div className="wizard-container">
            <div className="wizard-header">
                <h1>공연 올인원 등록</h1>
                <p className="wizard-sub">공연장 → 공연 → 회차 → 좌석을 한 곳에서 등록합니다.</p>
                <div className="wizard-steps">
                    {[1, 2, 3, 4].map((s) => (
                        <div key={s} className={`wizard-step ${step === s ? 'active' : ''} ${step > s ? 'done' : ''}`}>
                            STEP {s}
                        </div>
                    ))}
                </div>
            </div>

            {message && <div className="alert alert-success">{message}</div>}
            {error && <div className="alert alert-danger">{error}</div>}

            {step === 1 && (
                <form className="wizard-card" onSubmit={handleVenueSubmit}>
                    <div className="wizard-card-header">
                        <h2>1. 공연장 등록</h2>
                        {venueId && <span className="badge bg-success">등록완료 ID: {venueId}</span>}
                    </div>
                    <div className="wizard-grid">
                        <label>공연장 이름
                            <input value={venueForm.name} onChange={(e) => setVenueForm({ ...venueForm, name: e.target.value })} required />
                        </label>
                        <label>주소 1
                            <input value={venueForm.addressLine1} onChange={(e) => setVenueForm({ ...venueForm, addressLine1: e.target.value })} />
                        </label>
                        <label>주소 2
                            <input value={venueForm.addressLine2} onChange={(e) => setVenueForm({ ...venueForm, addressLine2: e.target.value })} />
                        </label>
                        <label>도시
                            <input value={venueForm.city} onChange={(e) => setVenueForm({ ...venueForm, city: e.target.value })} />
                        </label>
                        <label>주/도
                            <input value={venueForm.state} onChange={(e) => setVenueForm({ ...venueForm, state: e.target.value })} />
                        </label>
                        <label>우편번호
                            <input value={venueForm.postalCode} onChange={(e) => setVenueForm({ ...venueForm, postalCode: e.target.value })} />
                        </label>
                        <label>좌석도 URL
                            <input value={venueForm.seatingChartUrl} onChange={(e) => setVenueForm({ ...venueForm, seatingChartUrl: e.target.value })} />
                        </label>
                        <label>설명
                            <textarea rows="3" value={venueForm.description} onChange={(e) => setVenueForm({ ...venueForm, description: e.target.value })} />
                        </label>
                    </div>
                    <div className="wizard-actions">
                        <button type="submit" className="btn btn-primary" disabled={busy}>공연장 등록</button>
                        {venueId && <button type="button" className="btn btn-outline-secondary" onClick={() => setStep(2)}>다음 단계</button>}
                    </div>
                </form>
            )}

            {step === 2 && (
                <form className="wizard-card" onSubmit={handleEventSubmit}>
                    <div className="wizard-card-header">
                        <h2>2. 공연 등록</h2>
                        {eventId && <span className="badge bg-success">등록완료 ID: {eventId}</span>}
                    </div>
                    <div className="wizard-grid">
                        <label>제목
                            <input value={eventForm.title} onChange={(e) => setEventForm({ ...eventForm, title: e.target.value })} required />
                        </label>
                        <label>카테고리
                            <input value={eventForm.category} onChange={(e) => setEventForm({ ...eventForm, category: e.target.value })} placeholder="뮤지컬/콘서트/스포츠 등" />
                        </label>
                        <label>주최/주관
                            <input value={eventForm.organizerName} onChange={(e) => setEventForm({ ...eventForm, organizerName: e.target.value })} />
                        </label>
                        <label>관람 등급
                            <input value={eventForm.ageRestriction} onChange={(e) => setEventForm({ ...eventForm, ageRestriction: e.target.value })} />
                        </label>
                        <label>판매 시작일
                            <input type="date" value={eventForm.salesStartDate} onChange={(e) => setEventForm({ ...eventForm, salesStartDate: e.target.value })} />
                        </label>
                        <label>판매 종료일
                            <input type="date" value={eventForm.salesEndDate} onChange={(e) => setEventForm({ ...eventForm, salesEndDate: e.target.value })} />
                        </label>
                        <label>포스터 URL
                            <input value={eventForm.posterImageUrl} onChange={(e) => setEventForm({ ...eventForm, posterImageUrl: e.target.value })} />
                        </label>
                        <label>설명
                            <textarea rows="3" value={eventForm.description} onChange={(e) => setEventForm({ ...eventForm, description: e.target.value })} />
                        </label>
                        <label>상태
                            <select value={eventForm.status} onChange={(e) => setEventForm({ ...eventForm, status: e.target.value })}>
                                {eventStatuses.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
                            </select>
                        </label>
                    </div>
                    <div className="wizard-actions">
                        <button type="button" className="btn btn-outline-secondary" onClick={() => setStep(1)}>이전</button>
                        <button type="submit" className="btn btn-primary" disabled={busy || !venueId}>공연 등록</button>
                        {eventId && <button type="button" className="btn btn-outline-secondary" onClick={() => setStep(3)}>다음 단계</button>}
                    </div>
                </form>
            )}

            {step === 3 && (
                <div className="wizard-card">
                    <div className="wizard-card-header">
                        <h2>3. 회차 등록</h2>
                        <span className="badge bg-secondary">등록된 회차 {showtimes.length}건</span>
                    </div>
                    <form className="wizard-grid" onSubmit={handleShowtimeAdd}>
                        <label>시작 일시
                            <input type="datetime-local" value={showtimeForm.startDateTime} onChange={(e) => setShowtimeForm({ ...showtimeForm, startDateTime: e.target.value })} required />
                        </label>
                        <label>종료 일시
                            <input type="datetime-local" value={showtimeForm.endDateTime} onChange={(e) => setShowtimeForm({ ...showtimeForm, endDateTime: e.target.value })} />
                        </label>
                        <label>판매 시작
                            <input type="datetime-local" value={showtimeForm.salesOpenAt} onChange={(e) => setShowtimeForm({ ...showtimeForm, salesOpenAt: e.target.value })} />
                        </label>
                        <label>판매 종료
                            <input type="datetime-local" value={showtimeForm.salesCloseAt} onChange={(e) => setShowtimeForm({ ...showtimeForm, salesCloseAt: e.target.value })} />
                        </label>
                        <label>수용 인원
                            <input type="number" value={showtimeForm.capacity} onChange={(e) => setShowtimeForm({ ...showtimeForm, capacity: e.target.value })} />
                        </label>
                        <label>상태
                            <select value={showtimeForm.status} onChange={(e) => setShowtimeForm({ ...showtimeForm, status: e.target.value })}>
                                {showtimeStatuses.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
                            </select>
                        </label>
                        <div className="wizard-full-row">
                            <button type="submit" className="btn btn-primary" disabled={busy || !eventId}>회차 추가</button>
                        </div>
                    </form>
                    {showtimes.length > 0 && (
                        <div className="wizard-list">
                            <h4>등록된 회차</h4>
                            <ul>
                                {showtimes.map((st) => (
                                    <li key={st.showtimeId || st.id}>
                                        {st.startDateTime} ({st.status})
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                    <div className="wizard-actions">
                        <button type="button" className="btn btn-outline-secondary" onClick={() => setStep(2)}>이전</button>
                        <button type="button" className="btn btn-outline-secondary" onClick={() => setStep(4)}>다음 단계</button>
                    </div>
                </div>
            )}

            {step === 4 && (
                <div className="wizard-card">
                    <div className="wizard-card-header">
                        <h2>4. 좌석 등록</h2>
                        <span className="badge bg-secondary">등록된 좌석 {seats.length}개</span>
                    </div>
                    <form className="wizard-grid" onSubmit={handleSeatBatchAdd}>
                        <label>구역
                            <input value={seatBatch.sectionName} onChange={(e) => setSeatBatch({ ...seatBatch, sectionName: e.target.value })} placeholder="VIP존 등" required />
                        </label>
                        <label>열
                            <input value={seatBatch.rowLabel} onChange={(e) => setSeatBatch({ ...seatBatch, rowLabel: e.target.value })} placeholder="A" required />
                        </label>
                        <label>시작 번호
                            <input type="number" value={seatBatch.seatFrom} onChange={(e) => setSeatBatch({ ...seatBatch, seatFrom: e.target.value })} required />
                        </label>
                        <label>끝 번호
                            <input type="number" value={seatBatch.seatTo} onChange={(e) => setSeatBatch({ ...seatBatch, seatTo: e.target.value })} required />
                        </label>
                        <label>좌석 유형
                            <select value={seatBatch.seatType} onChange={(e) => setSeatBatch({ ...seatBatch, seatType: e.target.value })}>
                                {seatTypes.map((t) => <option key={t} value={t}>{t}</option>)}
                            </select>
                        </label>
                        <label>기본 가격
                            <input type="number" value={seatBatch.basePrice} onChange={(e) => setSeatBatch({ ...seatBatch, basePrice: e.target.value })} required />
                        </label>
                        <label>상태
                            <select value={seatBatch.status} onChange={(e) => setSeatBatch({ ...seatBatch, status: e.target.value })}>
                                {seatStatuses.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
                            </select>
                        </label>
                        <div className="wizard-full-row">
                            <button type="submit" className="btn btn-primary" disabled={busy || !venueId}>범위 좌석 추가</button>
                        </div>
                    </form>
                    {seats.length > 0 && (
                        <div className="wizard-list">
                            <h4>최근 추가 좌석</h4>
                            <ul>
                                {seats.slice(-10).map((s, idx) => (
                                    <li key={`${s.seatId || s.id || idx}`}>
                                        [{s.sectionName}] {s.rowLabel} {s.seatNumber} ({s.seatType})
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                    <div className="wizard-actions">
                        <button type="button" className="btn btn-outline-secondary" onClick={() => setStep(3)}>이전</button>
                        <button type="button" className="btn btn-success" onClick={() => navigate('/')}>완료 후 홈으로</button>
                        <button type="button" className="btn btn-outline-danger" onClick={resetAll}>새 등록 시작</button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default EventWizardPage;
