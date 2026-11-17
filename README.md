# AI Football Analysis

Nền tảng dữ liệu bóng đá phục vụ quản trị, truy vấn lịch sử và phân tích AI trong các giai đoạn tiếp theo.

## Trạng thái

Đã khởi tạo cấu trúc repository và frontend theo template Vite React + TypeScript mặc định. Chưa có module nghiệp vụ, API, database schema hoặc CI/CD workflow. Chưa có Reference Module được duyệt.

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
README.md
```

## Tài liệu

- [Ý tưởng và phạm vi Sprint 1](docs/requirements/ai_football_analysis_project_overview_sprint_1.md)
- [Tech Stack](docs/architecture/AI_Football_Analysis_Sprint_1_Tech_Stack.md)
- [Coding Standards](docs/standards/AI_Football_Analysis_Sprint_1_Coding_Standards.md)

Đọc các tài liệu này trước khi triển khai. Module đầu tiên được duyệt sẽ là Reference Module cho các module tiếp theo.

## Công nghệ

Modular Monolith; Java + Spring Boot; Spring Data JPA/Hibernate; PostgreSQL; Flyway; React + TypeScript; Docker; GitHub CI/CD.

`backend/pom.xml` được giữ từ skeleton có sẵn, sử dụng Maven và khai báo Java 21. Chưa thêm Spring Boot hoặc dependency. Cấu hình hiện có cần được xác nhận khi thiết lập toolchain.

Frontend sử dụng template `react-ts` từ create-vite, giữ nguyên màn hình demo mặc định. Phiên bản dependency được lưu trong `frontend/package.json` và `frontend/package-lock.json`.

Chạy frontend từ thư mục `frontend`:

```sh
npm ci
npm run dev
```

Kiểm tra bằng `npm run lint` và `npm run build`. Xem bản build bằng `npm run preview`.

Hai Dockerfile là placeholder; Compose chưa có service; thư mục workflows chưa có pipeline.

Các thư mục source rỗng được giữ bằng `.gitkeep`. Không commit `.env` hoặc thông tin bí mật.
