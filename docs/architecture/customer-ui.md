# Customer UI

Customer UI tại `/matches`; chi tiết tại `/matches/:matchId` với bốn route con `ai`, `form`, `statistics`, `standings`. Route chi tiết mặc định chuyển đến `ai`. Admin Dashboard vẫn ở `/dashboard`.

Frontend dùng React Router, Lucide và CSS variables tương thích màu nhận diện `#99FF33`. Customer có theme riêng sáng/tối lưu trong trình duyệt, không thay đổi theme Admin. Bố cục responsive, tab và thanh lọc có thể cuộn ngang trong vùng của chúng ở mobile.

Dữ liệu tại `frontend/src/customer/mock.ts` là minh họa, không phải dữ liệu thật hoặc kết quả AI. Kiểu domain nằm ở `types.ts`; `service.ts` là adapter duy nhất cho danh sách, tìm kiếm, phong độ, đối đầu, thống kê và bảng xếp hạng. Khi tích hợp backend, thay adapter này và giữ các component/trang.

Danh sách trận có lọc ngày, đội, CompetitionSeason và trạng thái; các điều kiện kết hợp, tìm tên đội có debounce 280ms, tải thêm theo lô bốn trận. Chi tiết giữ header khi chuyển tab. Phong độ/H2H chỉ dùng trận FINISHED trước kickoff của trận đang xem, tính W/D/L theo đội và H2H có tải thêm. Tỷ số thiếu hiển thị `- : -`; thống kê và bảng điểm thiếu có empty state. Giờ hiển thị theo `Asia/Ho_Chi_Minh`.

Danh sách và đầu trang chi tiết gọi `/api/matches`, dùng DTO tên/logo đội, tên giải theo mùa, tỷ số và giờ Việt Nam từ backend. Bộ lọc giải theo mùa lấy `/api/competition-seasons`; tìm tên đội, lọc ngày/trạng thái/giải dùng tham số Match API. Các tab phân tích vẫn dùng dữ liệu mẫu; với trận thực không có dữ liệu tương ứng thì hiển thị trạng thái chưa có dữ liệu. Chưa có AI thật, Standing backend hoặc xác thực khách hàng.
