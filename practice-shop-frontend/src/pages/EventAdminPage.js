import React, { useEffect, useState } from 'react';
import VenueService from '../services/venue.service';
import EventService from '../services/event.service';
import './TicketingAdmin.css';

const initialForm = {
    title: '',
    description: '',
    category: '',
    organizerName: '',
    ageRestriction: '',
    salesStartDate: '',
    salesEndDate: '',
    posterImageUrl: '',
    venueId: '',
    status: 'DRAFT',
    runningTime: '', // [NEW] Running Time
    casting: '',     // [NEW] Casting (Text or JSON)
};

const eventStatuses = [
    { value: 'DRAFT', label: '준비중' },
    { value: 'ON_SALE', label: '판매중' },
    { value: 'COMPLETED', label: '종료' },
    { value: 'CANCELED', label: '취소' },
];

const EventAdminPage = () => {
    const [venues, setVenues] = useState([]);
    const [events, setEvents] = useState([]);
    const [form, setForm] = useState(initialForm);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState(null);
    const [viewMode, setViewMode] = useState('LIST'); // 'LIST' or 'FORM'

    const loadVenues = () => {
        VenueService.listVenues().then((res) => setVenues(res.data || []));
    };

    const loadEvents = () => {
        setLoading(true);
        EventService.listEvents()
            .then((res) => setEvents(res.data || []))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        loadVenues();
        loadEvents();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        setMessage('');
        setError(null);
        const payload = {
            ...form,
            venueId: form.venueId ? Number(form.venueId) : null,
            salesStartDate: form.salesStartDate || null,
            salesEndDate: form.salesEndDate || null,
        };
        EventService.createEvent(payload)
            .then(() => {
                setMessage('공연을 등록했습니다.');
                setForm(initialForm);
                loadEvents();
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
                    <h1>공연 등록</h1>
                    <button className="btn-secondary" onClick={() => setViewMode('LIST')}>목록으로 돌아가기</button>
                </div>
                
                <form className="ticketing-form" onSubmit={handleSubmit}>
                    <h2>기본 정보</h2>
                    <label>제목</label>
                    <input name="title" value={form.title} onChange={handleChange} required />

                    <label>설명</label>
                    <textarea name="description" value={form.description} onChange={handleChange} />

                    <label>카테고리</label>
                    <input name="category" value={form.category} onChange={handleChange} />

                    <label>주최/주관</label>
                    <input name="organizerName" value={form.organizerName} onChange={handleChange} />

                    <label>관람 등급</label>
                    <input name="ageRestriction" value={form.ageRestriction} onChange={handleChange} />

                    <label>판매 시작일</label>
                    <input type="date" name="salesStartDate" value={form.salesStartDate} onChange={handleChange} />

                    <label>판매 종료일</label>
                    <input type="date" name="salesEndDate" value={form.salesEndDate} onChange={handleChange} />

                    <label>관람 시간 (예: 120분)</label>
                    <input name="runningTime" value={form.runningTime} onChange={handleChange} placeholder="ex) 120분" />

                    <label>출연진 (예: 홍길동, 김철수)</label>
                    <input name="casting" value={form.casting} onChange={handleChange} placeholder="쉼표로 구분하여 입력" />

                    <label>포스터 URL</label>
                    <input name="posterImageUrl" value={form.posterImageUrl} onChange={handleChange} />

                    <label>공연장</label>
                    <select name="venueId" value={form.venueId} onChange={handleChange} required>
                        <option value="">선택</option>
                        {venues.map((v) => (
                            <option key={v.venueId} value={v.venueId}>{v.name}</option>
                        ))}
                    </select>

                    <label>상태</label>
                    <select name="status" value={form.status} onChange={handleChange}>
                        {eventStatuses.map((s) => (
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
                <h1>공연 관리</h1>
                <button className="btn-primary" onClick={() => setViewMode('FORM')}>+ 신규 공연 등록</button>
            </div>
            
            <div className="ticketing-list">
                {loading ? <p>불러오는 중...</p> : (
                    <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '20px' }}>
                        <thead>
                            <tr style={{ background: '#f5f5f5', borderBottom: '2px solid #333' }}>
                                <th style={{ padding: '12px', textAlign: 'left' }}>상태</th>
                                <th style={{ padding: '12px', textAlign: 'left' }}>제목</th>
                                <th style={{ padding: '12px', textAlign: 'left' }}>공연장</th>
                                <th style={{ padding: '12px', textAlign: 'left' }}>카테고리</th>
                            </tr>
                        </thead>
                        <tbody>
                            {events.map((ev) => (
                                <tr key={ev.eventId} style={{ borderBottom: '1px solid #eee' }}>
                                    <td style={{ padding: '12px' }}>
                                        <span className={`status-badge ${ev.status}`}>{ev.status}</span>
                                    </td>
                                    <td style={{ padding: '12px', fontWeight: 'bold' }}>{ev.title}</td>
                                    <td style={{ padding: '12px' }}>{ev.venueName}</td>
                                    <td style={{ padding: '12px' }}>{ev.category}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default EventAdminPage;
