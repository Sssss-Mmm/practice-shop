# Practice Shop

**Practice Shop**은 Spring Boot와 React를 기반으로 구축된 티켓팅 및 이커머스 실습 프로젝트입니다.  
대용량 트래픽 상황을 가정한 대기열 시스템, 실시간 좌석 선점, 결제 시스템 연동 등 다양한 기능을 포함하고 있습니다.

## 🛠 Tech Stack

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.7
- **Data**: Spring Data JPA (Hibernate), PostgreSQL
- **Cache & Queue**: Redis
- **Security**: Spring Security, OAuth2 (Google), JWT
- **Realtime**: WebSocket (STOMP)
- **Payment**: Toss Payments API
- **Docs**: Swagger (SpringDoc)

### Frontend
- **Framework**: React 19
- **UI Library**: Material UI (MUI) v7
- **Networking**: Axios
- **Realtime**: SockJS, StompJS
- **Routing**: React Router v7

### Infrastructure
- **Docker & Docker Compose** (App, PostgreSQL, Redis, PgAdmin)

## ✨ Key Features

1.  **사용자 인증 (Authentication)**
    - 이메일 로그인 및 Google OAuth2 소셜 로그인
    - JWT 기반 인증/인가 처리

2.  **티켓팅 시스템 (Ticketing)**
    - 공연(Event) 및 회차(Showtime) 조회
    - 구역(Section) 및 좌석(Seat) 선택
    - **실시간 좌석 상태 공유**: WebSocket을 통해 다른 사용자가 선점한 좌석을 실시간으로 확인

3.  **대기열 시스템 (Queue System)**
    - Redis를 활용한 접속 대기열 구현
    - 트래픽 폭주 시 서버 부하 제어 및 순차적 진입 처리

4.  **결제 및 환불 (Payment)**
    - Toss Payments 연동
    - 결제 검증 및 예매 확정
    - 예약 취소 시 자동 환불 처리

5.  **관리자 기능 (Admin)**
    - 공연, 공연장, 좌석 배치 관리 (Batch Create 지원)

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose

### Environment Setup
프로젝트 루트에 `.env` 파일을 생성하고 다음 환경 변수를 설정해야 합니다. (기본값 참고 `docker-compose.yml`)

```properties
# Database
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password
POSTGRES_DB=shop
DB_URL=jdbc:postgresql://db:5432/shop
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key_must_be_long_enough

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Mail (Optional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password
MAIL_FROM=your_email

# Toss Payments
TOSS_SECRET_KEY=your_toss_payments_secret_key
```

### Run with Docker Compose

전체 애플리케이션(DB, Redis, Backend)을 Docker로 실행합니다.

```bash
docker-compose up -d --build
```

### Run Locally

**Backend**
```bash
./gradlew bootRun
```
- Server: `http://localhost:8084`
- Swagger UI: `http://localhost:8084/swagger-ui/index.html`

**Frontend**
```bash
cd practice-shop-frontend
npm install
npm start
```
- Client: `http://localhost:3000`

## 📚 API Documentation
서버 실행 후 `/swagger-ui/index.html` 경로에서 Swagger 문서를 확인할 수 있습니다.
