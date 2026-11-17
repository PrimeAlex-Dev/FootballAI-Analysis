# AI Football Analysis

Nền tảng dữ liệu bóng đá phục vụ quản trị, truy vấn lịch sử và phân tích AI trong các giai đoạn tiếp theo.

## Trạng thái

Chỉ khởi tạo cấu trúc repository. Chưa có code ứng dụng, module nghiệp vụ, API, database schema hoặc CI/CD workflow. Chưa có Reference Module được duyệt.

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

`frontend/package.json` chỉ chứa metadata. Hai Dockerfile là placeholder; Compose chưa có service; thư mục workflows chưa có pipeline. Chưa có lệnh chạy ứng dụng hoặc triển khai ở giai đoạn này.

Các thư mục source rỗng được giữ bằng `.gitkeep`. Không commit `.env` hoặc thông tin bí mật.
