# Match Analysis V1

Admin nhập thủ công một bản nhận định hiện hành cho mỗi Match. Không gọi AI API, không lưu phiên bản cũ. Bảng `match_analyses` được tạo bởi V9, gồm `id`, `match_id` (unique, FK `ON DELETE CASCADE`), `analysis_content` (`TEXT`), `prediction_content` (`TEXT`), `status` (`DRAFT`/`PUBLISHED`). Xóa nhận định không xóa Match; xóa Match xóa nhận định phụ thuộc. Ẩn Match không xóa nhận định.

| Method | Endpoint | Hành vi |
| --- | --- | --- |
| GET | `/api/matches/{matchId}/analysis` | Đọc cả bản nháp |
| POST | `/api/matches/{matchId}/analysis` | Tạo, trùng trả 409 |
| PUT | `/api/matches/{matchId}/analysis` | Thay nội dung và trạng thái |
| DELETE | `/api/matches/{matchId}/analysis` | Xóa bản nhận định, trả 204 |
| GET | `/api/public/matches/{matchId}/analysis` | Chỉ đọc bản `PUBLISHED` của Match không bị ẩn |

Request POST/PUT gồm `analysisContent`, `predictionContent`, `status`. Response gồm các trường này và `matchId`. Bản nháp được để trống nội dung; bản xuất bản bắt buộc cả hai nội dung có từ. Giới hạn 20.000 từ cho phân tích, 2.000 từ cho dự đoán. Từ là chuỗi ký tự liên tiếp không phải khoảng trắng Unicode; nhiều dấu cách, tab và xuống dòng là dấu phân tách. Backend kiểm tra giới hạn, frontend hiển thị cùng cách đếm. Văn bản giữ xuống dòng, được render như text thuần.

Match hoặc bản nhận định không tồn tại: 404; dữ liệu sai: 400; tạo trùng: 409. Bản nháp hoặc Match ẩn không xuất hiện ở API công khai. Các endpoint `/api/matches` hiện chưa có xác thực/phân quyền, nên chức năng này chưa bảo đảm chỉ Admin được ghi dữ liệu; cần bổ sung xác thực ở một hạng mục riêng.
