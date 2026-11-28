# Customer UI

Tab Nhận định AI của trận thật gọi [Match Analysis API](match-analysis-api.md), chỉ hiển thị bản `PUBLISHED` dưới dạng văn bản thuần giữ xuống dòng. Bản nháp hoặc chưa có nhận định hiển thị trạng thái thiếu dữ liệu.

Customer có thể bấm “Đọc phân tích” để nghe `analysisContent` bằng Web Speech API của trình duyệt, với giọng tiếng Việt khi thiết bị cung cấp. Có phát, tạm dừng, tiếp tục, dừng và tốc độ 1×/1.25×/1.5×. Giọng đọc dừng khi rời tab AI hoặc đổi trận; tính năng không tự phát và không tạo file âm thanh.

Tab Thống kê của trận sắp diễn ra dùng [Playing Style Radar V1](playing-style-radar-api.md) từ API thật. Trận trạng thái khác giữ giao diện thống kê hiện có. Radar chỉ vẽ khi cả hai đội có đủ sáu điểm; thiếu mẫu hiển thị chỉ số gốc, không gán 0.

Customer đọc Match và Standing qua `/api/public/matches` và `/api/public/competition-seasons/{id}/standing`. Match ẩn không xuất hiện trong danh sách, chi tiết, Recent Form hoặc H2H; Standing ẩn hiển thị “Chưa có dữ liệu”. API quản trị `/api/matches` và `/api/standings` vẫn đọc được dữ liệu ẩn.

Tab Bảng xếp hạng gọi Standing API theo CompetitionSeason của Match; hiển thị các bảng theo thứ tự nguồn, tô sáng hai đội trong trận và hiện trạng thái chưa có dữ liệu khi thiếu Standing. Xem [Standing API](standing-api.md).

Customer UI tại `/matches`; chi tiết tại `/matches/:matchId` với bốn route con `ai`, `form`, `statistics`, `standings`. Route chi tiết mặc định chuyển đến `ai`. Admin Dashboard vẫn ở `/dashboard`.

Frontend dùng React Router, Lucide và CSS variables tương thích màu nhận diện `#99FF33`. Customer có theme riêng sáng/tối lưu trong trình duyệt, không thay đổi theme Admin. Bố cục responsive, tab và thanh lọc có thể cuộn ngang trong vùng của chúng ở mobile.

Dữ liệu còn lại tại `frontend/src/customer/mock.ts` chỉ phục vụ nội dung minh họa chưa có API thật. Kiểu domain nằm ở `types.ts`; `service.ts` là adapter cho trang Customer.

Danh sách trận có lọc ngày, đội, CompetitionSeason và trạng thái; các điều kiện kết hợp, tìm tên đội có debounce 280ms, tải thêm theo lô bốn trận. Chi tiết giữ header khi chuyển tab. Phong độ/H2H chỉ dùng trận FINISHED trước kickoff của trận đang xem, tính W/D/L theo đội và H2H có tải thêm. Tỷ số thiếu hiển thị `- : -`; thống kê và bảng điểm thiếu có empty state. Giờ hiển thị theo `Asia/Ho_Chi_Minh`.

Danh sách và đầu trang chi tiết gọi `/api/public/matches`, dùng DTO tên/logo đội, tên giải theo mùa, tỷ số và giờ Việt Nam từ backend. Bộ lọc giải theo mùa lấy `/api/competition-seasons`; tìm tên đội, lọc ngày/trạng thái/giải dùng tham số Match API. Tab Phong độ & Đối đầu có ba mục con: Phong độ, Đối đầu và Biểu đồ; mỗi lần chỉ hiển thị mục được chọn. Biểu đồ dùng kết quả Recent Form để hiển thị hai đội cạnh nhau trên màn hình rộng, mỗi đội một thanh tỷ lệ W/D/L, một hàng tối đa 10 kết quả từ cũ đến mới và số trận kèm phần trăm thắng/hòa/thua. Trên mobile, hai đội xếp dọc, mỗi hàng kết quả co theo chiều rộng màn hình, không yêu cầu cuộn ngang. Tổng H2H hiển thị tỷ lệ đội nhà thắng/hòa/đội khách thắng. Phong độ gọi `/api/public/matches/{id}/recent-form` một lần cho cả hai đội; H2H gọi `/api/public/matches/{id}/head-to-head` theo trang 5 trận, bấm Xem thêm mới tải trang tiếp. Các dòng trận trong lịch sử phong độ và H2H chỉ để xem, không mở chi tiết trận khi bấm. Tổng H2H lấy từ backend trên toàn bộ lịch sử, không tính lại từ trang đã tải. Tab Bảng xếp hạng gọi Standing API thật; tab Nhận định AI của trận thật dùng nội dung Admin đã xuất bản. Tab Thống kê của trận không thuộc diện Radar giữ dữ liệu mẫu hoặc trạng thái thiếu dữ liệu. Chưa tích hợp AI API hoặc xác thực khách hàng.
