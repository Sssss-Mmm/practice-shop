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

const ShowtimeAdminPage = () => {
    const [events, setEvents] = useState([]);
    const [venues, setVenues] = useState([]);
    const [showtimes, setShowtimes] = useState([]);
    const [form, setForm] = useState(initialForm);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState(null);

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
            })
            .catch((err) => setError(err.response?.data?.message || err.message || '등록 실패'));
    };

    return (
        <div className="ticketing-admin">
            <h1>회차 관리</h1>
            <div className="ticketing-admin__grid">
                <form className="ticketing-form" onSubmit={handleSubmit}>
                    <h2>회차 등록</h2>
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
                        <option value="SCHEDULED">SCHEDULED</option>
                        <option value="ON_SALE">ON_SALE</option>
                        <option value="SOLD_OUT">SOLD_OUT</option>
                        <option value="COMPLETED">COMPLETED</option>
                        <option value="CANCELED">CANCELED</option>
                    </select>

                    <button type="submit">저장</button>
                    {message && <p className="ticketing-success">{message}</p>}
                    {error && <p className="ticketing-error">{error}</p>}
                </form>

                <div className="ticketing-list">
                    <h2>회차 목록</h2>
                    {loading ? <p>불러오는 중...</p> : (
                        <ul>
                            {showtimes.map((st) => (
                                <li key={st.showtimeId}>
                                    <strong>{st.eventTitle}</strong> @ {st.venueName}<br />
                                    {st.startDateTime?.replace('T', ' ')} ({st.status})
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ShowtimeAdminPage;
