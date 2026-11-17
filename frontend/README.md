# AI Football Analysis — Admin Frontend

Vite + React + TypeScript từ template react-ts; hiện giữ demo mặc định, chưa có giao diện nghiệp vụ.

## Chạy local

Cần Node.js/npm tương thích với lockfile. Môi trường khởi tạo: Node.js 24.15.0, npm 11.12.1; chưa chốt toolchain chung cho nhóm.

Chạy trong thư mục frontend:

```sh
npm ci
npm run dev
```

Mở URL Vite in trong terminal. Các lệnh kiểm tra:

```sh
npm run lint
npm run build
npm run preview
```

Build kiểm tra TypeScript và tạo dist/. Preview chỉ phục vụ bản build local. Lint dùng Oxlint từ template; chưa có lệnh test hoặc formatter riêng.

Lần kiểm tra khởi tạo: build thành công; Windows Application Control chặn native binding của Oxlint nên lint chưa chạy được. Cần xác minh trên môi trường được phép chạy công cụ, không tắt chính sách bảo vệ để bỏ qua lỗi.

## Quy ước

- Dùng npm và commit package-lock.json khi dependency thay đổi.
- Theo [AGENTS.md](../AGENTS.md) và [Coding Standards](../docs/standards/AI_Football_Analysis_Sprint_1_Coding_Standards.md).
- Chưa có Reference Module được duyệt.
- Không đưa credential hoặc secret vào frontend, kể cả biến môi trường đóng gói cho trình duyệt.
- Dockerfile hiện là placeholder.
