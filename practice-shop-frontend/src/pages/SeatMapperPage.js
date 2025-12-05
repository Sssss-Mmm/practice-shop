import React, { useEffect, useRef, useState } from 'react';
import './SeatMapperPage.css';

const SeatMapperPage = () => {
    const [backgroundUrl, setBackgroundUrl] = useState('');
    const [zones, setZones] = useState([]);
    const [currentPoints, setCurrentPoints] = useState([]);
    const [drawing, setDrawing] = useState(false);
    const [form, setForm] = useState({
        name: '',
        grade: 'VIP',
        price: '',
    });
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');
    const stageRef = useRef(null);

    const handleImageUpload = (e) => {
        const file = e.target.files?.[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = () => setBackgroundUrl(reader.result.toString());
        reader.readAsDataURL(file);
    };

    const startDrawing = () => {
        if (!form.name) {
            setError('구역 이름을 입력하세요.');
            return;
        }
        setDrawing(true);
        setCurrentPoints([]);
        setMessage('도면 위를 클릭해 구역을 그려주세요. (3점 이상)');
        setError('');
    };

    const handleStageClick = (e) => {
        if (!drawing || !stageRef.current) return;
        const rect = stageRef.current.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        setCurrentPoints((prev) => [...prev, { x, y }]);
    };

    const finishZone = () => {
        if (currentPoints.length < 3) {
            setError('3개 이상 점을 찍어야 구역을 완성할 수 있습니다.');
            return;
        }
        const zone = {
            id: `${form.name}-${zones.length + 1}`,
            name: form.name,
            grade: form.grade,
            price: form.price ? Number(form.price) : null,
            points: currentPoints,
        };
        setZones((prev) => [...prev, zone]);
        setCurrentPoints([]);
        setDrawing(false);
        setMessage('구역이 추가되었습니다.');
        setError('');
    };

    const resetDrawing = () => {
        setCurrentPoints([]);
        setDrawing(false);
        setMessage('');
        setError('');
    };

    const copyJson = async () => {
        const payload = JSON.stringify(zones, null, 2);
        try {
            await navigator.clipboard.writeText(payload);
            setMessage('구역 JSON을 클립보드에 복사했습니다.');
        } catch (err) {
            setError('복사에 실패했습니다. 수동으로 복사하세요.');
        }
    };

    useEffect(() => {
        if (!message && !error) return;
        const t = setTimeout(() => {
            setMessage('');
            setError('');
        }, 3000);
        return () => clearTimeout(t);
    }, [message, error]);

    return (
        <div className="seat-mapper-page">
            <h1>좌석 매퍼 (간이 폴리곤 도구)</h1>
            <p className="muted">도면 이미지를 배경으로 올리고, 구역을 클릭해서 그리세요. 저장/전송은 JSON으로 추출해 백엔드에 맞게 사용하세요.</p>

            <div className="mapper-grid">
                <div className="mapper-controls">
                    <h3>1) 도면 업로드</h3>
                    <input type="file" accept="image/*" onChange={handleImageUpload} />
                    {backgroundUrl && <p className="muted">이미지 업로드됨</p>}

                    <h3>2) 구역 정보</h3>
                    <label>구역 이름
                        <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="예: 1층 R존" />
                    </label>
                    <label>등급/좌석유형
                        <select value={form.grade} onChange={(e) => setForm({ ...form, grade: e.target.value })}>
                            <option value="VIP">VIP</option>
                            <option value="R">R</option>
                            <option value="S">S</option>
                            <option value="A">A</option>
                            <option value="B">B</option>
                            <option value="C">C</option>
                            <option value="ETC">기타</option>
                        </select>
                    </label>
                    <label>가격
                        <input type="number" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} placeholder="예: 100000" />
                    </label>

                    <div className="btn-row">
                        <button className="btn btn-primary" type="button" onClick={startDrawing} disabled={drawing}>구역 그리기 시작</button>
                        <button className="btn btn-outline-secondary" type="button" onClick={finishZone} disabled={!drawing || currentPoints.length < 3}>구역 완료</button>
                        <button className="btn btn-outline-danger" type="button" onClick={resetDrawing}>취소</button>
                    </div>

                    <h3>3) 구역 목록 / JSON 내보내기</h3>
                    <div className="zone-list">
                        {zones.length === 0 && <p className="muted">등록된 구역이 없습니다.</p>}
                        {zones.map((z) => (
                            <div key={z.id} className="zone-item">
                                <strong>{z.name}</strong> ({z.grade}) {z.price ? `${z.price.toLocaleString()}원` : '가격없음'} - 점 {z.points.length}개
                            </div>
                        ))}
                    </div>
                    {zones.length > 0 && (
                        <div className="btn-row">
                            <button className="btn btn-outline-secondary" type="button" onClick={copyJson}>JSON 복사</button>
                            <details>
                                <summary>JSON 보기</summary>
                                <pre className="json-preview">{JSON.stringify(zones, null, 2)}</pre>
                            </details>
                        </div>
                    )}
                </div>

                <div className="mapper-stage-wrapper">
                    <div
                        className={`mapper-stage ${drawing ? 'drawing' : ''}`}
                        ref={stageRef}
                        onClick={handleStageClick}
                        style={backgroundUrl ? { backgroundImage: `url(${backgroundUrl})` } : {}}
                    >
                        <svg className="mapper-svg">
                            {zones.map((z) => (
                                <polygon
                                    key={z.id}
                                    points={z.points.map((p) => `${p.x},${p.y}`).join(' ')}
                                    className="zone-polygon"
                                />
                            ))}
                            {currentPoints.length > 0 && (
                                <>
                                    <polyline
                                        points={currentPoints.map((p) => `${p.x},${p.y}`).join(' ')}
                                        className="zone-polyline"
                                    />
                                    {currentPoints.map((p, idx) => (
                                        <circle key={idx} cx={p.x} cy={p.y} r="4" className="zone-point" />
                                    ))}
                                </>
                            )}
                        </svg>
                        {!backgroundUrl && <div className="stage-placeholder">도면 이미지를 업로드하세요</div>}
                    </div>
                    <p className="muted">클릭으로 점을 추가하고, 3개 이상 점을 찍은 후 “구역 완료”를 누르세요.</p>
                    {message && <div className="alert alert-success mt-2">{message}</div>}
                    {error && <div className="alert alert-danger mt-2">{error}</div>}
                </div>
            </div>
        </div>
    );
};

export default SeatMapperPage;
