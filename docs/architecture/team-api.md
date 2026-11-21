# Team API và giao diện quản trị

Trạng thái: triển khai theo thiết kế Team 4 field được yêu cầu. Chưa tự đánh dấu Reference Module được duyệt.

## Dữ liệu và quy tắc

Team là danh mục độc lập gồm đúng `id` (Long tự sinh), `name` (bắt buộc), `logo` (nullable), `type` (`NATIONAL` hoặc `CLUB`, bắt buộc). Không thêm quan hệ giải/mùa, ranking, squad, players hoặc cấp độ đội.

Tên và logo được trim theo convention Competition; tên không được trắng. Tên duy nhất toàn danh mục, không phụ thuộc type; trim đầu/cuối và không phân biệt hoa/thường. Tạo/sửa trùng tên trả 409. V4 thêm unique index lower(name); dữ liệu cũ trùng phải được giải quyết trước, không tự gộp/xóa. ID là định danh. Logo nhập chuỗi URL/đường dẫn, chưa có upload file. Flyway V3 tạo bảng teams với PK, NOT NULL và CHECK tên/type; không sửa V1/V2.

## Contract

| Method | Endpoint | Kết quả |
|---|---|---|
| POST | `/api/teams` | 201, TeamResponse |
| GET | `/api/teams/{id}` | 200, TeamResponse |
| GET | `/api/teams` | 200, `content` và `page` |
| PUT | `/api/teams/{id}` | 200, TeamResponse |
| DELETE | `/api/teams/{id}` | 204 |

Request tạo/sửa: `{"name":"Arsenal","type":"CLUB","logo":null}`. Response có thêm `id`, không expose entity.

Ví dụ: `/api/teams?name=arsenal&type=CLUB&page=0&size=10`. Dùng `name` theo convention Competition/Season, không dùng `search`. Tìm chứa chuỗi không phân biệt hoa/thường; trim từ khóa, từ khóa rỗng bỏ lọc. `%`, `_`, `\` là ký tự literal. Search/type kết hợp trước khi phân trang tại database. Sorting cố định tên tăng dần rồi ID tăng dần. Page từ 0, size mặc định 20, tối đa 100.

Lỗi theo ProblemDetail hiện có: 400 input/enum/ID/page/size không hợp lệ; 404 không tìm thấy; 409 khi xóa bị ràng buộc tham chiếu. Chưa có Match/Standing tham chiếu thật; các module tương lai phải tạo FK không cascade xóa Team. Kiểm thử 409 dùng bảng tham chiếu tạm trong database test và dọn sau test, không thêm bảng nghiệp vụ giả vào migration.

## Frontend

`/dashboard/teams`, truy cập từ sidebar và trang tổng quan. Tái sử dụng layout, field, button, modal, table, pagination, detail/delete dialog, thông báo kết quả và xử lý lỗi chung. Component logo được chuyển thành EntityLogo dùng chung với Competition, giữ hành vi cũ.

Danh sách hiển thị logo/tên/loại và thao tác chi tiết/sửa/xóa. Search/filter/page/size qua API thực, giữ trong URL; đổi điều kiện về trang đầu. Form chỉ gửi name/type/logo. Lưu lỗi giữ form; thành công refresh. Logo lỗi hoặc không có dùng chữ viết tắt. Theme và responsive theo dashboard hiện có.

## Xác minh

- Backend `clean verify`: 10 test đạt, trong đó 4 test Team bao phủ CRUD, 400/404/409, tìm tên, filter NATIONAL/CLUB, kết hợp phân trang, sorting ổn định, rỗng, wildcard literal và ràng buộc database. Chỉ dùng football_ai_test.
- Frontend: npm ci, lint, build (bao gồm TypeScript), format check đạt. Repository chưa cấu hình frontend test runner; không thêm framework.
- Smoke integration qua Vite `/api/teams` tới backend thật và PostgreSQL test: create/get/update/delete, search/filter/pagination và kết quả rỗng đạt.
- Kiểm tra UI bằng Chrome bị công cụ Computer Use chặn vì không xác minh được URL hiện tại. Chưa xác nhận thao tác form/bảng, loading/error, dark/light và responsive trên trình duyệt cho Team. Không coi build hoặc API smoke là thay thế E2E UI.
