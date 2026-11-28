# Playing Style Radar V1

Customer gọi `GET /api/public/matches/{id}/playing-style-radar`. Match không tồn tại hoặc đang ẩn trả 404. Chỉ trận `SCHEDULED` có `kickoffTime` trong tương lai được tính Radar; các trận khác trả `NOT_APPLICABLE` và không truy vấn lịch sử. API chỉ đọc, không gọi AI, không lưu điểm và không cần migration.

Với mỗi đội, lấy tối đa 5 Match `FINISHED` gần nhất trước kickoff trận mục tiêu, trên mọi giải đấu và cả sân nhà/khách; loại Match đã ẩn. Sắp xếp `kickoffTime DESC, id DESC`. Trận thiếu Match Statistics vẫn giữ vị trí trong 5 trận. Statistics lấy theo tập Match ID, đối chiếu home/away của từng trận để xác định chỉ số đội đang xét và đối thủ.

Sáu trục có một chỉ số chính tính điểm: Ball Control = possession; Attacking Progression = passesFinalThird; Chance Creation = xg; Shooting Activity = shotsOnTarget; Box Penetration = touchesInBox; Chance Suppression = xg đối thủ, đảo chiều. Các chỉ số bổ trợ trong response: passAccuracy, bigChances, keyPasses, shots, shots đối thủ. Không cộng trực tiếp các đơn vị hoặc tự tính trung bình từ giá trị null; 0 là dữ liệu hợp lệ.

Mỗi chỉ số trả trung bình của các trận có giá trị và `availableMatches`. Tham chiếu gồm hồ sơ tối đa 5 trận gần nhất của các đội trước cùng kickoff, theo cùng quy tắc lọc và tính trung bình. Mỗi hồ sơ tham chiếu phải có ít nhất 2 trận có chỉ số; mỗi trục cần ít nhất 5 hồ sơ tham chiếu. Ngưỡng này là mức tối thiểu thử nghiệm để tránh dùng một trận đơn lẻ hoặc so sánh chỉ với chính hai đội. Khi thiếu mẫu, điểm là null, không phải 0.

Điểm là phân vị giữa các hồ sơ tham chiếu: `100 × (số hồ sơ thấp hơn + 0,5 × số hồ sơ bằng) / số hồ sơ hợp lệ`. Chance Suppression lấy `100 − phân vị xG đối thủ`. Hai đội dùng cùng tập tham chiếu và công thức. Điểm chỉ mô tả vị trí tương đối trong dữ liệu hiện có, không phải xếp hạng sức mạnh, dự đoán kết quả hay điều chỉnh theo sức mạnh đối thủ.

Response gồm `status` (`READY`, `PARTIAL`, `INSUFFICIENT_REFERENCE_DATA`, `NOT_APPLICABLE`), `sampleLimit`, `referenceMethod`, hai đội `home`/`away`, mỗi đội có `historyMatches` và sáu `axes`. Mỗi trục gồm `key`, `label`, `score` nullable, `primary`, danh sách `supporting`, `referenceProfiles`. Chỉ `READY` được vẽ đa giác đủ sáu trục; trường hợp khác hiển thị số liệu gốc và lý do thiếu dữ liệu.

Nguồn test local chỉ có dữ liệu khởi tạo Match, không import Match Statistics; chưa thể đánh giá độ phủ Stats trên dữ liệu vận hành. Ngưỡng và phân vị cần được xem lại khi có bộ dữ liệu đủ lớn và đại diện.
