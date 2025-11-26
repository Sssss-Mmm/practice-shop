import React, { useEffect, useState } from 'react';
import VenueService from '../services/venue.service';
import SeatService from '../services/seat.service';
import './TicketingAdmin.css';

const initialForm = {
    venueId: '',
    sectionName: '',
    rowLabel: '',
    seatNumber: '',
    seatType: '',
    basePrice: '',
    status: 'AVAILABLE',
};

const SeatAdminPage = () => {
    const [venues, setVenues] = useState([]);
    const [seats, setSeats] = useState([]);
    const [form, setForm] = useState(initialForm);
    const [message, setMessage] = useState('');
    const [error, setError] = useState(null);

    useEffect(() => {
        VenueService.listVenues().then((res) => setVenues(res.data || []));
    }, []);

    const loadSeats = (venueId) => {
        if (!venueId) {
            setSeats([]);
            return;
        }
        SeatService.listByVenue(venueId).then((res) => setSeats(res.data || []));
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
        if (name === 'venueId') {
            loadSeats(value);
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        setMessage('');
        setError(null);
        const payload = {
            ...form,
            venueId: form.venueId ? Number(form.venueId) : null,
            basePrice: form.basePrice ? Number(form.basePrice) : null,
        };
        SeatService.createSeat(payload)
            .then(() => {
                setMessage('좌석을 등록했습니다.');
                setForm((prev) => ({ ...prev, seatNumber: '', basePrice: '' }));
                loadSeats(payload.venueId);
            })
            .catch((err) => setError(err.response?.data?.message || err.message || '등록 실패'));
    };

    return (
        <div className="ticketing-admin">
            <h1>좌석 관리</h1>
            <div className="ticketing-admin__grid">
                <form className="ticketing-form" onSubmit={handleSubmit}>
                    <h2>좌석 등록</h2>
                    <label>공연장</label>
                    <select name="venueId" value={form.venueId} onChange={handleChange} required>
                        <option value="">선택</option>
                        {venues.map((v) => (
                            <option key={v.venueId} value={v.venueId}>{v.name}</option>
                        ))}
                    </select>

                    <label>구역</label>
                    <input name="sectionName" value={form.sectionName} onChange={handleChange} />

                    <label>열</label>
                    <input name="rowLabel" value={form.rowLabel} onChange={handleChange} />

                    <label>좌석 번호</label>
                    <input name="seatNumber" value={form.seatNumber} onChange={handleChange} required />

                    <label>좌석 유형</label>
                    <input name="seatType" value={form.seatType} onChange={handleChange} />

                    <label>기본 가격</label>
                    <input type="number" name="basePrice" value={form.basePrice} onChange={handleChange} required />

                    <label>상태</label>
                    <select name="status" value={form.status} onChange={handleChange}>
                        <option value="AVAILABLE">AVAILABLE</option>
                        <option value="HOLD">HOLD</option>
                        <option value="RESERVED">RESERVED</option>
                        <option value="SOLD">SOLD</option>
                        <option value="DISABLED">DISABLED</option>
                    </select>

                    <button type="submit">저장</button>
                    {message && <p className="ticketing-success">{message}</p>}
                    {error && <p className="ticketing-error">{error}</p>}
                </form>

                <div className="ticketing-list">
                    <h2>좌석 목록</h2>
                    <ul>
                        {seats.map((s) => (
                            <li key={s.seatId}>
                                [{s.sectionName}] {s.rowLabel} {s.seatNumber} - {s.status}
                            </li>
                        ))}
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default SeatAdminPage;
