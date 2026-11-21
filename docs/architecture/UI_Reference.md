# UI Reference — /template-ui

Mẫu tham chiếu thiết kế, chưa phải Reference Module nghiệp vụ được duyệt. Dữ liệu giả trong dashboard chỉ minh họa bố cục. Nút nghiệp vụ disabled; chuyển sáng/tối phục vụ xem mẫu.

CSS variables trong .ui-template định nghĩa light/dark, màu nhấn #99FF33, surface trung tính, spacing 8/12/16/24/32 và radius 12/16/20. Font stack Inter, Segoe UI, Arial, sans-serif; Inter chưa đóng gói, dùng fallback có sẵn. Mobile chuyển sidebar sang navigation ngang, nội dung một cột.

Dùng CSS thuần và lucide-react cho icon thống nhất. Chưa cần Tailwind/shadcn, Recharts, TanStack Table cho mẫu tĩnh. Biểu đồ SVG minh họa; bảng semantic HTML chưa có sorting/filtering. Các thư viện còn lại được đánh giá khi có yêu cầu tương tác cụ thể.

Khi làm module, tham chiếu trang này cho giao diện và tuân thủ Coding Standards cùng Reference Module được duyệt cho kiến trúc, contract, hành vi. Không chuyển nhãn và dữ liệu minh họa thành nghiệp vụ mặc định.

Dashboard thật: xem [Dashboard quản trị dữ liệu](dashboard-ui.md). Tokens màu đã tách vào frontend/src/shared/tokens.css để dùng chung; trang mẫu giữ riêng tại /template-ui.
