# Competition và Season API

**Trạng thái:** Triển khai đầu tiên, chờ review để quyết định Reference Module. Tài liệu ý tưởng Sprint 1 không thay thế contract này.

## Đường dẫn và phản hồi

Backend sở hữu tiền tố `/api`; Vite và Nginx chuyển tiếp nguyên đường dẫn. Chưa có quy ước versioning trong repository, vì vậy các endpoint hiện chưa mang version. `GET` chi tiết và `PUT` trả DTO; `POST` trả DTO với HTTP 201; `DELETE` trả 204. Lỗi dùng Spring `ProblemDetail` với HTTP 400, 404 hoặc 409. Danh sách dùng Spring Data `PagedModel`: `content` và `page` (`size`, `number`, `totalElements`, `totalPages`). Trang bắt đầu từ 0, kích thước mặc định 20 và tối đa 100.

Swagger UI chạy trực tiếp trên backend tại `http://localhost:8080/swagger-ui.html`; OpenAPI JSON tại `http://localhost:8080/v3/api-docs`. Có thể dùng **Try it out** để gửi request tới backend. Khi đổi `BACKEND_PORT` của Docker Compose, dùng cổng host tương ứng. OpenAPI chỉ liệt kê các đường dẫn `/api/**`; endpoint sức khỏe `/actuator/health` không thuộc tài liệu nghiệp vụ.

| Method | Path | Chức năng |
|---|---|---|
| POST | `/api/competitions` | Tạo giải đấu |
| GET | `/api/competitions/{id}` | Chi tiết giải đấu |
| GET | `/api/competitions?name=&type=&page=&size=` | Tìm theo tên, lọc loại, phân trang; sắp `name ASC, id ASC` |
| PUT | `/api/competitions/{id}` | Thay `name`, `type`, `logo` |
| DELETE | `/api/competitions/{id}` | Xóa nếu chưa có Season |
| POST | `/api/competitions/{competitionId}/seasons` | Tạo mùa giải |
| GET | `/api/competitions/{competitionId}/seasons/{id}` | Chi tiết mùa giải |
| GET | `/api/competitions/{competitionId}/seasons?page=&size=` | Danh sách mùa giải; sắp `name DESC, id DESC` |
| PUT | `/api/competitions/{competitionId}/seasons/{id}` | Đổi tên mùa giải |
| DELETE | `/api/competitions/{competitionId}/seasons/{id}` | Xóa nếu không bị dữ liệu khác tham chiếu |

Competition request: `{ "name": "Premier League", "type": "LEAGUE", "logo": null }`; response có thêm `id`. `type` chỉ nhận `LEAGUE` hoặc `CUP`. Season request: `{ "name": "2026/2027" }`; response gồm `id`, `competitionId`, `name`. Competition ID lấy từ path; không nhận thay đổi quan hệ trong request. Tên và logo được cắt khoảng trắng đầu/cuối trước khi lưu. Season name chỉ nhận `YYYY` hoặc hai năm liên tiếp `YYYY/YYYY`; sắp xếp theo chuỗi giảm dần, phù hợp các năm bốn chữ số.

Flyway V1 tạo `competitions`, `seasons`, khóa ngoại không cascade và ràng buộc duy nhất `(competition_id, name)`. Unique constraint cung cấp index với cột đầu là `competition_id`, dùng cho truy vấn danh sách theo giải đấu. PostgreSQL từ chối tên rỗng, loại giải không hợp lệ, mùa giải sai cấu trúc, trùng tên trong cùng giải và quan hệ mồ côi. Quy tắc hai năm liên tiếp được kiểm tra ở service. Không có bảng cho các module tương lai trong migration này.
