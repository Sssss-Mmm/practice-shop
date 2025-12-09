import React, { useEffect, useState, useRef } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import QueueService from '../services/queue.service';
import AuthService from '../services/auth.service';
import './QueueWaitingPage.css';

const QueueWaitingPage = () => {
    const { eventId, showtimeId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    
    // Attempt to get userId if logged in, otherwise use anonymous or random
    const [userId, setUserId] = useState(() => {
        const user = AuthService.getCurrentUser();
        return user ? user.email : `anon-${Math.random().toString(36).substr(2, 9)}`;
    });

    const [token, setToken] = useState(null);
    const [position, setPosition] = useState(null);
    const [error, setError] = useState(null);
    const pollingRef = useRef(null);

    // Step 1: Enter Queue
    useEffect(() => {
        const enterQueue = async () => {
            try {
                const response = await QueueService.enterQueue(eventId, userId);
                setToken(response.data.token);
                setPosition(response.data.position);
            } catch (err) {
                console.error("Failed to enter queue", err);
                setError("대기열 진입에 실패했습니다.");
            }
        };

        if (eventId) {
            enterQueue();
        }
    }, [eventId, userId]);

    // Step 2: Poll Status
    useEffect(() => {
        if (!token) return;

        const checkStatus = async () => {
            try {
                const response = await QueueService.getQueueStatus(token);
                const { ready, position: currentPosition } = response.data;

                if (ready) {
                    // Ready to book! Redirect to Seat Selection
                    clearInterval(pollingRef.current);
                    navigate(`/book/showtime/${showtimeId}/seats`, { state: { queueToken: token } });
                } else {
                    setPosition(currentPosition);
                }
            } catch (err) {
                console.error("Failed to check queue status", err);
                // Don't stop polling immediately on one error, but maybe warn if persistent
            }
        };

        // Poll every 3 seconds
        pollingRef.current = setInterval(checkStatus, 3000);

        // Initial check
        checkStatus();

        return () => {
            if (pollingRef.current) clearInterval(pollingRef.current);
        };
    }, [token, showtimeId, navigate]);

    if (error) {
        return (
            <div className="queue-waiting-container">
                <div className="queue-box error">
                    <h3>오류 발생</h3>
                    <p>{error}</p>
                    <button onClick={() => navigate(-1)}>돌아가기</button>
                </div>
            </div>
        );
    }

    return (
        <div className="queue-waiting-container">
            <div className="queue-box">
                <h2 className="queue-title">접속 대기 중입니다</h2>
                <div className="queue-info">
                    {position !== null ? (
                        <>
                             {position === -1 ? (
                                <p>순번 확인 중...</p>
                             ) : (
                                <>
                                    <p>현재 내 앞 대기 인원</p>
                                    <div className="queue-position">{position}명</div>
                                </>
                             )}
                        </>
                    ) : (
                        <div className="queue-loading">
                            <div className="spinner"></div>
                            <p>대기열 진입 중...</p>
                        </div>
                    )}
                </div>
                <p className="queue-message">
                    잠시만 기다려주시면 예매 페이지로 이동합니다.<br/>
                    새로고침을 하시면 순번이 라납니다.
                </p>
            </div>
        </div>
    );
};

export default QueueWaitingPage;
