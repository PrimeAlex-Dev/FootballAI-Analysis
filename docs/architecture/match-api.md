# Match — API và giao diện quản trị

Trạng thái: triển khai theo thiết kế Match hiện tại; không tự đánh dấu Reference Module được duyệt. Tuân thủ [Coding Standards](../standards/AI_Football_Analysis_Sprint_1_Coding_Standards.md).

## Phạm vi dữ liệu

Entity có đúng 9 field: id, competitionSeasonId, homeTeamId, awayTeamId, kickoffTime, status, homeScore, awayScore, round. Ba ID tham chiếu được lưu dạng scalar; FK nằm ở database, không lưu trùng tên hoặc logo. Không thêm Standing/Squad/Player/thống kê/AI, WDL, bảng RecentForm/H2H hoặc quy tắc nhận diện trùng trận chưa được chốt.

CompetitionSeason và hai Team phải tồn tại; hai đội khác nhau. Tạo/sửa kiểm tra qua service công khai của module liên quan. Database dùng FK ON DELETE RESTRICT nên không thể xóa Team/CompetitionSeason đang được Match sử dụng; API hiện có trả 409. Không có cascade xóa lịch sử.

## API

Dùng tiền tố `/api` theo convention hiện tại, không thêm `/api/v1` riêng cho Match.

| Method | Path | Kết quả |
|---|---|---|
| POST | `/api/matches` | 201 MatchResponse |
| GET | `/api/matches/{id}` | 200 MatchResponse |
| GET | `/api/matches` | 200 content + page |
| PUT | `/api/matches/{id}` | 200 MatchResponse |
| DELETE | `/api/matches/{id}` | 204 |

Request tạo/sửa:

```json
{
  "competitionSeasonId": 1,
  "homeTeamId": 1,
  "awayTeamId": 2,
  "kickoffTime": "2030-06-15T20:00:00+07:00",
  "status": "SCHEDULED",
  "homeScore": null,
  "awayScore": null,
  "round": "Round 1"
}
```

POST thiếu/null status mặc định SCHEDULED. PUT phải gửi status; các ID và kickoffTime bắt buộc ở cả hai phương thức. Round rỗng chuẩn hóa null, còn lại trim. Score dùng Integer, chỉ nhận JSON integer hoặc null; không ép số thập phân/chuỗi thành tỷ số.

Response trả 9 field trên và `competitionSeasonName`, `homeTeamName`, `homeTeamLogo`, `awayTeamName`, `awayTeamLogo`. Tên/ảnh được join theo ID tại thời điểm truy vấn, không lưu trong matches. Repository dùng DTO projection và phân trang database, không expose entity hoặc truy vấn thêm từng hàng.

GET danh sách hỗ trợ kết hợp:

- `name`: tìm chứa tên đội nhà hoặc đội khách, trim và không phân biệt hoa/thường; %, _ và backslash là ký tự literal.
- `competitionSeasonId`, `homeTeamId`, `awayTeamId`: lọc chính xác theo ID dương.
- `teamId`: lọc đội bất kể sân nhà/khách.
- `status`: SCHEDULED, LIVE, FINISHED, POSTPONED, CANCELLED.
- `from`, `to`: ISO Instant có Z/offset; frontend gửi giờ Việt Nam với +07:00, bao gồm hai đầu mốc. From không được lớn hơn to.
- `direction`: DESC mặc định hoặc ASC. Sorting kickoffTime rồi id cùng chiều, đảm bảo thứ tự ổn định.
- `page`: từ 0; `size`: mặc định 20, tối đa 100.

Lỗi theo ProblemDetail hiện có: 400 validation, 404 ID tham chiếu/bản ghi không tồn tại, 409 ràng buộc dữ liệu. Không đặt quy tắc hạn chế các bước chuyển trạng thái ngoài yêu cầu về thời gian/tỷ số đã chốt.

## Trạng thái và tỷ số

| Status | Tỷ số |
|---|---|
| SCHEDULED, POSTPONED, CANCELLED | Backend chuẩn hóa cả hai thành null |
| LIVE | Cả hai null hoặc cả hai nguyên không âm |
| FINISHED | Bắt buộc cả hai nguyên không âm, cho phép 0:0 |

Không chấp nhận chỉ một bên có tỷ số. Khi lưu SCHEDULED đã đến giờ, backend chuyển LIVE ngay nhưng vẫn giữ tỷ số null theo trạng thái SCHEDULED ban đầu. Không suy luận thắng/hòa/thua và không tự gán 0:0.

## Tự chuyển LIVE và tính đồng thời

Job Spring Scheduling chạy sau mỗi 15 giây, cấu hình bằng `match.live-update-delay-ms`; lần đầu cũng chờ chu kỳ này. Clock UTC dùng chung trong Match. Job chạy UPDATE có điều kiện `status = SCHEDULED AND kickoff_time <= now`, chỉ đổi status và không tải toàn bộ dữ liệu. Chạy lặp an toàn; không tự chuyển LIVE sang FINISHED hoặc thay đổi trạng thái khác. Log số trận đã chuyển khi có cập nhật.

Create/update xử lý trận SCHEDULED đã đến giờ ngay trong transaction. Update/delete khóa hàng bằng PESSIMISTIC_WRITE; job UPDATE theo điều kiện tại database để dựa trên kickoff/status mới nhất khi cạnh tranh cập nhật. Job có thể trễ tới một chu kỳ cộng thời gian thực thi; backend là nguồn chuẩn cho UI.

## Migration và lịch sử

V5 tạo matches với PK, FK RESTRICT, CHECK hai đội khác nhau, status hợp lệ, score không âm/đủ cặp và phù hợp status. Không sửa V1–V4.

Index: competitionSeason + kickoff + id; homeTeam + status + kickoff + id; awayTeam + status + kickoff + id; kickoff + id; index riêng SCHEDULED theo kickoff cho job. Các index theo hai đội hỗ trợ truy vấn lịch sử FINISHED theo sân nhà/khách và thời gian; chưa triển khai endpoint Recent Form/H2H.

## Frontend

`/dashboard/matches`, có trong sidebar và trang tổng quan. Dùng layout, button, field, dialog, bảng, phân trang, EntityLogo và xử lý lỗi hiện có. Tìm tên đội, lọc giải theo mùa/đội/trạng thái/khoảng giờ Việt Nam, sorting và phân trang qua backend; điều kiện giữ trong URL. Danh sách tải lại mỗi 15 giây khi tab hiển thị, không tự suy luận LIVE bằng đồng hồ trình duyệt.

Bộ chọn CompetitionSeason tải trang 50 bản ghi và tải thêm; bộ chọn Team hỗ trợ tìm tên qua API, tải thêm và giữ nhãn đội đang chọn. Không lọc trên một trang thay cho backend.

Datetime-local luôn nhập/hiển thị giờ Việt Nam (Asia/Ho_Chi_Minh), bất kể múi giờ máy. Frontend gửi kickoffTime và from/to với offset +07:00; response kickoffTime cũng biểu diễn +07:00. Bảng và chi tiết chỉ hiển thị giờ Việt Nam. Persistence vẫn dùng Instant/timestamptz để giữ nguyên thời điểm, không cộng 7 giờ vào dữ liệu cũ và không đổi schema. Job so sánh cùng thời điểm tuyệt đối nên không đổi logic. LIVE cho phép để trống cả hai ô tỷ số; FINISHED yêu cầu đủ cặp. Chuyển từ trận đã lưu tỷ số sang trạng thái không có tỷ số phải xác nhận. Hiển thị - : - khi chưa có tỷ số, giữ đúng 0 : 0 khi có kết quả hòa không bàn thắng.

## Kiểm tra và giới hạn

- Backend: clean verify trên football_ai_test, gồm test Match CRUD, default SCHEDULED, lịch sử FINISHED, 0:0, LIVE null, validation/404, FK/409 và CHECK, search/filter/time range/pagination/sorting, tự chuyển LIVE tại mốc, đổi kickoff, job lặp và giữ các trạng thái khác.
- Frontend: npm ci, lint, TypeScript/build và format check. Chưa có test runner frontend; không thêm framework. Kiểm tra helper hiển thị null/0 và chuyển giờ Việt Nam sang ISO +07:00 và giữ đúng thời điểm bằng Node assert.
- Integration qua Vite tới backend và PostgreSQL test: CRUD, DTO join, filter/search/pagination, 0:0, xóa tỷ số và job scheduled thực tế (chu kỳ test 1 giây).
- Computer Use dừng do phím Escape vật lý trong lúc mở URL kiểm tra. Chưa kiểm tra trực tiếp danh sách/form/picker, loading/error, responsive và dark/light cho Match trên trình duyệt. Không coi API smoke/build là E2E giao diện.

Rà soát trước commit: 18 test backend đạt; lint, TypeScript/build, format check và Docker build backend/frontend đạt. Smoke test dùng API client frontend thật qua Vite và PostgreSQL test kiểm tra CRUD, lỗi 400/404, tìm kiếm/lọc theo giờ Việt Nam, hai trang dữ liệu/sorting, truy vấn picker/reset, null/0 và job LIVE thực tế. Đã sửa việc URL đổi làm dựng lại toàn thanh lọc và mất nội dung chưa gửi; chỉ dựng lại toàn bộ khi bấm Đặt lại. Thử E2E trình duyệt lần này bị Computer Use chặn vì không xác minh được URL Chrome; chưa xác nhận trực tiếp form/responsive/theme trên trình duyệt. npm ci local bị Windows khóa file native Rolldown, đã khôi phục dependency bằng npm install; Docker build frontend dùng npm ci thành công từ lockfile.
