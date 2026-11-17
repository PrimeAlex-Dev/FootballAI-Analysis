# AI Football Analysis — Sprint 1 Tech Stack & Architecture Specification

**Phiên bản:** 1.0  
**Trạng thái:** Tech stack cốt lõi đã được lựa chọn; một số quyết định triển khai cần chốt sau  
**Phạm vi:** Sprint 1 — Football Data Foundation / Admin Data Management  
**Mục đích tài liệu:** Làm nguồn tham chiếu thống nhất để thiết kế kiến trúc, tổ chức mã nguồn, tạo backlog, thiết kế cơ sở dữ liệu, triển khai API và cấu hình môi trường phát triển.

---

## 1. Bối cảnh và mục tiêu

AI Football Analysis là dự án hướng đến phân tích chuyên sâu các trận đấu bóng đá dựa trên dữ liệu. Sprint 1 **chưa phát triển AI Analysis Engine hoặc hệ thống khách hàng** mà tập trung xây dựng nền tảng dữ liệu và công cụ quản trị.

Hệ thống phải quản lý và liên kết các đối tượng nghiệp vụ như liên đoàn, giải đấu, mùa giải, đội bóng, cầu thủ, squad, trận đấu, kết quả và thống kê. Dữ liệu lịch sử phải có khả năng truy xuất và tái sử dụng, thay vì tạo lại đội bóng hay phong độ cho từng trận như phiên bản trước.

**Ưu tiên nghiệp vụ đặc biệt:** nhập nhiều dữ liệu JSON từ công cụ thu thập/extension, kiểm tra hợp lệ, nhận diện đối tượng đã có, liên kết các thực thể, lưu kết quả và báo cáo lỗi rõ ràng. Recent Form và Head-to-Head về sau phải có thể truy vấn từ lịch sử trận đấu đã lưu.

### Mục tiêu kỹ thuật Sprint 1

1. Xây dựng một backend API tập trung, tách module theo nghiệp vụ.
2. Duy trì ràng buộc quan hệ, tính toàn vẹn và lịch sử dữ liệu trên database.
3. Phát triển ứng dụng admin để tìm kiếm, kiểm tra, nhập và quản lý dữ liệu.
4. Tạo luồng bulk import JSON có validation, phát hiện trùng, cơ chế tạo mới/cập nhật và khả năng nhập lại an toàn.
5. Chuẩn hóa môi trường local và triển khai thông qua Docker.
6. Quản lý mã nguồn trên GitHub và thiết lập pipeline CI/CD để kiểm tra, đóng gói và triển khai có kiểm soát.
7. Để ngỏ khả năng tích hợp OpenAI API, job processing và live data ở sprint sau mà không bắt buộc triển khai ngay.

---

## 2. Tech stack đã chốt

| Lớp | Công nghệ | Trạng thái | Vai trò |
|---|---|---|---|
| Architecture | **Modular Monolith** | **Đã chốt** | Một backend triển khai chung, chia module rõ ràng theo nghiệp vụ |
| Backend | **Java + Spring Boot** | **Đã chốt** | Xây dựng REST API, nghiệp vụ, validation, quản trị dữ liệu và import |
| Frontend | **React + TypeScript** | **Đã chốt** | Ứng dụng giao diện quản trị Admin |
| ORM / Persistence | **Spring Data JPA + Hibernate** | **Đã chốt** | Ánh xạ entity và truy cập dữ liệu quan hệ |
| Database | **PostgreSQL** | **Đã chốt** | Lưu trữ dữ liệu nghiệp vụ, lịch sử, quan hệ và metadata |
| Database Migration | **Flyway** | **Đã chốt** | Quản lý phiên bản và thay đổi schema có kiểm soát |
| Containerization | **Docker** | **Đã chốt** | Đóng gói ứng dụng và chuẩn hóa môi trường |
| Version Control | **Git + GitHub** | **Đã chốt** | Lưu trữ mã nguồn, lịch sử thay đổi, pull request và review |
| CI/CD | **GitHub-based CI/CD** | **Đã chốt về nền tảng** | Tự động build/test và triển khai theo quy trình; **GitHub Actions là đề xuất triển khai** |

**Chưa chốt:** Phiên bản cụ thể của Java/Spring Boot/Node, công cụ build backend (Maven/Gradle), trình bundler frontend (Vite hoặc khác), UI library, thư viện client gọi API, phương án authentication, hosting/cloud provider, môi trường staging/production và chiến lược deployment. Các lựa chọn này sẽ được quyết định sau dựa trên yêu cầu vận hành thực tế.

## 3. Nguyên tắc kiến trúc

### 3.1. Modular Monolith

- Toàn bộ backend vận hành như **một ứng dụng Spring Boot**; không triển khai microservices trong Sprint 1.
- Mỗi module có ranh giới nghiệp vụ, API, application service, domain/entity và persistence riêng khi phù hợp.
- Module không truy cập trực tiếp repository nội bộ của module khác. Giao tiếp qua interface/application service đã công bố.
- Ưu tiên transaction trong cùng một ứng dụng/database cho các nghiệp vụ có thay đổi liên quan.
- Tránh thiết kế vòng phụ thuộc giữa các module. Dùng module dùng chung có giới hạn cho các thành phần thực sự xuyên suốt (errors, audit, định danh nguồn, security primitives).
- Không tạo các service phân tán hay message broker nếu chưa có nhu cầu đã được chứng minh.

### 3.2. Luồng dữ liệu cấp cao

```text
Chrome Extension / JSON Input           Admin Web (React + TypeScript)
             |                                      |
             +--------------- HTTP API -------------+
                                    |
                      Spring Boot Modular Monolith
                                    |
     +------------------------------+-----------------------------+
     | Federation / Competition / Season / Team / Player / Squad |
     | Match / Match Statistics / Search / Import / Admin       |
     +------------------------------+-----------------------------+
                                    |
                         Spring Data JPA / Hibernate
                                    |
                              PostgreSQL
                                    |
                     Schema migrations by Flyway
```

Extension là **nguồn xuất dữ liệu đầu vào**, không truy cập trực tiếp PostgreSQL. Backend là nơi thực hiện validation, xác định quan hệ, kiểm tra trùng và lưu dữ liệu.

### 3.3. Phạm vi triển khai

- **Sprint 1:** dữ liệu và quản trị, REST API, bulk JSON import, lịch sử, search/filter, kiểm thử, Docker, GitHub CI/CD.
- **Chưa thuộc Sprint 1:** AI tự research, gọi OpenAI API để phân tích trận, auto-publish, live match ingestion, live tactical analysis, customer website, chat và bình chọn.
- **Thiết kế dự phòng:** Đặt ranh giới rõ ràng để sau này bổ sung AI Integration, scheduler/worker và các nguồn API dữ liệu có quyền sử dụng phù hợp.

---

## 5. Backend — Spring Boot

### 5.1. Phân công trách nhiệm

Backend chịu trách nhiệm:

- REST API cho CRUD có kiểm soát, query/search/filter/pagination.
- Kiểm tra dữ liệu đầu vào và quy tắc nghiệp vụ.
- Điều phối import theo thứ tự phụ thuộc: tham chiếu giải/mùa/đội/cầu thủ trước khi tạo quan hệ và trận đấu.
- Phân biệt tạo mới, cập nhật, bỏ qua, báo lỗi/xung đột; chống nhập trùng.
- Transaction, tính toàn vẹn dữ liệu, báo cáo import và lịch sử tác động.
- Cấp dữ liệu cho Admin UI; không đưa logic nhận diện trùng vào React.

### 5.2. Phân lớp gợi ý bên trong module

```text
<module>/
  api/              # REST controller, request/response DTO
  application/      # use case, service điều phối nghiệp vụ
  domain/           # entity, rule, domain services nếu cần
  infrastructure/   # JPA repositories, persistence adapters
```

Đây là **quy ước tổ chức đề xuất**, không bắt buộc tất cả module phải có đầy đủ bốn package ngay từ đầu. Tránh dựng nhiều lớp boilerplate không mang lại giá trị thực.

### 5.3. API contract

- Request và response dùng DTO; không trả JPA entity trực tiếp.
- API danh sách có filter, sort, pagination.
- Có mã lỗi/response nhất quán cho validation error, not found, duplicate, conflict, forbidden và import partial failure.
- API bulk import phải thể hiện rõ số bản ghi được tạo/cập nhật/bỏ qua/thất bại và lý do.
- Tài liệu OpenAPI/Swagger **được đề xuất**, chưa phải công nghệ đã chốt.

---

## 6. Persistence — PostgreSQL + Spring Data JPA/Hibernate

### 6.1. Nguyên tắc lưu trữ

- Dữ liệu nghiệp vụ chính được mô hình hóa bằng bảng quan hệ và khóa ngoại.
- Có ràng buộc `UNIQUE`, `NOT NULL`, `CHECK`, foreign keys và index phù hợp; validation của ứng dụng **không thay thế** các ràng buộc database.
- Mỗi đối tượng có **ID nội bộ ổn định**; ID nguồn bên ngoài (như Flashscore match ID) là ánh xạ riêng và phải tính tới nguồn dữ liệu.
- Bảo toàn lịch sử tham dự giải, thay đổi squad và trận đấu; hạn chế hard-delete dữ liệu đã được tham chiếu.
- Có thể dùng `JSONB` cho raw import payload, metadata nguồn hoặc phần chỉ số mở rộng; **không dùng một JSON blob thay cho tất cả thực thể quan hệ**.
- Lưu `source`, `externalId`, thời điểm thu thập và các thông tin cần thiết cho truy xuất nguồn gốc khi có dữ liệu tương ứng.

### 6.2. Quy tắc ORM

- Quản lý transaction ở application/service layer cho các use case có thay đổi nhiều bảng.
- Tránh `EAGER` mặc định cho toàn bộ quan hệ và tránh serialization trực tiếp entity gây vòng lặp hoặc N+1 query.
- Sử dụng DTO/projection/query chuyên biệt cho màn hình search và lịch sử khi hợp lý.
- Chốt quy tắc cascade và xóa theo từng quan hệ; không cascade xóa trận lịch sử khi xóa/ẩn đội bóng.
- Bulk import lớn có thể cần batching và truy vấn SQL tối ưu ở những điểm đo đạc thấy JPA chưa phù hợp; **không bắt buộc mọi thao tác phải qua `save()` từng hàng**.

---

## 7. Flyway — Quản lý schema

- **Flyway là nguồn sự thật cho thay đổi cấu trúc database** giữa các phiên bản.
- Các migration được version hóa và commit vào GitHub cùng mã nguồn.
- Cấu hình production theo hướng Hibernate **không tự ý thay đổi schema**; migration thực hiện qua Flyway.
- Không sửa migration đã chạy trên môi trường dùng chung; tạo migration mới để thay đổi.
- Dữ liệu mẫu/test và dữ liệu production phải phân biệt; không vô tình seed dữ liệu minh họa vào production.
- Pipeline triển khai phải xem xét backup và khả năng khôi phục trước các migration phá vỡ tương thích.

Định hướng thư mục:

```text
backend/src/main/resources/db/migration/
  V1__initial_schema.sql
  V2__add_source_identifiers.sql
  ...
```

Các tên migration chỉ để minh họa; danh sách thực tế sẽ phụ thuộc ERD được duyệt.

---

## 8. Frontend Admin — React + TypeScript

Giao diện dành cho admin trong Sprint 1, không phải website khách hàng.

### Màn hình nghiệp vụ tối thiểu

1. Danh sách và trang chi tiết giải đấu/mùa giải.
2. Quản lý đội bóng, cầu thủ và squad.
3. Danh sách trận đấu với filter theo giải, mùa, đội, ngày, trạng thái.
4. Trang chi tiết trận, kết quả và thống kê.
5. Trang **Bulk JSON Import**: tải/dán JSON, kiểm tra trước khi nhập, xác nhận và xem báo cáo.
6. Hiển thị lỗi trùng, liên kết mơ hồ và thông tin nguồn để admin xử lý.

### Nguyên tắc frontend

- React quản lý tương tác và hiển thị; backend xử lý logic nghiệp vụ và tính hợp lệ cuối cùng.
- Sử dụng TypeScript cho type-safety; ưu tiên type API rõ ràng.
- Xử lý loading, empty state, validation errors, server errors và import results nhất quán.
- Không nhúng database credential/API secret trong frontend.
- **Chưa chốt**: Vite/Next.js, routing, UI component library, form/state/data fetching library.

---

## 9. Bulk Import — Luồng trọng tâm Sprint 1

### 9.1. Workflow dự kiến

```text
JSON từ Admin / Extension
    -> Parse + Schema Validation
    -> Normalize dữ liệu, tên, mã nguồn, thời gian
    -> Resolve existing records / external IDs
    -> Detect duplicate / ambiguous matches
    -> Preview (create / update / skip / reject)
    -> Confirm import
    -> Persist trong transaction thích hợp
    -> Import report + audit
```

### 9.2. Các đặc tính bắt buộc

- **Idempotent:** nhập lại cùng nguồn và cùng bản ghi không tạo thêm bản sao.
- **Relationship-aware:** không tạo trận nếu đội hoặc mùa giải chưa được xác định theo quy tắc.
- **Conflict-safe:** nếu thông tin định danh mâu thuẫn hoặc tên trùng, báo cần xử lý thay vì ghép bừa.
- **Partial failure rõ ràng:** chính sách atomic theo batch hay từng nhóm giao dịch sẽ được chốt khi thiết kế import; báo cáo phải thể hiện chính xác kết quả.
- **Traceable:** có nguồn, thời điểm, trạng thái import, thống kê create/update/skip/reject.
- **Validated:** kiểm tra kiểu dữ liệu, phạm vi giá trị, tham chiếu và quy tắc nghiệp vụ; không coi số 0 là thiếu dữ liệu.
- **Extensible:** có adapter chuyển JSON của extension sang một dạng nội bộ thống nhất; không thiết kế entity/database phụ thuộc trực tiếp tên field của extension.

### 9.3. Scope thực tế

Sprint 1 hỗ trợ **admin chủ động gửi JSON**. Chưa yêu cầu scheduler tự crawl hoặc gọi API dữ liệu. Việc tự động lấy nguồn chỉ là hướng tích hợp về sau, tuân thủ điều kiện sử dụng của nhà cung cấp.

---

## 10. Docker và các môi trường

**Đã chốt sử dụng Docker**, triển khai cụ thể dự kiến gồm container backend và PostgreSQL; frontend có thể dùng container khi phục vụ build/triển khai.

- Local development: cấu hình nhất quán để chạy backend, frontend, PostgreSQL.
- Sử dụng biến môi trường cho cấu hình và secret; không commit mật khẩu/database credentials.
- Có health check và thứ tự khởi tạo phù hợp cho database/migration/backend.
- Chọn volume/persistence cho dữ liệu PostgreSQL; không xóa dữ liệu do thao tác build container.
- Docker Compose cho local là **đề xuất**, chưa phải quyết định bổ sung đã chốt.
- Chiến lược staging/production, domain, SSL/TLS, reverse proxy, backup và hosting sẽ chốt ở bước triển khai.

---

## 11. GitHub Version Control & CI/CD

### 11.1. GitHub

- Một repository hay nhiều repository **chưa chốt**; monorepo là đề xuất thực dụng cho Sprint 1.
- Thay đổi mã nguồn được theo dõi bằng commit và pull request.
- Không commit `.env`, API key, database password hoặc tệp bí mật.
- README hướng dẫn setup, cách chạy migration, test và import mẫu.

### 11.2. CI/CD đề xuất với GitHub Actions

**CI tối thiểu khi push / pull request:**

1. Checkout mã nguồn.
2. Cài đặt toolchain đúng phiên bản dự án.
3. Backend: compile, unit test, integration test phù hợp.
4. Frontend: install dependencies từ lockfile, lint/typecheck/test, production build.
5. Kiểm tra Docker build khi đã có Dockerfile tương ứng.
6. Chặn merge hoặc báo fail khi các bước bắt buộc không đạt.

**CD (khi có môi trường triển khai):**

1. Build artifact/container image từ commit đã kiểm tra.
2. Chạy/deploy lên môi trường mục tiêu theo branch/tag được cho phép.
3. Quản lý secret bằng cơ chế an toàn của môi trường CI/CD.
4. Thực hiện kiểm tra migration, health check và khả năng khôi phục.
5. Production deploy ưu tiên có bước phê duyệt ở giai đoạn đầu.

**Quan trọng:** CI/CD được chốt là một phần Sprint 1, nhưng không mặc định phải tự động deploy production ngay khi push code. Workflow phát hành phụ thuộc hạ tầng được lựa chọn sau.

---

## 12. Kiểm thử và chất lượng

### Backend

- Unit test cho các nghiệp vụ quan trọng: xác định bản ghi trùng, ràng buộc quan hệ, quy tắc cập nhật.
- Integration test với PostgreSQL cho repository/transaction/import, đặc biệt các tình huống dữ liệu trùng và nhập lại.
- API test cho validation, lỗi, phân trang, recent form và H2H.
- Migration test để xác minh schema khởi tạo từ database sạch.

### Frontend

- Kiểm thử các trạng thái danh sách, tìm kiếm, lỗi form/import, preview và kết quả import.
- Bảo đảm thao tác admin không vô tình tạo bản ghi trùng.

### Acceptance tests cốt lõi

- Nhập đội bóng lần đầu -> tạo bản ghi; nhập lại -> tái sử dụng.
- Nhập trận đấu có hai đội đã tồn tại -> liên kết đúng home/away.
- Nhập squad cầu thủ chuyển đội -> giữ được lịch sử đội cũ.
- Nhập JSON sai hoặc mơ hồ -> từ chối hoặc yêu cầu xử lý, không âm thầm ghi sai.
- Query lịch sử đội, recent form, H2H dựa trên dữ liệu đã lưu.
- Chạy database từ môi trường sạch -> migration thành công.

---

## 13. Định hướng repository (đề xuất)

```text
ai-football-analysis/
  backend/
    src/main/java/.../
    src/main/resources/db/migration/
    src/test/
    Dockerfile
  frontend/
    src/
    Dockerfile                 # nếu cần đóng gói frontend
  docs/
    project-overview.md
    sprint-1-tech-stack.md
    api-contracts/            # sau khi thiết kế chi tiết
    data-model/               # ERD, quy tắc quan hệ
  infrastructure/
    ...                       # cấu hình triển khai khi có
  .github/workflows/
    ...                       # CI/CD
  README.md
  .gitignore
```

Đây là cấu trúc gợi ý để dễ quản lý chung một dự án; có thể điều chỉnh khi phân chia module thực tế.

---

## 14. Các quyết định đã chốt và chưa chốt

### Đã chốt

- Kiến trúc **Modular Monolith**.
- Backend **Spring Boot**.
- Frontend Admin **React + TypeScript**.
- Persistence **Spring Data JPA / Hibernate**.
- Database **PostgreSQL**.
- Migration **Flyway**.
- Containerization **Docker**.
- Version control **GitHub**.
- CI/CD dựa trên GitHub; workflow cụ thể sẽ thiết kế ở bước tiếp theo.

### Chưa chốt — không tự mặc định trong code

- Java/JDK, Spring Boot, PostgreSQL và React phiên bản cụ thể.
- Maven hay Gradle; build tooling frontend.
- UI library, router, HTTP client, state management.
- JWT hay session và mô hình phân quyền.
- Monorepo hay multirepo.
- Chiến lược ID/khóa, audit fields, enum, soft delete cụ thể.
- ERD chi tiết, API endpoint names, DTO/schema, business validation chi tiết.
- Import sync/async, transaction boundary, batching và dung lượng JSON tối đa.
- Test framework cụ thể ngoài stack tiêu chuẩn phù hợp.
- GitHub Actions workflow chi tiết, artifact registry, hosting, secrets và deployment approvals.
- Quan sát vận hành: logs/metrics/alerts, backup và disaster recovery.

---

## 15. Definition of Done — Sprint 1 (góc nhìn tech stack)

- [ ] Backend Spring Boot chạy theo kiến trúc module rõ ràng và có REST API nghiệp vụ.
- [ ] PostgreSQL lưu được Federation, Competition, Season, Team, Player, Squad, Match và Statistics với quan hệ đúng.
- [ ] Schema được tạo và nâng cấp bằng Flyway, không phụ thuộc Hibernate tự tạo bảng trong môi trường dùng chung.
- [ ] Spring Data JPA/Hibernate đáp ứng truy vấn, transaction và các use case chính.
- [ ] React + TypeScript cung cấp màn hình admin tối thiểu cho quản lý, search/filter và bulk import.
- [ ] Import JSON có validation, liên kết bản ghi, chống trùng, preview/report và xử lý lỗi rõ ràng.
- [ ] API truy vấn được lịch sử trận đấu, Recent Form và H2H từ dữ liệu đã lưu.
- [ ] Docker hỗ trợ môi trường chạy có thể tái lập, dữ liệu PostgreSQL được lưu bền vững.
- [ ] Repository GitHub có README, quy trình branch/PR và không chứa secret.
- [ ] CI chạy được build/test backend và frontend; CD theo môi trường triển khai đã thống nhất.
- [ ] Có bộ kiểm thử quan trọng cho tính toàn vẹn dữ liệu, import và migrations.

---

## 16. Kết luận

Sprint 1 sử dụng **Modular Monolith + Spring Boot + React/TypeScript + Spring Data JPA/Hibernate + PostgreSQL + Flyway + Docker + GitHub CI/CD**.

Tech stack này nhằm tạo **Football Data Foundation**: dữ liệu tập trung, quan hệ chính xác, có lịch sử, có thể tìm kiếm và tái sử dụng; import JSON thuận tiện để giảm tải cho admin. Mọi quyết định kỹ thuật tiếp theo cần phục vụ trực tiếp các yêu cầu này trước khi mở rộng sang AI Analysis Engine hoặc các tính năng khách hàng.

> **Nguyên tắc xuyên suốt:** *Collect Once, Store Properly, Reuse Everywhere.*
