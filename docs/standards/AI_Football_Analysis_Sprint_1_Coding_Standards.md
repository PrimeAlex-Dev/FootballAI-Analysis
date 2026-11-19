# AI Football Analysis — Sprint 1 Coding Standards & Conventions

> **Phiên bản:** 1.0  
> **Phạm vi:** Toàn bộ source code Sprint 1 — Backend, Frontend, Database Migration, Tests, Scripts và CI/CD.  
> **Tech stack đã chốt:** Modular Monolith; Spring Boot; Spring Data JPA/Hibernate; PostgreSQL; Flyway; React + TypeScript; Docker; GitHub CI/CD.  
> **Mục đích:** Tạo một bộ quy tắc thống nhất để lập trình viên và AI coding assistant xây dựng các module theo **cùng kiến trúc, cùng cách tổ chức, cùng phong cách code**, dễ đọc và dễ bảo trì.

---

Tài liệu liên quan: [Ý tưởng Sprint 1](../requirements/ai_football_analysis_project_overview_sprint_1.md), [Tech Stack](../architecture/AI_Football_Analysis_Sprint_1_Tech_Stack.md), [AGENTS.md](../../AGENTS.md).

**Trạng thái repository:** Monorepo; frontend dùng Vite + React + TypeScript với npm và Oxlint từ template; backend dùng Java 21, Spring Boot 4.1.2, Maven Wrapper 3.9.13. Chưa có Reference Module được duyệt. Định dạng theo .editorconfig root; Java dùng Spotless 3.11.0 + Eclipse 4.40 (4 spaces), frontend/cấu hình được chọn dùng Prettier 3.10.1 (2 spaces). Maven spotless:apply và npm run format sửa định dạng; spotless:check và npm run format:check chỉ kiểm tra. CI bắt buộc format check.

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
