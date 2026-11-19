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

## Frontend

| Công nghệ | Vai trò |
|---|---|
| React + TypeScript | Giao diện quản trị Admin |
| Vite | Dev server và production build |
| Node.js + npm | Chạy công cụ frontend và quản lý dependency |
| Oxlint | Kiểm tra lint |

Phiên bản frontend được khai báo trong frontend/package.json; phiên bản cài đặt cụ thể nằm trong frontend/package-lock.json. Phiên bản Node.js chung cho nhóm chưa chốt.

## Dữ liệu và hạ tầng

PostgreSQL, JPA/Hibernate và Flyway đã tích hợp. Docker Compose chạy database; GitHub Actions đã có backend/frontend CI. Docker hóa backend/frontend và Docker build CI thuộc task tiếp theo.

| Công nghệ | Vai trò |
|---|---|
| PostgreSQL 18.7 | Database quan hệ, image Docker chính thức |
| Spring Data JPA + Hibernate | Truy cập dữ liệu và ánh xạ entity |
| Flyway | Quản lý phiên bản schema database |
| Docker + Docker Compose | Đóng gói và chạy các thành phần ứng dụng |
| GitHub Actions | CI: backend build/test với PostgreSQL, frontend lint/typecheck/build |

## Quản lý mã nguồn

- **Git + GitHub:** quản lý phiên bản, Pull Request và review.
- **GitHub Flow + Merge Commit:** quy trình tích hợp thay đổi vào main.
