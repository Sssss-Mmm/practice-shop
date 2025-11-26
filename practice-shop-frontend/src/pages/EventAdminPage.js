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
};

const EventAdminPage = () => {
    const [venues, setVenues] = useState([]);
    const [events, setEvents] = useState([]);
    const [form, setForm] = useState(initialForm);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState('');

    const loadVenues = () => {
        VenueService.listVenues().then((res) => setVenues(res.data || []));
    };

    const loadEvents = () => {
        setLoading(true);
        EventService.listEvents().then((res) => setEvents(res.data || [])).finally(() => setLoading(false));
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
            })
            .catch((err) => setError(err.response?.data?.message || err.message || '등록 실패'));
    };

    return (
        <div className="ticketing-admin">
            <h1>공연 관리</h1>
            <div className="ticketing-admin__grid">
                <form className="ticketing-form" onSubmit={handleSubmit}>
                    <h2>공연 등록</h2>
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
                        <option value="DRAFT">DRAFT</option>
                        <option value="ON_SALE">ON_SALE</option>
                        <option value="COMPLETED">COMPLETED</option>
                        <option value="CANCELED">CANCELED</option>
                    </select>

                    <button type="submit">저장</button>
                    {message && <p className="ticketing-success">{message}</p>}
                    {error && <p className="ticketing-error">{error}</p>}
                </form>

                <div className="ticketing-list">
                    <h2>공연 목록</h2>
                    {loading ? <p>불러오는 중...</p> : (
                        <ul>
                            {events.map((ev) => (
                                <li key={ev.eventId}>
                                    <strong>{ev.title}</strong> / {ev.status}
                                    <div>{ev.venueName}</div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );
};

export default EventAdminPage;
