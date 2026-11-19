# Hướng dẫn làm việc trong repository

Áp dụng cho toàn bộ monorepo. Đọc tài liệu trước khi triển khai; không coi bản ý tưởng là nghiệp vụ đã duyệt.

## Tài liệu bắt buộc

1. [Tổng quan ý tưởng Sprint 1](docs/requirements/ai_football_analysis_project_overview_sprint_1.md).
2. [Tech Stack](docs/architecture/AI_Football_Analysis_Sprint_1_Tech_Stack.md).
3. [Coding Standards](docs/standards/AI_Football_Analysis_Sprint_1_Coding_Standards.md).

Coding Standards là nguồn quy tắc code chi tiết; không thay thế bằng pattern riêng.

## Phạm vi và kiến trúc

- `backend/`: Spring Boot 4.1.2, Java 21, Maven Wrapper 3.9.13; có Spring MVC, Actuator, PostgreSQL, Spring Data JPA/Hibernate và Flyway. Chưa có entity hoặc module nghiệp vụ.
- `frontend/`: Vite + React + TypeScript, npm và lockfile; hiện là template mặc định.
- `docs/`: requirements, architecture và standards.
- Compose chạy PostgreSQL 18.7 với named volume và health check. CI backend dùng PostgreSQL service riêng; frontend có lint/build. Dockerfile backend/frontend vẫn là placeholder.
- Chưa có Reference Module được duyệt. Khi có module mẫu được duyệt, đọc và bám sát trước khi thêm module tiếp theo.
- Giữ Modular Monolith và ranh giới module theo Coding Standards. Không tự thêm dependency, tầng kiến trúc hoặc refactor ngoài yêu cầu.
- Không tự suy diễn field, API contract, quan hệ hoặc quy tắc import chưa chốt. Nêu điểm cần quyết định và giả định quan trọng trước khi triển khai phần phụ thuộc.
- Bảo toàn lịch sử và toàn vẹn dữ liệu; không bỏ validation hoặc xử lý lỗi để code chạy.
- Cập nhật tài liệu khi quyết định/hành vi thay đổi; phân biệt cấu hình hiện có, quyết định đã duyệt và đề xuất.

## Kiểm tra và báo cáo

- Frontend: dùng `npm ci` để cài từ lockfile; chạy `npm run lint` và `npm run build` trong `frontend/` khi đổi code/config liên quan.
- Backend: chạy `./mvnw clean verify` (Linux/macOS) hoặc `.\mvnw.cmd clean verify` (PowerShell) trong `backend/`. Chạy `docker compose up -d --wait postgres` từ root trước. Spring Boot import .env dạng Java properties khi working directory là root hoặc backend/; biến môi trường thực có độ ưu tiên cao hơn. Dùng KEY=value không bọc dấu nháy. Entry point chuẩn hóa alias Asia/Saigon thành Asia/Ho_Chi_Minh trước khi khởi tạo datasource, không cần JAVA_TOOL_OPTIONS. Test kiểm tra health qua HTTP, kết nối JPA và Flyway history; chưa có test nghiệp vụ.
- Thay đổi tài liệu: kiểm tra link nội bộ, tính nhất quán và `git diff --check`; không cần build lại nếu không đổi code/config ứng dụng.
- Báo rõ thay đổi, kiểm tra đã chạy và lỗi/chặn còn tồn tại. Không báo pass nếu chưa chạy được.
- `.editorconfig` quy định định dạng cơ bản, chưa thay thế formatter/linter. Không format hàng loạt ngoài phạm vi.

## Git và thông tin bí mật

- GitHub Flow: branch theo mục tiêu → PR vào `main` → review → Merge Commit. Không push trực tiếp vào `main` hoặc bỏ qua bảo vệ nhánh.
- Không tự commit, push hoặc merge nếu yêu cầu chưa bao gồm các thao tác đó.
- Không commit `.env`, credential, API key hoặc payload nhạy cảm. `.env.example` chỉ chứa giá trị mẫu an toàn.
