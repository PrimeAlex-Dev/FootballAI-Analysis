# Customer UI

Tab Bảng xếp hạng gọi Standing API theo CompetitionSeason của Match; hiển thị các bảng theo thứ tự nguồn, tô sáng hai đội trong trận và hiện trạng thái chưa có dữ liệu khi thiếu Standing. Xem [Standing API](standing-api.md).

Customer UI tại `/matches`; chi tiết tại `/matches/:matchId` với bốn route con `ai`, `form`, `statistics`, `standings`. Route chi tiết mặc định chuyển đến `ai`. Admin Dashboard vẫn ở `/dashboard`.

Frontend dùng React Router, Lucide và CSS variables tương thích màu nhận diện `#99FF33`. Customer có theme riêng sáng/tối lưu trong trình duyệt, không thay đổi theme Admin. Bố cục responsive, tab và thanh lọc có thể cuộn ngang trong vùng của chúng ở mobile.

Dữ liệu còn lại tại `frontend/src/customer/mock.ts` chỉ phục vụ nội dung minh họa chưa có API thật. Kiểu domain nằm ở `types.ts`; `service.ts` là adapter cho trang Customer.

Danh sách trận có lọc ngày, đội, CompetitionSeason và trạng thái; các điều kiện kết hợp, tìm tên đội có debounce 280ms, tải thêm theo lô bốn trận. Chi tiết giữ header khi chuyển tab. Phong độ/H2H chỉ dùng trận FINISHED trước kickoff của trận đang xem, tính W/D/L theo đội và H2H có tải thêm. Tỷ số thiếu hiển thị `- : -`; thống kê và bảng điểm thiếu có empty state. Giờ hiển thị theo `Asia/Ho_Chi_Minh`.

Danh sách và đầu trang chi tiết gọi `/api/matches`, dùng DTO tên/logo đội, tên giải theo mùa, tỷ số và giờ Việt Nam từ backend. Bộ lọc giải theo mùa lấy `/api/competition-seasons`; tìm tên đội, lọc ngày/trạng thái/giải dùng tham số Match API. Tab Phong độ & Đối đầu có ba mục con: Phong độ, Đối đầu và Biểu đồ; mỗi lần chỉ hiển thị mục được chọn. Biểu đồ dùng kết quả Recent Form để hiển thị hai đội cạnh nhau trên màn hình rộng, mỗi đội một thanh tỷ lệ W/D/L, một hàng tối đa 10 kết quả từ cũ đến mới và số trận kèm phần trăm thắng/hòa/thua. Trên mobile, hai đội xếp dọc, mỗi hàng kết quả co theo chiều rộng màn hình, không yêu cầu cuộn ngang. Tổng H2H hiển thị tỷ lệ đội nhà thắng/hòa/đội khách thắng. Phong độ gọi `/api/matches/{id}/recent-form` một lần cho cả hai đội; H2H gọi `/api/matches/{id}/head-to-head` theo trang 5 trận, bấm Xem thêm mới tải trang tiếp. Các dòng trận trong lịch sử phong độ và H2H chỉ để xem, không mở chi tiết trận khi bấm. Tổng H2H lấy từ backend trên toàn bộ lịch sử, không tính lại từ trang đã tải. Tab Bảng xếp hạng gọi Standing API thật; tab AI và thống kê vẫn dùng dữ liệu mẫu hoặc hiện trạng thái thiếu dữ liệu. Chưa có AI thật hoặc xác thực khách hàng.
