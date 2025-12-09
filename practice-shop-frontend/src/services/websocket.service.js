import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
    constructor() {
        this.client = null;
        this.isConnected = false;
    }

    connect(onConnect, onError) {
        if (this.client && this.client.active) {
            if (onConnect) onConnect(); // Already connected
            return;
        }

        const socketUrl = process.env.REACT_APP_WS_URL || 'http://localhost:8084/ws';

        this.client = new Client({
            webSocketFactory: () => new SockJS(socketUrl),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: (frame) => {
                console.log('Connected to WebSocket');
                this.isConnected = true;
                if (onConnect) onConnect(frame);
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
                if (onError) onError(frame);
            },
            onWebSocketClose: () => {
                console.log('WebSocket connection closed');
                this.isConnected = false;
            }
        });

        this.client.activate();
    }

    subscribe(topic, callback) {
        if (!this.client || !this.client.active) {
            console.error('WebSocket is not connected. Cannot subscribe.');
            return null;
        }
        return this.client.subscribe(topic, (message) => {
            if (message.body) {
                callback(JSON.parse(message.body));
            }
        });
    }

    send(destination, body = {}) {
        if (!this.client || !this.client.active) {
            console.error('WebSocket is not connected. Cannot send.');
            return;
        }
        this.client.publish({
            destination: destination,
            body: JSON.stringify(body)
        });
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.client = null;
            this.isConnected = false;
            console.log('Disconnected from WebSocket');
        }
    }
}

const webSocketService = new WebSocketService();
export default webSocketService;
