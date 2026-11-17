# AI Football Analysis — Sprint 1 Coding Standards & Conventions

> **Phiên bản:** 1.0  
> **Phạm vi:** Toàn bộ source code Sprint 1 — Backend, Frontend, Database Migration, Tests, Scripts và CI/CD.  
> **Tech stack đã chốt:** Modular Monolith; Spring Boot; Spring Data JPA/Hibernate; PostgreSQL; Flyway; React + TypeScript; Docker; GitHub CI/CD.  
> **Mục đích:** Tạo một bộ quy tắc thống nhất để lập trình viên và AI coding assistant xây dựng các module theo **cùng kiến trúc, cùng cách tổ chức, cùng phong cách code**, dễ đọc và dễ bảo trì.

---

Tài liệu liên quan: [Ý tưởng Sprint 1](../requirements/ai_football_analysis_project_overview_sprint_1.md), [Tech Stack](../architecture/AI_Football_Analysis_Sprint_1_Tech_Stack.md), [AGENTS.md](../../AGENTS.md).

**Trạng thái repository:** Monorepo; frontend dùng Vite + React + TypeScript với npm và Oxlint từ template; backend dùng Java 21, Spring Boot 4.1.2, Maven Wrapper 3.9.13. Chưa có Reference Module được duyệt. Định dạng cơ bản theo .editorconfig root; formatter đầy đủ chưa chốt.

## 1. Triết lý viết code

1. **Rõ ràng hơn ngắn gọn.** Viết code sạch, vừa đủ ngắn nhưng **tường minh**. Không rút ngắn chỉ để giảm số dòng.
2. **Dễ hiểu hơn thông minh.** Ưu tiên cú pháp phổ biến, dễ đọc, dễ debug. Tránh mẹo lập trình, cú pháp lạ, nested ternary, chaining quá dài và lạm dụng abstraction.
3. **Nhất quán quan trọng hơn sở thích cá nhân.** Những module sau phải tuân theo mẫu kiến trúc, cấu trúc folder, naming, response format, exception handling và coding style đã thống nhất ở module đầu tiên.
4. **Tách trách nhiệm rõ ràng.** Mỗi class, function, component và module nên có một nhiệm vụ chính.
5. **Không over-engineering.** Chỉ thêm interface, generic, design pattern, service, helper, thư viện hoặc tầng trừu tượng khi có lý do thực tế.
6. **Dễ thay đổi, dễ kiểm thử.** Không trộn logic nghiệp vụ vào controller, UI component hoặc repository.
7. **Bảo toàn dữ liệu.** Các thao tác import, cập nhật quan hệ và lịch sử phải rõ ràng, an toàn, không tự ghi đè hay tạo trùng.
8. **Không làm ngoài phạm vi.** Không tự thêm tính năng, thư viện, module hoặc thay đổi kiến trúc khi chưa được yêu cầu hoặc thống nhất.

**Nguyên tắc quyết định khi có nhiều cách viết:** chọn phương án mà một lập trình viên khác có thể hiểu nhanh, sửa an toàn và tiếp tục phát triển theo mẫu hiện có.

---

## 2. Mức độ bắt buộc và cách thay đổi quy tắc

- **MUST / Bắt buộc:** Tuân thủ đối với mọi module.
- **SHOULD / Khuyến nghị:** Áp dụng mặc định; có thể thay đổi nếu giải thích được lợi ích.
- **MAY / Tùy chọn:** Chỉ sử dụng khi cần thiết.

Nếu một yêu cầu thực tế xung đột với quy tắc này:

1. Không âm thầm làm khác.
2. Nêu rõ điểm xung đột và đề xuất cách xử lý.
3. Chỉ áp dụng thay đổi sau khi được chấp thuận.
4. Cập nhật tài liệu và **module mẫu** nếu quy tắc mới áp dụng toàn dự án.

**Không thay đổi hàng loạt code cũ theo sở thích khi đang làm một chức năng mới.** Refactor lớn phải có phạm vi và mục tiêu riêng.

---

## 3. Module đầu tiên là chuẩn tham chiếu (Reference Module)

### 3.1. Quy tắc quan trọng nhất

**Module nghiệp vụ đầu tiên được hoàn thiện và duyệt sẽ trở thành _Reference Module_ chính thức của Sprint 1.** Chưa mặc định module nào là chuẩn khi chưa duyệt.

Reference Module phải thể hiện đầy đủ, ở mức phù hợp:

- Cấu trúc thư mục/package.
- Phân lớp Controller → Service → Repository và vai trò của Entity, DTO, Mapper.
- Quy tắc đặt tên.
- Validation và business rule.
- Exception handling và API response.
- Logging.
- Transaction boundary.
- Test structure.
- API documentation.
- Frontend API client, form, bảng danh sách và state nếu có UI quản trị tương ứng.

### 3.2. Quy tắc nhân bản mô hình

Khi triển khai module tiếp theo, **MUST**:

1. Đọc lại Reference Module và tài liệu này trước khi code.
2. Sử dụng cùng cấu trúc package/folder và cách phân lớp.
3. Dùng cùng cách đặt tên, DTO, exception, validation, pagination, search/filter và response format.
4. Dùng cùng phương pháp mapping, logging, transaction và viết test.
5. Chỉ thay đổi những phần khác biệt về nghiệp vụ; không tự sáng tạo một kiến trúc khác.
6. Nếu module mới cần pattern chưa có trong mẫu, đề xuất bổ sung pattern dùng chung trước khi áp dụng.

**Ví dụ không chấp nhận:** Module Team dùng `Controller → Service → Repository`, nhưng module Match lại đưa SQL hoặc business logic trực tiếp vào Controller; module Player trả JSON trực tiếp từ Entity trong khi các module khác trả DTO.

### 3.3. Reference Module Checklist

Trước khi đánh dấu module đầu tiên là chuẩn, phải kiểm tra:

- [ ] Folder/package và trách nhiệm các lớp rõ ràng.
- [ ] DTO request/response riêng khi cần.
- [ ] Validation ở đúng tầng.
- [ ] API response và lỗi đồng nhất.
- [ ] Business rules có test.
- [ ] Không để controller chứa nghiệp vụ.
- [ ] Không expose trực tiếp Entity JPA ra API.
- [ ] Có migration tương ứng.
- [ ] Review code, formatting và naming hoàn tất.

---

## 4. Kiến trúc Backend: Modular Monolith

### 4.1. Quy tắc tổ chức module

- **MUST** tổ chức code theo **nhóm nghiệp vụ** (feature/module), không gom toàn bộ controllers của mọi module vào một folder toàn cục rồi toàn bộ services vào folder khác.
- Mỗi module sở hữu phần logic nghiệp vụ của nó.
- Code dùng chung chỉ đưa vào `common`/`shared` khi thực sự dùng chung và không chứa logic đặc thù một module.
- Module khác không truy cập tùy tiện repository hay internals của module này; giao tiếp qua service/interface công khai được thống nhất.
- Tránh phụ thuộc vòng tròn giữa các module.
- Không tách microservice trong Sprint 1.

**Cấu trúc tham khảo (sẽ điều chỉnh theo Reference Module được duyệt):**

```text
backend/
  src/main/java/.../
    common/
      exception/
      response/
      config/
    federation/
      controller/
      service/
      repository/
      entity/
      dto/
      mapper/
    competition/
    season/
    team/
    player/
    squad/
    match/
    statistics/
    importdata/
  src/main/resources/
    db/migration/
  src/test/
```

Đây là **mẫu định hướng**, không phải yêu cầu tạo tất cả folder trống trước khi có chức năng.

### 4.2. Trách nhiệm từng tầng

| Tầng | Trách nhiệm | Không được làm |
|---|---|---|
| Controller | Nhận request, validation đầu vào, gọi service, trả response | Business logic, truy vấn DB trực tiếp |
| Service | Nghiệp vụ, điều phối module, transaction | Phụ thuộc chi tiết HTTP/UI |
| Repository | Truy vấn, lưu trữ dữ liệu | Quyết định nghiệp vụ, dựng response HTTP |
| Entity | Mô hình dữ liệu persistence và quan hệ | Dùng làm public API contract |
| DTO | Hình dạng dữ liệu request/response | Trộn truy vấn DB hoặc logic nghiệp vụ phức tạp |
| Mapper | Chuyển đổi rõ ràng giữa Entity và DTO | Thực hiện truy vấn DB ẩn hoặc side effect bất ngờ |

**Không thêm các tầng như Facade, Manager, Handler, UseCase chỉ vì thích.** Chỉ sử dụng khi có nhu cầu và thống nhất cho toàn dự án.

---

## 5. Quy tắc Java / Spring Boot

### 5.1. Naming và formatting

- Class, enum: `PascalCase` — `TeamService`, `MatchStatus`.
- Method, variable, field: `camelCase` — `findTeamById`, `homeTeamId`.
- Constant: `UPPER_SNAKE_CASE` — `MAX_IMPORT_ITEMS`.
- Package: chữ thường — `team`, `match`, `importdata`.
- Tên phản ánh ý nghĩa, tránh viết tắt khó đoán như `tm`, `mObj`, `doIt`, `x1`.
- Mỗi method nên có một mục đích. Nếu dài và xử lý nhiều bước độc lập, tách thành các method có tên rõ nghĩa.
- Sử dụng formatter thống nhất cho toàn repository. Không format bằng tay mỗi người một kiểu.

### 5.2. Cú pháp ưu tiên

**Ưu tiên:** `if/else`, `for`, early return hợp lý, các phương thức Java phổ biến và tên biến rõ ràng.

**Hạn chế:** nested ternary, stream nhiều tầng khó theo dõi, Optional chaining quá mức, reflection không cần thiết, generic phức tạp, utility quá trừu tượng.

Ví dụ ưu tiên:

```java
if (team == null) {
    throw new TeamNotFoundException(teamId);
}

if (!team.isActive()) {
    throw new InvalidTeamStateException(teamId);
}

return team;
```

Không cần biến một logic đơn giản thành chuỗi toán tử hoặc nhiều lớp abstraction.

### 5.3. Dependency Injection và transaction

- Sử dụng **constructor injection**, tránh field injection.
- Service xử lý nghiệp vụ và xác định transaction boundary.
- Dùng `@Transactional` cho những thao tác thay đổi dữ liệu có liên quan; `readOnly = true` cho các luồng chỉ đọc khi phù hợp.
- Không tự ý gọi external API trong transaction kéo dài nếu có thể tách riêng.
- Hạn chế N+1 query và việc load dữ liệu không cần thiết.

### 5.4. Entity / DTO / Repository

- **MUST** tách request/response DTO khỏi Entity JPA.
- Không trả thẳng Entity chứa quan hệ lazy ra JSON.
- Định nghĩa quan hệ JPA và cascade rõ ràng; không sử dụng cascade xóa hàng loạt theo thói quen.
- Tránh `EAGER` trên mọi quan hệ chỉ để giải quyết lỗi serialization.
- Dùng repository method có tên dễ hiểu; chỉ viết query tùy chỉnh khi query mặc định không đáp ứng tốt.
- Không dùng `findAll()` để tải toàn bộ lịch sử lớn rồi lọc trong bộ nhớ.

### 5.5. Validation, exception, response

- Input validation: bắt buộc, kiểu dữ liệu, range, định dạng.
- Business validation: kiểm tra quan hệ, trạng thái, duplicate, điều kiện cập nhật.
- Có **Global Exception Handler** thống nhất cho lỗi API.
- Message lỗi rõ nghĩa; không trả stack trace hoặc thông tin nhạy cảm cho frontend.
- API response, phân trang và error code **phải đồng nhất giữa các module** theo Reference Module.
- Không tự ý trộn `ResponseEntity`, kiểu raw map và custom wrapper theo từng module khi chưa có quy ước.

### 5.6. Logging

- Log những sự kiện vận hành quan trọng: import bắt đầu/kết thúc, số bản ghi thành công/thất bại, lỗi nghiệp vụ đáng chú ý.
- Log kèm ID liên quan (`matchId`, `teamId`, `importBatchId`) khi có.
- Không dùng `System.out.println()` cho logging nghiệp vụ.
- Không log API key, token, mật khẩu hay toàn bộ payload nhạy cảm.
- Không nuốt exception hoặc chỉ log rồi tiếp tục như thể thành công.

---

## 6. Quy tắc Frontend: React + TypeScript

### 6.1. Cấu trúc và tính nhất quán

- Tổ chức theo feature/module tương ứng với backend khi hợp lý.
- Tách phần gọi API khỏi React component.
- Tách UI trình bày, state và business rules khi logic đủ phức tạp.
- Các màn hình quản trị tương tự nhau phải có chung cách bố trí list/detail/form/search và cùng pattern loading/error/empty state.
- Module mới tham chiếu component, hook và feature đầu tiên đã được duyệt; không tự chọn thư viện state/form/API client mới.

**Cấu trúc tham khảo:**

```text
frontend/src/
  app/
  shared/
    components/
    api/
    types/
    utils/
  features/
    teams/
      api/
      components/
      pages/
      types/
    matches/
    competitions/
    players/
```

### 6.2. TypeScript / React style

- Bật và duy trì TypeScript strict checking khi cấu hình dự án.
- **Không lạm dụng `any`**; ưu tiên type/interface rõ ràng.
- Component và function dùng tên có ý nghĩa, tránh viết tắt.
- Ưu tiên JSX rõ ràng, hạn chế nested ternary và render condition phức tạp.
- Không viết API request trực tiếp lặp lại trong nhiều component.
- Không dùng `useEffect` cho các thao tác có thể xử lý bằng event handler hoặc derived state đơn giản.
- Không tối ưu sớm bằng `useMemo`, `useCallback` ở mọi nơi khi chưa có lý do.
- Input form phải có validation và hiển thị lỗi phù hợp với backend.
- Không nhét một màn hình lớn vào duy nhất một component nhiều trăm dòng nếu có thể tách thành phần rõ ràng.

Ví dụ ưu tiên:

```tsx
function TeamStatus({ isActive }: { isActive: boolean }) {
  if (!isActive) {
    return <span>Inactive</span>;
  }

  return <span>Active</span>;
}
```

---

## 7. Database & Flyway

- **MUST** sử dụng Flyway để theo dõi thay đổi schema.
- **Không** sửa trực tiếp database production rồi quên tạo migration.
- Migration đã áp dụng ở môi trường chia sẻ/production không được sửa nội dung; tạo migration mới để thay đổi.
- Quy tắc tên migration thống nhất, ví dụ `V1__create_team_tables.sql` (quy tắc chính thức theo repository).
- Dùng foreign key, unique constraint, index khi có yêu cầu toàn vẹn/truy vấn cụ thể.
- Quan hệ theo thời gian phải có cấu trúc lưu lịch sử; không xóa lịch sử chỉ để cập nhật trạng thái hiện tại.
- Các thao tác xóa và cascade phải được xem xét đặc biệt với Match, Squad và dữ liệu lịch sử.
- Không lưu toàn bộ dữ liệu bóng đá dưới dạng JSONB nếu việc liên kết/truy vấn yêu cầu các bảng quan hệ. JSONB chỉ dùng cho payload nguồn/metadata/chỉ số linh hoạt phù hợp.
- Mọi thay đổi schema quan trọng cần đi cùng cập nhật Entity, query và test liên quan.

---

## 8. Bulk JSON Import — quy tắc đặc biệt

Bulk Import là nghiệp vụ trọng tâm, nên code phải ưu tiên **đúng, rõ và an toàn**, không tối ưu thiếu kiểm soát.

1. **Tách rõ các bước:** nhận JSON → kiểm tra schema → chuẩn hóa → đối chiếu bản ghi → quyết định tạo/cập nhật/bỏ qua → lưu quan hệ → báo cáo.
2. **Không ghi DB trước khi hoàn thành kiểm tra bắt buộc** trong phạm vi chính sách import đã chọn.
3. **Không matching mù quáng theo tên.** Ưu tiên ID ổn định/ID nguồn; trường hợp mơ hồ phải báo để xử lý.
4. **Idempotency:** Import cùng dữ liệu nhiều lần không tạo bản ghi trùng.
5. **Upsert có kiểm soát:** Không lấy dữ liệu thiếu hoặc cũ ghi đè dữ liệu đúng đang lưu.
6. **Không âm thầm bỏ lỗi:** Tạo báo cáo created/updated/skipped/failed và lý do.
7. **Transaction policy phải rõ:** Có thể atomic theo lô hoặc theo bản ghi/nhóm, nhưng cần quy định trước, không tùy tiện mỗi module một cách.
8. **Không trộn parser của từng nguồn với nghiệp vụ lưu dữ liệu.** Dữ liệu từ extension hay nguồn khác phải được chuẩn hóa thành cấu trúc trung gian dùng chung.
9. **Lưu provenance phù hợp:** source, external ID, thời điểm thu thập và import batch khi có.
10. **Import lớn:** Có giới hạn kích thước, phân trang/batch và cơ chế xử lý phù hợp. Chỉ thêm queue/background worker khi thực sự cần.

Một JSON import thử nghiệm phải có test cho: dữ liệu hợp lệ, thiếu trường, duplicate, import lại, liên kết thực thể, xung đột và lỗi từng bản ghi.

---

## 9. API Design và hợp đồng giao tiếp

- Sử dụng REST theo quy ước nhất quán.
- Endpoint, HTTP method, response status, pagination, filter và sort phải đồng nhất.
- API dùng DTO rõ ràng và có tài liệu OpenAPI/Swagger nếu được chọn cho dự án.
- Không tự ý thay đổi public API contract khi sửa logic nội bộ.
- ID bản ghi, ID nguồn và tên hiển thị phải phân biệt rõ.
- Khi lọc Recent Form/H2H phải trả dữ liệu đúng phạm vi đã lưu; không khẳng định dữ liệu đầy đủ nếu database chưa có đủ lịch sử.
- Khi cập nhật Match, ưu tiên thao tác vào cùng bản ghi tồn tại thay vì tạo bản ghi mới.

**Không quy định cứng mọi URL hay response payload trong file này**; chúng phải được chốt trong API Contract và Reference Module, sau đó áp dụng thống nhất.

---

## 10. Tests và Definition of Done

### 10.1. Quy tắc kiểm thử

- Test quan trọng nhất là **business rules**, không phải chỉ test getter/setter.
- Service test: kiểm tra nghiệp vụ, validation, duplicate, upsert, quan hệ.
- Controller/API test: kiểm tra request/response, validation, status lỗi.
- Repository/integration test: kiểm tra query, constraints và mapping quan hệ quan trọng.
- Import test: kiểm tra idempotency, partial failures hoặc atomicity đúng chính sách.
- Frontend test: các luồng quan trọng về nhập dữ liệu, hiển thị lỗi, tìm kiếm và danh sách.
- Test có tên mô tả hành vi; không phụ thuộc thứ tự chạy.

### 10.2. Definition of Done cho module

- [ ] Theo đúng kiến trúc và style của Reference Module.
- [ ] Tên class, method, biến dễ hiểu; không có đoạn code cố tình viết quá ngắn/khó đọc.
- [ ] Không business logic trong Controller/UI.
- [ ] DTO, validation, exception, response format nhất quán.
- [ ] Có Flyway migration khi thay đổi schema.
- [ ] Có test cho nghiệp vụ và các trường hợp lỗi chính.
- [ ] Đã xử lý data integrity, duplicate và lịch sử nếu liên quan.
- [ ] Không có secret hard-coded hoặc logging nhạy cảm.
- [ ] Format/lint/test/build chạy thành công.
- [ ] Có cập nhật tài liệu/API khi hành vi hoặc contract thay đổi.

---

## 11. GitHub, Pull Request và CI/CD

- Dùng Git và GitHub làm nguồn quản lý phiên bản chính.
- Commit nhỏ, theo mục đích, message có ý nghĩa; không gom nhiều thay đổi không liên quan.
- Một pull request nên giải quyết một mục tiêu rõ ràng.
- PR mô tả: thay đổi gì, vì sao, test như thế nào, có migration/breaking change hay không.
- Review phải ưu tiên tính rõ ràng, đúng nghiệp vụ, nhất quán với Reference Module và toàn vẹn dữ liệu.
- CI tối thiểu phải kiểm tra compile/build và test; format/lint được bổ sung theo cấu hình chốt.
- Không merge code đang build/test lỗi khi chưa có quyết định xử lý rõ ràng.
- Không commit `.env`, API keys, credentials hoặc dữ liệu nhạy cảm.
- Docker config phải nhất quán giữa các thành viên, nhưng không hard-code secret trong image/repository.

---

## 12. Quy tắc khi sử dụng AI để viết code

Áp dụng cho ChatGPT, Codex hoặc các AI coding assistant khác.

**AI MUST:**

1. Đọc tài liệu nghiệp vụ Sprint 1, Tech Stack và file Coding Standards này trước khi triển khai module.
2. Xem và bám sát **Reference Module** cùng những module đã được duyệt.
3. Không tự đổi stack, kiến trúc, cách tổ chức folder hoặc style của dự án.
4. Ưu tiên code thuần, thông dụng, dễ đọc; không dùng cú pháp lạ chỉ để biểu diễn ngắn hơn.
5. Giải thích khi cần thêm dependency, abstraction hoặc thay đổi pattern hiện có.
6. Giữ thay đổi trong phạm vi yêu cầu; không refactor những module không liên quan.
7. Không tự bịa field, quan hệ, quy tắc upsert hoặc API contract khi tài liệu chưa định nghĩa; nêu giả định quan trọng để duyệt.
8. Khi thêm module, bảo đảm controller/service/repository/entity/DTO/mapper/tests tương thích với module mẫu.
9. Không bỏ qua validation hoặc xử lý lỗi chỉ để code chạy được.
10. Kiểm tra build/test/format ở mức khả thi và báo trung thực những gì chưa chạy được.

**Mẫu chỉ dẫn ngắn cho AI (copy vào yêu cầu code):**

> Hãy tuân thủ `docs/standards/AI_Football_Analysis_Sprint_1_Coding_Standards.md` và module tham chiếu đã được duyệt. Viết code gọn, rõ nghĩa, dùng cú pháp phổ biến dễ bảo trì. Giữ đúng kiến trúc Modular Monolith và style của các module trước. Không tự thêm thư viện/tầng abstraction/kiến trúc mới. Nếu nghiệp vụ hoặc pattern chưa rõ, nêu giả định và điểm cần quyết định. Kiểm tra tính nhất quán của DTO, validation, error handling, transaction, migration và tests trước khi hoàn thành.

---

## 13. Những điểm chưa chốt (không được tự coi là quyết định)

Các phần sau phải thống nhất khi bắt đầu tạo project/reference module:

- Chính sách phiên bản React / TypeScript cho nhóm; backend đã chọn Java 21 và Spring Boot 4.1.2.
- Backend đã chọn Maven Wrapper 3.9.13; frontend dùng Vite và npm. Toolchain Node.js chung cho nhóm chưa chốt.
- Formatter đầy đủ và linter backend; frontend hiện dùng Oxlint từ template. Định dạng cơ bản theo .editorconfig root.
- API response/error schema và pagination contract.
- Cách mapping DTO (manual hoặc thư viện).
- Thư viện UI, form, gọi API và quản lý state ở frontend.
- Auth/session/role design.
- Chính sách import transaction, conflict resolution, preview và batch size.
- Cách cấu hình test database, Docker Compose và CI pipeline chi tiết.

**Nguyên tắc:** Chọn một cách thống nhất, ghi vào tài liệu hoặc code mẫu, sau đó tất cả module tiếp theo áp dụng cùng cách đó.

---

## 14. Nguyên tắc kiểm tra nhất quán khi thêm module mới

Trước khi merge, trả lời:

1. Module mới có giống module mẫu về cấu trúc và cách phân lớp không?
2. Có logic nào đáng lẽ thuộc Service nhưng lại nằm ở Controller/Repository/Component không?
3. Có đoạn code nào người khác khó đọc do viết tắt, cú pháp lạ hoặc chaining phức tạp không?
4. Có tự thêm pattern, helper, dependency hoặc response style mới không?
5. Có bảo toàn toàn vẹn dữ liệu và lịch sử không?
6. Có lỗi import, cập nhật và duplicate nào bị bỏ qua không?
7. Test, API contract và migration đã tương ứng với thay đổi chưa?

**Nếu câu trả lời không đạt, module chưa hoàn thành.**

---

## 15. Tuyên ngôn chung

> **Clean code không có nghĩa là code ngắn nhất. Clean code là code dễ hiểu, nhất quán, đúng nghiệp vụ và có thể bảo trì lâu dài.**
>
> **Module đầu tiên đã được duyệt là khuôn mẫu; mọi module sau phải tuân thủ cùng kiến trúc, cách tổ chức và phong cách code. Không có chuyện mỗi module viết theo một kiểu riêng.**

*Tài liệu này là chuẩn áp dụng cho Sprint 1. Mọi thay đổi cần được thống nhất và cập nhật trước khi áp dụng rộng rãi.*
