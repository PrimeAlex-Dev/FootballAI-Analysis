# Standing — API và quản lý

Một Standing hiện hành cho mỗi CompetitionSeason. Migration V7 tạo standings (id, competition_season_id duy nhất, created_at, updated_at) và standing_rows (standing_id, table_index, row_index, team_id FK, rank, matches_played, wins, draws, losses, goals_for, goals_against, goal_difference, points). Không lưu JSONB hoặc tên đội lặp trong bảng. FK Team/CompetitionSeason RESTRICT; xóa Standing xóa các dòng của riêng nó. Số liệu được giữ đúng theo JSON nguồn, không tính lại điểm hoặc hiệu số.

## API

- GET `/api/standings?competitionSeasonId=&page=0&size=20`: danh sách phân trang.
- GET `/api/standings/{id}`: chi tiết.
- GET `/api/competition-seasons/{id}/standing`: bảng theo giải/mùa, Customer sử dụng.
- POST `/api/competition-seasons/{id}/standing/preview`: kiểm tra JSON và đối chiếu Team, không ghi.
- POST `/api/competition-seasons/{id}/standing`: tạo, 409 nếu đã có.
- PUT `/api/competition-seasons/{id}/standing`: thay toàn bộ trong transaction.
- DELETE `/api/competition-seasons/{id}/standing`: xóa bảng.

Request chứa `tables` đúng cấu trúc JSON Extension. Nếu tên đội không khớp sau trim và so sánh không phân biệt hoa thường, Admin chọn Team trong preview; request lưu kèm `mappings: [{tableIndex,rowIndex,teamId}]`. Backend kiểm tra lại mọi ánh xạ và Team ID trước khi lưu. Không tự tạo Team hoặc lưu alias. Preview trả `teamId: null` cho dòng chưa khớp. JSON sai cấu trúc, field lạ/sai kiểu, đội trùng trong một bảng, hoặc ánh xạ chưa đủ bị từ chối 400. Hạng được phép trùng, thứ tự dòng dùng row_index. `points` và `goalDifference` có thể âm.

Admin tại `/dashboard/standings`: chọn giải/mùa, dán JSON, preview/đối chiếu, lưu, xem, cập nhật và xóa. Customer tab Standing gọi API theo CompetitionSeason; thiếu bảng hiện trạng thái chưa có dữ liệu.
