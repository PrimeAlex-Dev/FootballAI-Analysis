# Match Statistics — Admin

Trạng thái: triển khai theo đặc tả 16 chỉ số mỗi đội. Dữ liệu nguồn Flashscore chỉ dùng làm JSON đầu vào; không lưu mã nguồn, URL hoặc thời gian thu thập. Không có `fouls`.

## Lưu trữ

Migration V6 tạo `match_statistics`: `id`, `match_id` duy nhất/FK RESTRICT, `created_at`, `updated_at`, và 16 cột `home_*` cùng 16 cột `away_*`. Các chỉ số: bigChances, clearances, corners, crosses, interceptions, keyPasses, longPasses, passAccuracy, passesFinalThird, possession, redCards, shots, shotsOnTarget, touchesInBox, xg, yellowCards. Các cột nullable; 0 khác null. Chỉ số đếm không âm, phần trăm nguyên 0–100, xg decimal không âm tối đa 2 chữ số thập phân.

## API

- `GET /api/matches/{id}/statistics`: dữ liệu đã lưu; 404 nếu thiếu.
- `POST /api/matches/{id}/statistics/preview`: kiểm tra không ghi dữ liệu.
- `POST /api/matches/{id}/statistics`: tạo lần đầu; 409 nếu đã có.
- `PUT /api/matches/{id}/statistics`: thay thế toàn bộ; 404 nếu chưa có.

Request JSON gồm đúng hai object `home` và `away`; mỗi object nhận tối đa 16 key đã liệt kê. Key thiếu/null lưu null; key lạ, sai kiểu, số âm hay phần trăm ngoài 0–100 trả 400. Response trả `matchId`, `home`, `away`, `createdAt`, `updatedAt`; preview có hai thời gian null. MatchResponse thêm `hasStatistics` được tính từ database, không thêm cột vào matches. Xóa Match có Stats bị FK chặn.

## Giao diện

Trong bảng Match có cột Đã lưu/Chưa có và nút Thống kê ở từng dòng. Dialog cho dán JSON, preview hai đội, lưu lần đầu hoặc xác nhận thay thế toàn bộ khi cập nhật. Tên đội lấy từ Match; JSON không có tên/mã trận để backend tự kiểm tra nó thuộc đúng trận nào. Admin đối chiếu ở preview.

Customer UI Statistics chưa tích hợp API này trong phạm vi công việc.
