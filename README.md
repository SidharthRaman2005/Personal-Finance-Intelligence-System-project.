# FinanceApp

A full-stack personal finance application for tracking income, expenses, savings, goals, investments, and advanced financial analysis.

## Project Overview

FinanceApp helps users manage day-to-day personal finances with a modern dashboard and analysis tools. The project is organized as a monorepo with separate backend and frontend applications.

## Tech Stack

- Backend: Java 17, Spring Boot 3, Spring Data JPA, Spring Security, JWT, H2
- Frontend: React 19, Vite, Tailwind CSS, Recharts
- Build Tools: Maven (backend), npm (frontend)

## Repository Structure

- backend/backend: Spring Boot API server
- frontend: React + Vite web app

## Core Features

- Authentication and account flow (JWT-based)
- Income and expense tracking
- Dashboard with total-based savings rate and health score
- Advanced analysis modules:
  - Hidden Expense Detector
  - Financial Twin Simulator
- Investment tracking with simplified entry flow

## Local Setup

### Prerequisites

- Java 17+
- Node.js 18+ and npm
- Git

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd financeapp
```

### 2. Run backend

```bash
cd backend/backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend runs on: http://localhost:8080

### 3. Run frontend

Open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: http://localhost:5173

## Build Commands

### Backend compile

```bash
cd backend/backend
./mvnw -q -DskipTests compile
```

### Frontend production build

```bash
cd frontend
npm run build
```

## Configuration Notes

- Backend config file: backend/backend/src/main/resources/application.properties
- Update SMTP and JWT settings before production use.
- Do not commit secrets. Use environment-specific configs for deployment.

## Git Workflow (Recommended)

- Main branch: master (or rename to main if preferred)
- Create feature branches from mainline:
  - feature/<short-description>
  - fix/<short-description>
- Open pull requests for review before merge.

Useful commands:

```bash
git checkout -b feature/your-change
git add .
git commit -m "feat: describe your change"
git push -u origin feature/your-change
```

## Roadmap

- Add MySQL/PostgreSQL production profile
- Introduce CI checks (build + test + lint)
- Add automated API tests and frontend unit tests
- Deploy backend and frontend with environment-based config
- Add role-based access and richer analytics insights

## License

Add your preferred license (MIT/Apache-2.0/etc.) before public distribution.
