class TradeWebSocket {
    constructor() {
        this.socket = null;
        this.subscribers = new Map();
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectInterval = 3000;
        this.isConnecting = false;
        this.connect();
    }

    connect() {
        if (this.isConnecting) return;

        this.isConnecting = true;
        const wsUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/trade`;

        try {
            this.socket = new SockJS('/ws/trade');
            this.stompClient = Stomp.over(this.socket);

            // STOMP 클라이언트 설정
            this.stompClient.connect(
                {},
                () => this.onConnect(),
                error => this.onError(error),
                () => this.onClose()
            );
        } catch (error) {
            console.error('WebSocket 연결 실패:', error);
            this.handleReconnect();
        }
    }

    onConnect() {
        console.log('WebSocket 연결 성공');
        this.isConnecting = false;
        this.reconnectAttempts = 0;

        // 기존 구독 복구
        this.subscribers.forEach((callback, coinCode) => {
            this.subscribeToCoin(coinCode);
        });
    }

    onError(error) {
        console.error('WebSocket 에러:', error);
        this.handleReconnect();
    }

    onClose() {
        console.log('WebSocket 연결 종료');
        this.handleReconnect();
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            setTimeout(() => {
                this.isConnecting = false;
                this.connect();
            }, this.reconnectInterval);
        } else {
            this.handleMaxReconnectError();
        }
    }

    handleMaxReconnectError() {
        Swal.fire({
            icon: 'error',
            title: '연결 오류',
            text: '서버와의 연결이 끊어졌습니다. 페이지를 새로고침해주세요.',
            confirmButtonText: '새로고침',
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.reload();
            }
        });
    }

    subscribe(coinCode, callback) {
        if (!coinCode || typeof callback !== 'function') {
            console.error('Invalid subscription parameters');
            return;
        }

        this.subscribers.set(coinCode, callback);

        if (this.stompClient && this.stompClient.connected) {
            this.subscribeToCoin(coinCode);
        }
    }

    subscribeToCoin(coinCode) {
        if (!this.stompClient || !this.stompClient.connected) {
            console.error('STOMP client is not connected');
            return;
        }

        this.stompClient.subscribe(`/topic/coin/${coinCode}`, message => {
            try {
                const data = JSON.parse(message.body);
                const callback = this.subscribers.get(coinCode);
                if (callback) callback(data);
            } catch (error) {
                console.error('Message parsing error:', error);
            }
        });
    }

    unsubscribe(coinCode) {
        if (!coinCode) return;

        this.subscribers.delete(coinCode);
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.unsubscribe(`/topic/coin/${coinCode}`);
        }
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
            this.stompClient = null;
        }
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
        this.subscribers.clear();
    }
}