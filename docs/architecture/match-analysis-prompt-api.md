# Export Match Analysis Prompt — V1

Admin gọi `GET /api/matches/{id}/analysis-prompt`. ID phải dương; trận không tồn tại trả 404. Mỗi lần gọi đọc lại database, không ghi prompt, không gọi AI và không cần migration. Response gồm `prompt` (văn bản UTF-8), `homeRecentCount`, `awayRecentCount`, `homeMatchesWithStatistics`, `awayMatchesWithStatistics`.

Trận mục tiêu lấy qua API Match quản trị nên được phép đang ẩn. Phong độ mỗi đội lấy tối đa 5 trận `FINISHED` gần nhất với `kickoffTime` nhỏ hơn trận mục tiêu, trên mọi giải đấu và cả sân nhà/khách; sắp xếp `kickoffTime DESC, id DESC`. H2H tối đa 5 trận theo cùng mốc và thứ tự. Export Admin bao gồm cả trận lịch sử đang ẩn; việc ẩn chỉ giới hạn Customer UI. Trận thiếu Stats vẫn nằm trong 5 trận, không thay bằng trận cũ hơn.

Stats lấy theo tập Match ID từ hai danh sách phong độ, một bản ghi cho mỗi trận. Prompt in hai phía home/away đúng theo trận lịch sử; tối đa 16 chỉ số mỗi phía như [Match Statistics](match-statistics-api.md), không có fouls. Chỉ số null bị bỏ qua, 0 được in ra. Nếu không có bản ghi Stats, ghi rõ trận thiếu. W/D/L và tổng bàn thắng/bàn thua tính theo đội đang xét. Giờ hiển thị theo `Asia/Ho_Chi_Minh`. Không xuất ID database hoặc JSON thô.

Frontend Admin có nút Export Prompt trên mỗi dòng Match. Dialog hiển thị prompt, số trận và số trận có Stats của từng đội; Copy Prompt dùng Clipboard API. Đóng rồi mở lại sẽ gọi API để đọc dữ liệu hiện tại. Dự án hiện chưa có xác thực hoặc phân quyền Admin.
