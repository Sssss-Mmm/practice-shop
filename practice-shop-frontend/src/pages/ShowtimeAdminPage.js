import React, { useEffect, useState } from 'react';
import EventService from '../services/event.service';
import VenueService from '../services/venue.service';
import ShowtimeService from '../services/showtime.service';
import './TicketingAdmin.css';

const initialForm = {
    eventId: '',
    venueId: '',
    startDateTime: '',
    endDateTime: '',
    salesOpenAt: '',
    salesCloseAt: '',
    capacity: '',
    status: 'SCHEDULED',
};

const showtimeStatuses = [
    { value: 'SCHEDULED', label: '예정' },
    { value: 'ON_SALE', label: '판매중' },
    { value: 'SOLD_OUT', label: '매진' },
    { value: 'COMPLETED', label: '종료' },
    { value: 'CANCELED', label: '취소' },
];

const ShowtimeAdminPage = () => {
    const [events, setEvents] = useState([]);
    const [venues, setVenues] = useState([]);
    const [showtimes, setShowtimes] = useState([]);
    const [form, setForm] = useState(initialForm);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState(null);
    const [viewMode, setViewMode] = useState('LIST'); // 'LIST' or 'FORM'

    useEffect(() => {
        EventService.listEvents().then((res) => setEvents(res.data || []));
        VenueService.listVenues().then((res) => setVenues(res.data || []));
    }, []);

    const loadShowtimes = (eventId) => {
        if (!eventId) {
            setShowtimes([]);
            return;
        }
        setLoading(true);
        ShowtimeService.listByEvent(eventId)
            .then((res) => setShowtimes(res.data || []))
            .finally(() => setLoading(false));
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
        if (name === 'eventId') {
            loadShowtimes(value);
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        setMessage('');
        setError(null);
        const payload = {
            ...form,
            eventId: form.eventId ? Number(form.eventId) : null,
            venueId: form.venueId ? Number(form.venueId) : null,
            capacity: form.capacity ? Number(form.capacity) : null,
        };
        ShowtimeService.createShowtime(payload)
            .then(() => {
                setMessage('회차를 등록했습니다.');
                setForm(initialForm);
                loadShowtimes(payload.eventId);
                setTimeout(() => {
                    setViewMode('LIST');
                    setMessage('');
                }, 1500);
            })
            .catch((err) => setError(err.response?.data?.message || err.message || '등록 실패'));
    };

    if (viewMode === 'FORM') {
        return (
            <div className="ticketing-admin">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h1>회차 등록</h1>
                    <button className="btn-secondary" onClick={() => setViewMode('LIST')}>목록으로 돌아가기</button>
                </div>

                <form className="ticketing-form" onSubmit={handleSubmit}>
                    <h2>기본 정보</h2>
                    <label>공연</label>
                    <select name="eventId" value={form.eventId} onChange={handleChange} required>
                        <option value="">선택</option>
                        {events.map((ev) => (
                            <option key={ev.eventId} value={ev.eventId}>{ev.title}</option>
                        ))}
                    </select>

                    <label>공연장</label>
                    <select name="venueId" value={form.venueId} onChange={handleChange} required>
                        <option value="">선택</option>
                        {venues.map((v) => (
                            <option key={v.venueId} value={v.venueId}>{v.name}</option>
                        ))}
                    </select>

                    <label>시작 일시</label>
                    <input type="datetime-local" name="startDateTime" value={form.startDateTime} onChange={handleChange} required />

                    <label>종료 일시</label>
                    <input type="datetime-local" name="endDateTime" value={form.endDateTime} onChange={handleChange} />

                    <label>판매 시작</label>
                    <input type="datetime-local" name="salesOpenAt" value={form.salesOpenAt} onChange={handleChange} />

                    <label>판매 종료</label>
                    <input type="datetime-local" name="salesCloseAt" value={form.salesCloseAt} onChange={handleChange} />

                    <label>수용 인원</label>
                    <input type="number" name="capacity" value={form.capacity} onChange={handleChange} />

                    <label>상태</label>
                    <select name="status" value={form.status} onChange={handleChange}>
                        {showtimeStatuses.map((s) => (
                            <option key={s.value} value={s.value}>{s.label}</option>
                        ))}
                    </select>

                    <button type="submit">저장</button>
                    {message && <p className="ticketing-success">{message}</p>}
                    {error && <p className="ticketing-error">{error}</p>}
                </form>
            </div>
        );
    }

    return (
        <div className="ticketing-admin">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1>회차 관리</h1>
                <button className="btn-primary" onClick={() => setViewMode('FORM')}>+ 신규 회차 등록</button>
            </div>
            
            <div className="ticketing-list">
                <div style={{ marginBottom: '20px', padding: '15px', background: '#f5f5f5', borderRadius: '4px' }}>
                    <label style={{ marginRight: '10px', fontWeight: 'bold' }}>공연 선택하여 조회:</label>
                    <select onChange={(e) => loadShowtimes(e.target.value)} style={{ padding: '5px' }}>
                        <option value="">-- 공연 선택 --</option>
                        {events.map((ev) => (
                            <option key={ev.eventId} value={ev.eventId}>{ev.title}</option>
                        ))}
                    </select>
                </div>

                <h2>회차 목록</h2>
                {loading ? <p>불러오는 중...</p> : (
                    <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '20px' }}>
                        <thead>
                            <tr style={{ background: '#f5f5f5', borderBottom: '2px solid #333' }}>
                                <th style={{ padding: '12px', textAlign: 'left' }}>상태</th>
                                <th style={{ padding: '12px', textAlign: 'left' }}>공연명</th>
                                <th style={{ padding: '12px', textAlign: 'left' }}>공연장</th>
                                <th style={{ padding: '12px', textAlign: 'left' }}>일시</th>
                            </tr>
                        </thead>
                        <tbody>
                            {showtimes.length === 0 && <tr><td colSpan="4" style={{ padding: '20px', textAlign: 'center', color: '#888' }}>조회할 공연을 선택해주세요.</td></tr>}
                            {showtimes.map((st) => (
                                <tr key={st.showtimeId} style={{ borderBottom: '1px solid #eee' }}>
                                    <td style={{ padding: '12px' }}>
                                        <span className={`status-badge ${st.status}`}>{st.status}</span>
                                    </td>
                                    <td style={{ padding: '12px', fontWeight: 'bold' }}>{st.eventTitle}</td>
                                    <td style={{ padding: '12px' }}>{st.venueName}</td>
                                    <td style={{ padding: '12px' }}>{st.startDateTime?.replace('T', ' ')}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default ShowtimeAdminPage;
