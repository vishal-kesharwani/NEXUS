# Knowledge Nexus

<div align="center">

# 🎓 Knowledge Nexus

### Connect • Learn • Mentor • Grow

*A modern mentorship platform that enables professionals and students to discover mentors, collaborate through dedicated mentorship workspaces, and conduct real Google Meet mentoring sessions.*

---

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=for-the-badge)
![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=for-the-badge)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge)
![JWT](https://img.shields.io/badge/Auth-JWT-red?style=for-the-badge)
![Google Calendar](https://img.shields.io/badge/Google-Calendar_API-4285F4?style=for-the-badge)

</div>

---

# ✨ Why Knowledge Nexus?

Knowledge Nexus is designed to solve a common problem inside companies, universities, and developer communities—finding the right mentor and managing mentorship professionally.

Instead of a generic chat application, every mentorship becomes its own collaborative workspace with:

* Dedicated mentorship lifecycle
* Separate conversation history
* Google Meet integration
* Skill-based mentor reputation
* Organization-aware mentor discovery

---

# 🚀 Core Features

## 👨‍🏫 Mentor Discovery

* Search mentors by skill
* Filter mentors by organization
* Experience-based mentor profiles
* Skill recommendations
* Mentor availability

---

## 🤝 Mentorship Lifecycle

```text
Discover Mentor
      │
      ▼
Send Request
      │
      ▼
Accept / Reject
      │
      ▼
Dedicated Chat Room
      │
      ▼
Schedule Google Meet
      │
      ▼
Conduct Mentorship
      │
      ▼
Rate Mentor Skill
      │
      ▼
Archive Conversation
```

Each accepted mentorship creates an independent conversation. Multiple mentorships between the same mentor and mentee never overwrite previous discussions.

---

## 💬 Real-Time Collaboration

* WebSocket messaging
* Typing indicators
* Online presence
* Conversation history
* Archived mentorship chats
* Read-only completed mentorships

---

## 📅 Google Meet Integration

Knowledge Nexus integrates directly with Google Calendar.

When a mentorship request is accepted:

* Organizer authenticates with Google OAuth2
* Backend creates Calendar Event
* Google automatically generates Meet link
* Both users receive the same meeting

Meet links support:

* Automatic expiration
* Manual deletion
* Created by Me
* Shared with Me

---

## ⭐ Dynamic Mentor Reputation

Each mentor owns their self-assessed skill level.

After every mentorship:

* Mentee reviews only the skill that was taught.
* Community feedback slightly adjusts the mentor's displayed expertise while preserving the mentor's own assessment.

---

# 🏛 System Architecture

```mermaid
flowchart LR

A[React + TypeScript]

A -->|REST API| B(Spring Boot)

A -->|WebSocket| B

B --> C[(PostgreSQL)]

B --> D[JWT Authentication]

B --> E[Google OAuth2]

E --> F[Google Calendar API]

F --> G[Google Meet]

C --> H[Flyway Migrations]
```

---

# 📂 Project Structure

```text
knowledge-nexus/

├── backend/
│   ├── src/main/java/com/knowledgenexus
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   └── websocket/
│   │
│   └── src/main/resources
│       ├── application.yml
│       └── db/migration
│
└── frontend/
    ├── src
    │   ├── components
    │   ├── context
    │   ├── hooks
    │   ├── pages
    │   ├── services
    │   ├── types
    │   └── utils
    └── public
```

---

# ⚙ Backend Configuration

## application.yml

```yaml
spring:
  application:
    name: knowledge-nexus

  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/knowledge_nexus}
    username: ${SPRING_DATASOURCE_USERNAME:nexus}
    password: ${SPRING_DATASOURCE_PASSWORD:change-me-local}
    driver-class-name: org.postgresql.Driver

server:
  port: ${SERVER_PORT:8080}

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  cors:
    allowed-origins:
      - http://localhost:5173
      - http://127.0.0.1:5173

  frontend-url: ${APP_FRONTEND_URL:http://localhost:5173}

google:
  client-id: ${GOOGLE_CLIENT_ID:change-me-client-id}
  client-secret: ${GOOGLE_CLIENT_SECRET:change-me-client-secret}
  redirect-uri: http://localhost:8080/api/google/oauth/callback

jwt:
  secret: ${JWT_SECRET:change-me-jwt-secret}
  expiration-ms: ${JWT_EXPIRATION_MS:86400000}

OPENROUTER_API_KEY: ${OPENROUTER_API_KEY:}
OPENROUTER_MODEL: ${OPENROUTER_MODEL:openrouter/free}
```

---

## docker-compose.yml

```yaml
services:
  postgres:
    image: postgres:17-alpine
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-knowledge_nexus}
      POSTGRES_USER: ${POSTGRES_USER:-nexus}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-change-me-local}

  backend:
    build:
      context: ./backend
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL:-jdbc:postgresql://postgres:5432/knowledge_nexus}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME:-nexus}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD:-change-me-local}
      GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID:-change-me-client-id}
      GOOGLE_CLIENT_SECRET: ${GOOGLE_CLIENT_SECRET:-change-me-client-secret}
      JWT_SECRET: ${JWT_SECRET:-change-me-jwt-secret}
      OPENROUTER_API_KEY: ${OPENROUTER_API_KEY:-}
```

---

## Environment File

Use [`.env.example`](/D:/nexus/NEXUS/.env.example) as the local template. Copy it to `.env` and fill real values only on your machine.

---

# 💻 Frontend

## .env.example

```env
VITE_API_BASE_URL=http://localhost:8080/api

VITE_GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID

VITE_APP_NAME=Knowledge Nexus
```

---

# 🚀 Running Locally

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend

npm install

npm run dev
```

### Docker

```bash
docker compose up --build
```

This starts:

* PostgreSQL on `localhost:5432`
* Backend on `localhost:8080`

The backend image uses Java 21, and runtime config is wired through environment variables in [`docker-compose.yml`](/D:/nexus/NEXUS/docker-compose.yml).

---

# 🧩 Tech Stack

| Layer          | Technology              |
| -------------- | ----------------------- |
| Frontend       | React, TypeScript, Vite |
| Backend        | Spring Boot, Java 21    |
| Authentication | JWT, Google OAuth2      |
| Database       | PostgreSQL              |
| ORM            | Spring Data JPA         |
| Realtime       | WebSocket + STOMP       |
| Calendar       | Google Calendar API     |
| Video          | Google Meet             |
| Migration      | Flyway                  |

---

# 📌 Roadmap

* AI Mentor Recommendation
* AI Session Summary
* Resume-based Mentor Matching
* Calendar Sync
* Docker Deployment
* Kubernetes Support
* CI/CD Pipeline
* Mobile App
* Analytics Dashboard

---

# 👨‍💻 Author

**Vishal Kesharwani**

Full Stack Developer • Java • Spring Boot • React • Cloud

If this project helped you or inspired you, consider giving it a ⭐ on GitHub.
