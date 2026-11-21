# Competition, Season và CompetitionSeason API

**Trạng thái:** Thiết kế ba module theo yêu cầu hiện tại; Competition và Season là danh mục độc lập.

Backend dùng tiền tố `/api` theo proxy hiện có. Swagger UI: `http://localhost:8080/swagger-ui.html`; OpenAPI: `http://localhost:8080/v3/api-docs`. Chưa dùng `/api/v1` vì repository chưa chốt versioning. Danh sách trả `content` và `page`, trang từ 0, kích thước mặc định 20, tối đa 100. Lỗi trả Spring `ProblemDetail`.

| Method | Path | Chức năng |
|---|---|---|
| POST, GET | `/api/competitions` | Tạo; danh sách tìm theo `name`, lọc `type`, phân trang |
| GET, PUT, DELETE | `/api/competitions/{id}` | Chi tiết; cập nhật; xóa khi chưa được sử dụng |
| POST, GET | `/api/seasons` | Tạo; danh sách tìm theo `name`, phân trang |
| GET, PUT, DELETE | `/api/seasons/{id}` | Chi tiết; đổi tên hoặc xóa khi chưa được sử dụng |
| POST, GET | `/api/competition-seasons` | Tạo từ `competitionId` và `seasonId`; danh sách lọc bằng hai ID, phân trang |
| GET, DELETE | `/api/competition-seasons/{id}` | Chi tiết; xóa khi không có dữ liệu phụ thuộc |

Competition có `id`, `name`, `type` (`LEAGUE` hoặc `CUP`), `logo`. Season có `id`, `name` duy nhất toàn hệ thống; tên hợp lệ là `YYYY` hoặc hai năm liên tiếp `YYYY/YYYY`. CompetitionSeason có `id`, `competitionId`, `seasonId`; response ghép thêm `displayName` từ tên giải và tên mùa, không lưu cột này. Liên kết không được cập nhật sau khi tạo.

Flyway V2 giữ V1 nguyên vẹn, chuyển từng liên kết Competition–Season cũ sang `competition_seasons`, hợp nhất các Season trùng tên theo ID nhỏ nhất, rồi đặt unique toàn cục trên `seasons.name`. FK không cascade; unique `(competition_id, season_id)` và index `season_id` phục vụ truy vấn. Initial Data tạo bốn Competition mẫu, dùng lại Season cùng tên và chỉ thêm liên kết thiếu khi backend khởi động.
Tìm mùa giải: GET /api/seasons?name=2026&page=0&size=20. Tên được trim, tìm chứa chuỗi trên toàn bộ danh mục trước khi phân trang; bỏ name hoặc chỉ nhập khoảng trắng trả danh sách không lọc. Các ký tự %, _ và dấu gạch chéo ngược được tìm theo nghĩa literal. Giữ sắp xếp tên giảm dần, rồi ID giảm dần.
