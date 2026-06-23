# Knowledge Nexus

**Connect. Learn. Grow.**

A full-stack mentorship platform where mentees discover mentors, send and accept mentorship requests, chat in real time, and schedule sessions with **real Google Meet links** — generated through the actual Google Calendar API, not a placeholder.

---

## ✨ Features

- **Mentor Discovery** — browse and search mentors by skill, with a recommendation engine surfacing relevant matches
- **Request Workflow** — send mentorship requests, accept/reject from a dedicated request hub
- **Real-time Chat** — WebSocket (STOMP) powered messaging with online/typing indicators
- **Session Scheduling with Real Google Meet**
  - Organizer connects their Google account (OAuth2)
  - Recipient accepts the session request
  - Backend creates a real Google Calendar event with auto-generated Meet conferencing
  - Both participants get an actual, joinable link
- **Notifications** — centralized notification feed for requests, messages, and session updates
- **Skill Catalog** — searchable skill catalog synced from an external API, attach skills to your profile
- **Authentication**
  - Email/password login with JWT
  - "Sign in with Google" (Google Identity Services, ID token verification)
- **Dashboard** — mentor/mentee activity overview (mentees, active chats, sessions, ratings)

---

## 🛠 Tech Stack

**Backend**
- Java 21, Spring Boot 3.5
- Spring Security (JWT-based, stateless)
- Spring Data JPA + Hibernate
- PostgreSQL
- Flyway (schema migrations)
- WebSocket / STOMP (real-time chat)
- Google Calendar API, Google OAuth2, Google Identity Services

**Frontend**
- React + TypeScript
- Vite
- TanStack Query (React Query)
- Tailwind CSS
- Axios

---

## 🏗 Architecture

```
┌─────────────────┐        REST + WebSocket        ┌──────────────────┐
│  React Frontend  │ ───────────────────────────▶  │  Spring Boot API  │
│  (Vite, :5173)   │ ◀───────────────────────────  │     (:8080)       │
└─────────────────┘                                 └──────────────────┘
                                                            │
                                ┌───────────────────────────┼───────────────────────────┐
                                ▼                           ▼                           ▼
                        ┌───────────────┐          ┌─────────────────┐         ┌──────────────────┐
                        │  PostgreSQL    │          │  Google OAuth2   │         │  Google Calendar  │
                        │  (Flyway)      │          │  (login + token) │         │  API (Meet links) │
                        └───────────────┘          └─────────────────┘         └──────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21
- Node.js 18+
- PostgreSQL 14+
- A Google Cloud project with **OAuth 2.0 Client ID** credentials and the **Google Calendar API** enabled

---

### 1. Google Cloud setup

1. Go to [Google Cloud Console](https://console.cloud.google.com) → create/select a project
2. **APIs & Services → Library** → enable **Google Calendar API**
3. **APIs & Services → OAuth consent screen** → add scope `https://www.googleapis.com/auth/calendar.events`, add test users (while in Testing mode)
4. **APIs & Services → Credentials** → create an **OAuth 2.0 Client ID** (Web application)
   - Authorized JavaScript origins: `http://localhost:5173`
   - Authorized redirect URIs: `http://localhost:8080/api/google/oauth/callback`
5. Copy the **Client ID** and **Client Secret**

---

### 2. Backend setup

```bash
cd backend
```

Create/update `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: knowledge-nexus
  datasource:
    url: jdbc:postgresql://localhost:5432/knowledge_nexus
    username: <your-db-username>
    password: <your-db-password>
    driver-class-name: org.postgresql.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    show-sql: true

server:
  port: 8080

app:
  cors:
    allowed-origins:
      - http://localhost:5173
  google:
    client-id: <your-google-client-id>.apps.googleusercontent.com
  frontend-url: http://localhost:5173

google:
  client-id: <your-google-client-id>.apps.googleusercontent.com
  client-secret: <your-google-client-secret>
  redirect-uri: http://localhost:8080/api/google/oauth/callback

jwt:
  secret: <a-long-random-secret-string>
  expiration-ms: 86400000
```

> `app.google.client-id` and `google.client-id` both need the **same** value — one is read by the login token verifier, the other by the Calendar integration.

Create the database:

```sql
CREATE DATABASE knowledge_nexus;
```

Run it:

```bash
./mvnw spring-boot:run
```

Flyway will apply all migrations automatically on startup. Backend runs on `http://localhost:8080`.

---

### 3. Frontend setup

```bash
cd frontend
npm install
```

Create `.env`:

```dotenv
VITE_GOOGLE_CLIENT_ID=<your-google-client-id>.apps.googleusercontent.com
VITE_BACKEND_TARGET=http://localhost:8080
```

Run it:

```bash
npm run dev
```

Frontend runs on `http://localhost:5173`.

---

## 📡 Key API Endpoints

| Method | Endpoint                            | Description                              |
|--------|--------------------------------------|-------------------------------------------|
| POST   | `/api/auth/register`                 | Register with email/password              |
| POST   | `/api/auth/login`                    | Login, returns JWT                         |
| POST   | `/api/auth/google`                   | Login/register via Google ID token         |
| GET    | `/api/google/oauth/connect`          | Start Google Calendar OAuth flow           |
| GET    | `/api/google/oauth/callback`         | OAuth callback, stores tokens               |
| POST   | `/api/meetings`                      | Request a mentorship session               |
| POST   | `/api/meetings/{id}/accept`          | Accept a session → creates real Meet link   |
| POST   | `/api/meetings/{id}/decline`         | Decline a session request                  |
| GET    | `/api/meetings`                      | List all sessions for the current user      |
| GET    | `/api/dashboard`                     | Dashboard stats                            |

---

## 📂 Project Structure

```
backend/
  src/main/java/com/knowledgenexus/
    controller/   → REST controllers
    service/      → Business logic (auth, meetings, Google integration, notifications)
    model/        → JPA entities
    repository/   → Spring Data repositories
    dto/          → Request/response DTOs
    security/     → JWT provider, filters, user details
    config/       → Security, CORS, WebSocket config
  src/main/resources/
    db/migration/ → Flyway SQL migrations

frontend/
  src/
    pages/        → Route-level pages (Dashboard, Chat, Meetings, Requests, etc.)
    components/   → Reusable UI components
    context/      → Auth context
    services/     → API client (Axios)
```

---

## 🗺 Roadmap

- [ ] Encrypt stored Google OAuth tokens at rest
- [ ] Unit/integration test coverage
- [ ] Session reschedule/cancel flow (with Calendar event updates)
- [ ] Mentor rating & review analytics
- [ ] Production deployment (Docker + CI/CD)

---

## 👤 Author

**Vishal Kesharwani**
🔗 [LinkedIn](https://linkedin.com/in/vishal-kesharwani02) · [Portfolio](https://vishalkesarwani.in) · [GitHub](https://github.com/vishal-kesharwani)
