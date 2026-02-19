# 🚀 SaaS Platform Access Guide

This document outlines how to access the various components of the Mini-Market SaaS ecosystem.

---

## 🌐 Main Entry Points

| Component | Access URL | Description |
| :--- | :--- | :--- |
| **Frontend UI** | [http://localhost](http://localhost) | The main React Dashboard. |
| **API (via Proxy)** | [http://localhost/api/auth/login](http://localhost/api/auth/login) | Test the API entry point (will return 405/400 but proves connectivity). |
| **Swagger UI** | [http://localhost/api/swagger-ui.html](http://localhost/api/swagger-ui.html) | Technical documentation & API Playground. |
| **Health Check** | [http://localhost/api/actuator/health](http://localhost/api/actuator/health) | Verify backend is UP and database is connected. |
| **Backend (Direct)** | [http://localhost:8081](http://localhost:8081) | Direct access to the Spring Boot service (Debugging). |

---

## 🛠️ Infrastructure & Dashboards

| Service | Access URL | Description |
| :--- | :--- | :--- |
| **Mailhog** | [http://localhost:8025](http://localhost:8025) | View sent emails (Welcome emails, notifications). |
| **Grafana** | [http://localhost:3000](http://localhost:3000) | Performance dashboards (User/Pass: `admin/admin`). |
| **Prometheus** | [http://localhost:9090](http://localhost:9090) | Raw metrics and time-series data. |
| **Redis Commander** | [http://localhost:8083](http://localhost:8083) | Redis GUI. |
| **Cadvisor** | [http://localhost:8082](http://localhost:8082) | Container resource monitoring. |

---

## 🔄 API Communication Flow (How it works)

1.  **Request**: The **React Frontend** sends a request to `/api/auth/login`.
2.  **Proxy**: The **Nginx** container (listening on port 80) receives this request.
3.  **Routing**: Nginx sees the `/api` prefix and forwards the request to the **Java Backend** inside the `saas-network`.
4.  **Security**: The Java backend validates the request (JWT/Tenant-ID) and returns the data.
5.  **Response**: Nginx sends the response back to the browser.

> [!TIP]
> Always use the **Proxied API** (`http://localhost/api`) for frontend testing to avoid CORS issues and simulate the production environment.
