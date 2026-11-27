# AI Football Analysis — Tech Stack

## Kiến trúc

- **Monorepo:** backend, frontend và tài liệu trong cùng repository.
- **Modular Monolith:** một ứng dụng backend, chia module theo nghiệp vụ.

## Backend

| Công nghệ | Vai trò |
|---|---|
| Java 21 | Ngôn ngữ và nền tảng chạy backend |
| Spring Boot 4.1.2 | Khởi tạo và cấu hình ứng dụng |
| Spring MVC | Xây dựng REST API |
| Spring Boot Actuator | Health endpoint |
| Maven Wrapper 3.9.13 | Build và quản lý dependency |
| Spring Boot Starter Test | Bộ công cụ kiểm thử backend |
| springdoc OpenAPI 3.2.0 | Sinh OpenAPI và cung cấp Swagger UI để thử API |
| Spotless 3.11.0 + Eclipse formatter 4.40 | Format/check Java, 4 spaces |

## Frontend

| Công nghệ | Vai trò |
|---|---|
| React + TypeScript | Giao diện quản trị Admin |
| Vite | Dev server và production build |
| Nginx 1.30.7 Alpine | Phục vụ frontend đã build và reverse proxy tới backend |
| Node.js + npm | Chạy công cụ frontend và quản lý dependency |
| Oxlint | Kiểm tra lint |
| Prettier 3.10.1 | Format/check frontend và cấu hình được chọn |

Phiên bản frontend khai báo trong frontend/package.json và được khóa trong frontend/package-lock.json. Docker build và CI dùng Node.js 24.15.0.

## Dữ liệu và hạ tầng

PostgreSQL, JPA/Hibernate và Flyway đã tích hợp. Docker Compose chạy cả database, backend và frontend. Backend dùng Eclipse Temurin Java 21; frontend build bằng Node.js 24.15.0 rồi chạy trên Nginx. GitHub Actions có backend/frontend CI và build hai Docker image bằng Buildx; chưa push image hoặc deploy.

| Công nghệ | Vai trò |
|---|---|
| PostgreSQL 18.7 | Database phát triển có volume; database test riêng qua Compose profile test và tmpfs |
| Spring Data JPA + Hibernate | Truy cập dữ liệu và ánh xạ entity |
| Flyway | Quản lý phiên bản schema database |
| Docker + Docker Compose | Đóng gói và chạy các thành phần ứng dụng |
| GitHub Actions | CI: backend build/test với PostgreSQL, frontend lint/typecheck/build, Docker build backend/frontend |

## Quản lý mã nguồn

- **Git + GitHub:** quản lý phiên bản, Pull Request và review.
- **GitHub Flow + Merge Commit:** quy trình tích hợp thay đổi vào main.

Frontend quản trị dùng React Router để điều hướng, Lucide React cho icon và CSS variables cho theme. Chi tiết tại [Dashboard UI](dashboard-ui.md).

Backend Docker build dùng image Maven 3.9.13 với Eclipse Temurin Java 21 để tránh tải bản phân phối Maven Wrapper trong lúc build image. Bước package vẫn giữ format check và -DskipTests; test PostgreSQL chạy riêng trong Backend CI. Maven Wrapper vẫn dùng cho local và Backend CI.
