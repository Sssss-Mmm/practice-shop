import axios from 'axios';
import authService from './auth.service';

const API_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8084/api';

class QueueService {
    enterQueue(eventId, userId) {
        return axios.post(`${API_URL}/queue/events/${eventId}/enter`, null, {
            params: { userId },
            headers: authService.authHeader()
        });
    }

    getQueueStatus(token) {
        return axios.get(`${API_URL}/queue/status`, {
            headers: { 
                'Queue-Token': token,
                ...authService.authHeader()
            }
        });
    }
}

const queueService = new QueueService();
export default queueService;
