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

const seatStatuses = [
    { value: 'AVAILABLE', label: '선택 가능' },
    { value: 'HOLD', label: '홀드' },
    { value: 'RESERVED', label: '예약됨' },
    { value: 'SOLD', label: '판매됨' },
    { value: 'DISABLED', label: '비활성' },
];

const SeatAdminPage = () => {
    const [venues, setVenues] = useState([]);
    const [seats, setSeats] = useState([]);
    const [form, setForm] = useState(initialForm);
    const [batchForm, setBatchForm] = useState({
        startNum: '1',
        endNum: '10',
    });
    const [isBatch, setIsBatch] = useState(false);
    const [viewMode, setViewMode] = useState('LIST'); // 'LIST' or 'FORM'
    const [message, setMessage] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        VenueService.listVenues().then((res) => setVenues(res.data || []));
    }, []);

    const loadSeats = (venueId) => {
        if (!venueId) {
            setSeats([]);
            return;
        }
        setLoading(true);
        SeatService.listByVenue(venueId)
            .then((res) => setSeats(res.data || []))
            .finally(() => setLoading(false));
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
        if (name === 'venueId') {
            loadSeats(value);
        }
    };

    const handleBatchChange = (e) => {
        const { name, value } = e.target;
        setBatchForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setError(null);

        if (!form.venueId) {
            setError('공연장을 선택해주세요.');
            return;
        }

        try {
            if (isBatch) {
                const start = parseInt(batchForm.startNum, 10);
                const end = parseInt(batchForm.endNum, 10);
                
                if (isNaN(start) || isNaN(end) || start > end) {
                    setError('유효하지 않은 범위입니다.');
                    return;
                }

                setMessage(`좌석 ${start}번 ~ ${end}번 생성 중...`);
                
                // 순차적 처리 또는 병렬 처리
                const promises = [];
                for (let i = start; i <= end; i++) {
                    const payload = {
                        ...form,
                        seatNumber: String(i),
                        venueId: Number(form.venueId),
                        basePrice: Number(form.basePrice),
                    };
                    promises.push(SeatService.createSeat(payload));
                }
                
                await Promise.all(promises);
                setMessage(`${end - start + 1}개의 좌석을 일괄 생성했습니다.`);
                
            } else {
                // 단건 등록
                const payload = {
                    ...form,
                    venueId: Number(form.venueId),
                    basePrice: Number(form.basePrice),
                };
                await SeatService.createSeat(payload);
                setMessage('좌석을 등록했습니다.');
                setForm((prev) => ({ ...prev, seatNumber: '' })); // 번호만 초기화
            }

            // 공통 후처리
            loadSeats(form.venueId);
            if (isBatch) {
                setTimeout(() => {
                    setViewMode('LIST');
                    setMessage('');
                }, 2000);
            }

        } catch (err) {
            console.error(err);
            setError('일부 또는 전체 좌석 등록 실패: ' + (err.response?.data?.message || err.message));
        }
    };

    if (viewMode === 'FORM') {
        return (
            <div className="ticketing-admin">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h1>좌석 등록</h1>
                    <button className="btn-secondary" onClick={() => setViewMode('LIST')}>목록으로 돌아가기</button>
                </div>
                
                <form className="ticketing-form" onSubmit={handleSubmit}>
                    <h2>기본 정보</h2>
                    <label>공연장</label>
                    <select name="venueId" value={form.venueId} onChange={handleChange} required>
                        <option value="">선택</option>
                        {venues.map((v) => (
                            <option key={v.venueId} value={v.venueId}>{v.name}</option>
                        ))}
                    </select>

                    <label>구역 (예: 1루 내야석)</label>
                    <input name="sectionName" value={form.sectionName} onChange={handleChange} placeholder="섹션 이름" required />

                    <label>열 (예: A열)</label>
                    <input name="rowLabel" value={form.rowLabel} onChange={handleChange} placeholder="열 이름" required />

                    <label>기본 가격</label>
                    <input type="number" name="basePrice" value={form.basePrice} onChange={handleChange} required />
                    
                    <label>좌석 유형</label>
                    <input name="seatType" value={form.seatType} onChange={handleChange} placeholder="예: STANDARD, VIP" />

                    <label>상태</label>
                    <select name="status" value={form.status} onChange={handleChange}>
                        {seatStatuses.map((s) => (
                            <option key={s.value} value={s.value}>{s.label}</option>
                        ))}
                    </select>

                    <div style={{ margin: '20px 0', padding: '15px', border: '1px solid #ddd', borderRadius: '4px' }}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '16px', fontWeight: 'bold' }}>
                            <input 
                                type="checkbox" 
                                checked={isBatch} 
                                onChange={(e) => setIsBatch(e.target.checked)} 
                            />
                            일괄 생성 모드 (범위 지정)
                        </label>
                        
                        {isBatch ? (
                            <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                                <div>
                                    <label>시작 번호</label>
                                    <input type="number" name="startNum" value={batchForm.startNum} onChange={handleBatchChange} />
                                </div>
                                <div style={{ display: 'flex', alignItems: 'end', paddingBottom: '10px' }}>~</div>
                                <div>
                                    <label>끝 번호</label>
                                    <input type="number" name="endNum" value={batchForm.endNum} onChange={handleBatchChange} />
                                </div>
                            </div>
                        ) : (
                            <div style={{ marginTop: '10px' }}>
                                <label>좌석 번호</label>
                                <input name="seatNumber" value={form.seatNumber} onChange={handleChange} placeholder="개별 좌석 번호" required={!isBatch} />
                            </div>
                        )}
                    </div>

                    <button type="submit" disabled={loading || (isBatch && loading)}>
                        {loading ? '처리 중...' : (isBatch ? '일괄 생성하기' : '등록하기')}
                    </button>
                    {message && <p className="ticketing-success">{message}</p>}
                    {error && <p className="ticketing-error">{error}</p>}
                </form>
            </div>
        );
    }

    return (
        <div className="ticketing-admin">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1>좌석 데이터 관리</h1>
                <button className="btn-primary" onClick={() => setViewMode('FORM')}>+ 신규 좌석 등록</button>
            </div>

            <div className="ticketing-list">
                <div style={{ marginBottom: '20px', padding: '15px', background: '#f5f5f5', borderRadius: '4px' }}>
                     <label style={{ marginRight: '10px', fontWeight: 'bold' }}>공연장 선택하여 조회:</label>
                     <select onChange={(e) => loadSeats(e.target.value)} style={{ padding: '5px' }}>
                        <option value="">-- 공연장 선택 --</option>
                        {venues.map((v) => (
                            <option key={v.venueId} value={v.venueId}>{v.name}</option>
                        ))}
                    </select>
                </div>
                
                <h2>좌석 목록 ({seats.length}개)</h2>
                {loading ? <p>불러오는 중...</p> : (
                    <div style={{ maxHeight: '500px', overflowY: 'auto', border: '1px solid #eee' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                            <thead style={{ position: 'sticky', top: 0, background: '#f5f5f5' }}>
                                <tr>
                                    <th style={{ padding: '8px', textAlign: 'left' }}>구역</th>
                                    <th style={{ padding: '8px', textAlign: 'left' }}>열</th>
                                    <th style={{ padding: '8px', textAlign: 'left' }}>번호</th>
                                    <th style={{ padding: '8px', textAlign: 'left' }}>가격</th>
                                    <th style={{ padding: '8px', textAlign: 'left' }}>상태</th>
                                </tr>
                            </thead>
                            <tbody>
                                {seats.length === 0 && <tr><td colSpan="5" style={{ padding: '15px', textAlign: 'center' }}>좌석이 없습니다.</td></tr>}
                                {seats.map((s) => (
                                    <tr key={s.seatId} style={{ borderBottom: '1px solid #eee' }}>
                                        <td style={{ padding: '8px' }}>{s.sectionName}</td>
                                        <td style={{ padding: '8px' }}>{s.rowLabel}</td>
                                        <td style={{ padding: '8px', fontWeight: 'bold' }}>{s.seatNumber}</td>
                                        <td style={{ padding: '8px' }}>{s.basePrice?.toLocaleString()}원</td>
                                        <td style={{ padding: '8px' }}>
                                            <span className={`status-badge ${s.status}`}>{s.status}</span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SeatAdminPage;
