# AI Football Analysis

Nền tảng dữ liệu bóng đá phục vụ quản trị, truy vấn lịch sử và phân tích AI trong các giai đoạn tiếp theo.

## Trạng thái

Đã khởi tạo cấu trúc repository và frontend theo template Vite React + TypeScript mặc định. Backend đã có Spring Boot foundation và health endpoint. Chưa có module nghiệp vụ, API nghiệp vụ, database schema hoặc CI/CD workflow. Chưa có Reference Module được duyệt.

## Cấu trúc

```text
backend/
  src/main/
  src/test/
  pom.xml
  Dockerfile
frontend/
  src/
  package.json
  Dockerfile
docs/
  requirements/
  architecture/
  standards/
.github/workflows/
docker-compose.yml
.env.example
.gitignore
.editorconfig
.gitattributes
AGENTS.md
README.md
```

## Tài liệu

- [Ý tưởng và phạm vi Sprint 1](docs/requirements/ai_football_analysis_project_overview_sprint_1.md)
- [Tech Stack](docs/architecture/AI_Football_Analysis_Sprint_1_Tech_Stack.md)
- [Coding Standards](docs/standards/AI_Football_Analysis_Sprint_1_Coding_Standards.md)

Đọc các tài liệu này trước khi triển khai. Module đầu tiên được duyệt sẽ là Reference Module cho các module tiếp theo.

## Quy ước repository

Dùng monorepo cho backend, frontend và tài liệu. Quy tắc ignore chung nằm ở .gitignore root.

- [Hướng dẫn coding agent](AGENTS.md).
- .editorconfig: UTF-8, LF, newline cuối file, spaces; Java và pom.xml dùng 4 spaces, frontend/JSON/YAML dùng 2 spaces. Markdown giữ trailing spaces để bảo toàn hard line break.
- EditorConfig chưa thay thế formatter/linter hoặc tự định dạng toàn bộ file cũ.
- .gitattributes giữ LF cho text trong Git; script Windows .bat/.cmd dùng CRLF. Không renormalize hàng loạt file cũ trong task này.
- Overview là bản ý tưởng; Tech Stack và Coding Standards là chuẩn kỹ thuật. Các điểm chưa chốt cần được thống nhất.

## GitHub Flow

GitHub Foundation đã được chủ dự án thiết lập: bảo vệ main, yêu cầu PR và Squash Merge.

1. Tạo branch theo mục tiêu từ main đã cập nhật.
2. Giữ thay đổi tập trung và kiểm tra phù hợp trước khi mở PR vào main.
3. PR mô tả thay đổi, lý do, kết quả kiểm tra và migration/breaking change nếu có.
4. Hoàn thành review và kiểm tra bắt buộc, sau đó Squash Merge. Không push trực tiếp vào main.

## Công nghệ

Modular Monolith; Java + Spring Boot; Spring Data JPA/Hibernate; PostgreSQL; Flyway; React + TypeScript; Docker; GitHub CI/CD.

Backend dùng Java 21, Spring Boot 4.1.2 và Maven Wrapper 3.9.13. Có Spring MVC, Actuator health và test khởi động qua HTTP; chưa thêm database, JPA, Flyway hoặc security. Xem [backend README](backend/README.md).

Frontend sử dụng template `react-ts` từ create-vite, giữ nguyên màn hình demo mặc định. Phiên bản dependency được lưu trong `frontend/package.json` và `frontend/package-lock.json`.

Cần Git và Node.js/npm tương thích với dependency frontend. Môi trường khởi tạo: Node.js 24.15.0, npm 11.12.1; chưa chốt toolchain chung. Backend cần JDK 21 và JAVA_HOME hợp lệ; Maven Wrapper tự tải Maven 3.9.13, không cần cài Maven riêng. Lần chạy đầu cần Internet để tải Maven và dependency.

Chạy backend trong PowerShell từ thư mục `backend`:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Health: `http://localhost:8080/actuator/health`. Linux/macOS dùng `sh ./mvnw` thay cho `.\mvnw.cmd`.

Chạy frontend từ thư mục `frontend`:

```sh
npm ci
npm run dev
```

Kiểm tra bằng `npm run lint` và `npm run build`. Xem bản build bằng `npm run preview`. Chi tiết và hạn chế môi trường tại [frontend README](frontend/README.md). Thay đổi tài liệu cần kiểm tra link và chạy `git diff --check`.

Hai Dockerfile là placeholder; Compose chưa có service; thư mục workflows chưa có pipeline.

Các thư mục source rỗng được giữ bằng `.gitkeep`. Không commit `.env` hoặc thông tin bí mật.
