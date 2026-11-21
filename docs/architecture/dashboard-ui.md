# Dashboard quản trị dữ liệu

Trạng thái: frontend tích hợp API hiện có, chưa tự đánh dấu Reference Module chính thức.

## Đường dẫn

- `/dashboard`: lối vào ba danh mục.
- `/dashboard/competitions`: danh sách, chi tiết, tạo/sửa/xóa, tìm tên, lọc loại, phân trang.
- `/dashboard/seasons`: danh sách, chi tiết, tạo/sửa/xóa, tìm theo tên, phân trang.
- `/dashboard/competition-seasons`: danh sách, chi tiết, tạo/xóa liên kết, lọc theo giải/mùa, phân trang.
- `/template-ui`: mẫu thiết kế độc lập, giữ dữ liệu minh họa.

## Quy ước triển khai

Dùng React Router, CSS variables và Lucide. Tokens màu dùng chung trong `frontend/src/shared/tokens.css`; dashboard tái sử dụng phong cách template và bổ sung CSS quản trị. Font Inter/Segoe UI/Arial theo stack hệ thống, chưa đóng gói Inter. Theme lưu trong localStorage nếu trình duyệt cho phép.

Mỗi module sở hữu types, API client, form và trang danh sách. Shared có button, field, dialog native, bảng, phân trang, xử lý lỗi, hook danh sách và bộ chọn danh mục. Dialog native giữ focus trong hộp thoại, Escape yêu cầu xác nhận khi có dữ liệu chưa lưu; đóng dialog trả focus. Tất cả form dùng cùng layout và hành động.

API dùng `/api`, nhận `content/page` và `ProblemDetail` theo [contract backend](competition-season-api.md). Filter/page/size lưu trong URL, API trang từ 0, UI từ 1. Đổi filter/size về trang đầu; trang vượt giới hạn được điều chỉnh. Request danh sách cũ bị hủy; thao tác lưu khóa nút gửi. Bản ghi lỗi giữ nguyên form. Lỗi dữ liệu đang dùng/trùng không được bỏ qua.

Bộ chọn giải/mùa tải theo lô 100 và có nút tải thêm, không coi lô đầu là toàn bộ danh mục. Logo hiển thị nếu là HTTP(S) hoặc đường dẫn tương đối bắt đầu bằng `/`; khi không có hoặc tải lỗi dùng chữ viết tắt, giá trị lưu vẫn theo API.

## Chức năng chưa có ở backend

- Season: các bộ lọc khác ngoài tìm theo tên.
- CompetitionSeason: sửa liên kết và tìm kiếm tự do.
- Tất cả danh sách: tùy chọn sorting từ client.
- Logo: upload file.

Frontend không mô phỏng các chức năng này bằng lọc trên một trang dữ liệu hoặc ghi endpoint chưa có.

## Kiểm tra

Kiểm tra trình duyệt với backend riêng cổng 18080, datasource `football_ai_test`: tạo/sửa giải đấu, tìm tên, chi tiết, tạo/sửa mùa giải, tạo liên kết, lỗi trùng liên kết, chặn xóa mùa đang dùng, xóa theo thứ tự liên kết → mùa → giải; đổi kích thước và chuyển trang. Đã quan sát giao diện sáng/tối và mobile 390px. Các bản ghi thử được dọn trong database test.

Không thay đổi backend. Chưa kiểm tra tự động toàn bộ tổ hợp lỗi, bộ chọn trên 100 bản ghi hoặc mọi thiết bị; chưa chạy Docker build cho thay đổi frontend này.

Tìm kiếm mùa giải dùng tham số name ở backend, giữ từ khóa trong URL và về trang đầu khi tìm/đặt lại. Không tải toàn bộ mùa giải để lọc tại frontend.


Module Đội bóng: /dashboard/teams; CRUD, chi tiết, tìm tên, lọc loại và phân trang server. Contract và kết quả kiểm tra tại [Team API](team-api.md). Logo dùng EntityLogo chung với Competition.

Module Match tại /dashboard/matches: CRUD, tìm tên đội, lọc giải theo mùa/đội/trạng thái/thời gian, sorting và phân trang backend. Contract tại [Match API](match-api.md).
