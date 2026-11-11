import axios from 'axios';
import authService from './auth.service';

const API_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8084/api';

class QueueService {
    // 사용자는 서버가 로그인 토큰에서 판별한다. (토큰을 사용자에 묶어 양도·대리 진입 방지)
    enterQueue(eventId) {
        return axios.post(`${API_URL}/queue/enter/${eventId}`, null, {
            headers: authService.authHeader()
        });
    }

    getQueueStatus(token) {
        return axios.get(`${API_URL}/queue/status`, {
            params: { token },
            headers: { 
                ...authService.authHeader()
            }
        });
    }
}

const queueService = new QueueService();
export default queueService;
