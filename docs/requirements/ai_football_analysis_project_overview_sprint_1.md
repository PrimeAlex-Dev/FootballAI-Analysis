# AI Football Analysis — Tổng quan dự án & Đặc tả nghiệp vụ Sprint 1
**Project Overview & Sprint 1 Business Requirements — Version 1.0**

---

## 1. Tổng quan dự án

**AI Football Analysis** là nền tảng cung cấp dữ liệu, thống kê và phân tích chuyên sâu về các trận đấu bóng đá, hướng đến việc hỗ trợ người dùng hiểu rõ hơn về đội bóng, phong độ, lực lượng, chiến thuật và những yếu tố có thể ảnh hưởng đến diễn biến trận đấu.

> **Triết lý cốt lõi của dự án:**  
> *"AI hỗ trợ phân tích trận đấu, dự đoán thế trận – Quyền quyết định là ở bạn."*

Khác với những hệ thống dự đoán tỷ số đơn thuần, **AI Football Analysis** hướng đến việc xây dựng một chuyên gia phân tích bóng đá dựa trên dữ liệu thực tế, thông tin lịch sử và các yếu tố ảnh hưởng đến trận đấu.

* **Tầm nhìn dài hạn:** Hệ thống dự kiến cung cấp các chức năng như lịch thi đấu, thống kê bóng đá, phân tích chuyên sâu bằng AI, nhận định trước trận, bình chọn cộng đồng và kênh giao lưu giữa người dùng.
* **Trọng tâm giai đoạn đầu:** Dự án chưa tập trung vào AI hay các chức năng phía khách hàng. Thay vào đó, ưu tiên xây dựng một **nền tảng quản lý dữ liệu bóng đá có cấu trúc, ổn định, dễ mở rộng và có khả năng tái sử dụng lâu dài**. Đây là nền móng dữ liệu phục vụ toàn bộ hệ thống trong những giai đoạn phát triển tiếp theo.

---

## 2. Bối cảnh và bài học từ phiên bản trước

Dự án từng triển khai một phiên bản ban đầu nhưng không đạt hiệu quả như mong đợi.

### Các vấn đề chính
* **Quản lý dữ liệu thủ công:** Admin phải tự thu thập thông tin từ các website bóng đá, sao chép dữ liệu, xử lý và nhập vào hệ thống.
* **Phụ thuộc quá nhiều vào CRUD:** Mỗi khi cần phân tích trận đấu mới, admin thường phải tạo lại thông tin đội bóng, phong độ và dữ liệu liên quan.
* **Thiếu tính liên kết:** Thông tin giải đấu, đội bóng, trận đấu, cầu thủ và lịch sử thi đấu chưa được tổ chức thành những thực thể có quan hệ rõ ràng.
* **Thiếu khả năng tái sử dụng:** Dữ liệu của một trận đấu đã được nhập trước đó nhưng không được tận dụng hiệu quả cho những trận tiếp theo.
* **Tìm kiếm và lọc khó khăn:** Hệ thống thiếu cách tổ chức dữ liệu thống nhất, dẫn đến việc truy xuất theo đội bóng, giải đấu hoặc lịch sử trận đấu không thuận tiện.
* **Thiếu quy trình nhập liệu hàng loạt:** Admin phải thực hiện nhiều thao tác CRUD riêng lẻ, gây lãng phí thời gian và làm tăng nguy cơ sai sót.
* **Chất lượng dữ liệu không đồng đều:** Việc thu thập, kiểm tra và chuẩn hóa phụ thuộc nhiều vào thao tác và nhận định cá nhân của admin.

> **Bài học quan trọng:** Trước khi xây dựng *AI Analysis Engine*, dự án cần có một hệ thống lưu trữ và quản lý dữ liệu bóng đá đủ tốt để bảo đảm khả năng truy xuất, liên kết, kiểm chứng và tái sử dụng dữ liệu.

---

## 3. Mục tiêu Sprint 1 — Football Data Foundation

Sprint 1 tập trung xây dựng **Football Data Management System**, đóng vai trò nền tảng dữ liệu trung tâm cho AI Football Analysis.

### Mục tiêu chính
1. Xây dựng các module quản lý dữ liệu bóng đá theo từng nhóm nghiệp vụ độc lập nhưng có quan hệ chặt chẽ.
2. Tổ chức dữ liệu theo mô hình có cấu trúc, tránh lưu trữ rời rạc hoặc trùng lặp không cần thiết.
3. Quản lý xuyên suốt thông tin giải đấu, mùa giải, liên đoàn, đội bóng, cầu thủ, đội hình và trận đấu.
4. Lưu trữ lịch sử thi đấu và thống kê để phục vụ việc truy vấn, tổng hợp và phân tích trong tương lai.
5. Cung cấp các API để quản lý, tìm kiếm, lọc và truy xuất dữ liệu.
6. Xây dựng chức năng nhập dữ liệu hàng loạt từ JSON, bao gồm kiểm tra hợp lệ và tự động liên kết các thực thể.
7. Giảm tối đa việc admin phải nhập hoặc tạo lại dữ liệu đã tồn tại.
8. Thiết kế dữ liệu có khả năng mở rộng để phục vụ *AI Analysis Engine* và các chức năng khách hàng sau này.

> **Nguyên tắc quan trọng nhất của Sprint 1:**  
> Một dữ liệu đã tồn tại trong hệ thống phải có khả năng được tìm kiếm, liên kết và tái sử dụng. Không được yêu cầu admin tạo lại cùng một dữ liệu chỉ để phục vụ trận đấu mới.

---

## 4. Phạm vi Sprint 1

### 4.1. Những chức năng cần triển khai

| Module | Trách nhiệm |
| :--- | :--- |
| **Federation Management** | Quản lý liên đoàn và tổ chức bóng đá |
| **Competition Management** | Quản lý giải đấu |
| **Season Management** | Quản lý các mùa giải |
| **Team Management** | Quản lý câu lạc bộ và đội tuyển quốc gia |
| **Player Management** | Quản lý thông tin cầu thủ |
| **Squad Management** | Quản lý danh sách cầu thủ thuộc đội bóng theo từng thời điểm hoặc mùa giải |
| **Match Management** | Quản lý trận đấu, lịch thi đấu và kết quả |
| **Match Statistics** | Quản lý thống kê từng trận đấu |
| **Data Import** | Nhập và xử lý dữ liệu JSON hàng loạt |
| **Data Query & Search** | Tìm kiếm, lọc và truy vấn dữ liệu |
| **Admin Data Management** | Quản lý, cập nhật và kiểm tra dữ liệu thông qua các API quản trị |

*Lưu ý: Các module cần có trách nhiệm rõ ràng nhưng không hoạt động hoàn toàn tách biệt. Hệ thống phải duy trì được mối quan hệ giữa chúng.*

### 4.2. Những chức năng chưa triển khai
Sprint 1 chưa tập trung vào:
* AI phân tích và nhận định trận đấu.
* Dự đoán thế trận hoặc xác suất kết quả.
* Hệ thống đánh giá và đề xuất kèo.
* Giao diện và trải nghiệm phía khách hàng.
* Bình chọn cộng đồng và chat.
* Hệ thống tự động nghiên cứu tin tức bằng AI.
* Tự động thu thập dữ liệu trực tiếp từ các website.

*Tuy chưa triển khai, cấu trúc dữ liệu của Sprint 1 phải tạo điều kiện để tích hợp các chức năng này về sau mà không cần thiết kế lại toàn bộ hệ thống.*

---

## 5. Các thực thể nghiệp vụ cốt lõi

### 5.1. Federation — Liên đoàn bóng đá
* Quản lý thông tin về các liên đoàn hoặc tổ chức quản lý bóng đá (Ví dụ: `FIFA`, `UEFA`, `AFC`, `VFF`).
* **Thông tin cơ bản:** Mã định danh, tên, tên viết tắt, phạm vi quản lý, quốc gia hoặc khu vực và các mối quan hệ với tổ chức khác.
* Một liên đoàn có thể quản lý nhiều giải đấu hoặc liên kết với nhiều đội tuyển.
* Hệ thống cần phân biệt giữa liên đoàn quốc tế, châu lục và liên đoàn quốc gia. Quan hệ trực thuộc hoặc quản lý có thể có nhiều cấp, thay vì giả định tất cả đội bóng chỉ thuộc một liên đoàn duy nhất.

### 5.2. Competition — Giải đấu
* Quản lý thông tin của một giải đấu bóng đá (Ví dụ: `Premier League`, `Champions League`, `World Cup`, `V.League 1`).
* **Thông tin cơ bản:** Tên giải đấu, quốc gia hoặc khu vực, loại giải đấu, đơn vị tổ chức và trạng thái hoạt động.
* Một giải đấu có thể diễn ra qua nhiều mùa giải. Cần phân biệt rõ giữa **giải đấu** (`Premier League`) và **mùa giải** (`Premier League 2025/2026`).

### 5.3. Season — Mùa giải
* Quản lý từng mùa giải thuộc một giải đấu.
* **Thông tin cơ bản:** Tên mùa giải, thời gian bắt đầu, thời gian kết thúc và trạng thái.
* Một mùa giải có thể có nhiều đội bóng tham dự và nhiều trận đấu.
* Một đội bóng có thể tham gia nhiều giải đấu trong cùng một mùa bóng (Ví dụ: Arsenal đồng thời tham gia Premier League và Champions League). Vì vậy, không thiết kế quan hệ cố định rằng một đội bóng chỉ thuộc một giải đấu mà phải quản lý danh sách đội tham dự theo từng mùa giải cụ thể.

### 5.4. Team — Đội bóng
* Quản lý thông tin đội bóng, bao gồm câu lạc bộ và đội tuyển quốc gia.
* **Thông tin cơ bản:** Mã định danh đội bóng, tên chính thức, tên viết tắt, tên thay thế hoặc bí danh, loại đội bóng, quốc gia, liên đoàn hoặc tổ chức liên quan, các thông tin bổ sung nếu có.
* Đội bóng phải tồn tại độc lập trong hệ thống. Khi đã tạo *Arsenal*, những trận đấu tiếp theo phải sử dụng lại *Arsenal* đã tồn tại, không tạo thêm một *Arsenal* mới.

### 5.5. Player — Cầu thủ
* Quản lý thông tin định danh và hồ sơ cầu thủ.
* **Thông tin cơ bản:** Mã định danh, họ tên, ngày sinh (hoặc tuổi nếu chưa có ngày sinh), quốc tịch, vị trí thi đấu, các thông tin bổ sung nếu có.
* Cầu thủ là thực thể độc lập với đội bóng. Một cầu thủ có thể chuyển câu lạc bộ, được triệu tập lên đội tuyển quốc gia hoặc thay đổi số áo qua thời gian. Do đó, không lưu câu lạc bộ hoặc số áo dưới dạng thuộc tính cố định duy nhất của cầu thủ.

### 5.6. Squad — Danh sách cầu thủ
* Quản lý quan hệ giữa cầu thủ và đội bóng trong một khoảng thời gian hoặc mùa giải.
* **Thông tin lưu trữ:** Đội bóng sở hữu danh sách cầu thủ, mùa giải hoặc giai đoạn áp dụng, các cầu thủ thuộc squad, số áo và vị trí trong đội, thời điểm bắt đầu/kết thúc quan hệ (nếu có), và các thống kê (số trận, số phút, bàn thắng, kiến tạo, thẻ phạt).
* **Yêu cầu quan trọng:** Khi cầu thủ chuyển đội, lịch sử thi đấu tại đội bóng cũ không được mất.
* **Phân biệt Squad và Match Lineup:**
  * **Squad:** Danh sách cầu thủ thuộc đội bóng trong một giai đoạn.
  * **Match Lineup:** Đội hình ra sân và dự bị trong một trận cụ thể.
  * *Sprint 1 ưu tiên Squad; dữ liệu Match Lineup có thể được bổ sung nếu nguồn dữ liệu hỗ trợ.*

### 5.7. Match — Trận đấu
* Thực thể trung tâm của hệ thống, liên kết với: Giải đấu, Mùa giải, Đội chủ nhà, Đội khách, Thời gian thi đấu, Trạng thái trận đấu, Kết quả và Thống kê trận đấu (nếu có).
* **Các trạng thái:** `Scheduled`, `Live`, `Finished`, `Postponed`, `Cancelled` và các trạng thái cần thiết khác.
* Một trận đấu đã diễn ra phải được lưu giữ trong lịch sử. Khi cập nhật kết quả, trạng thái hoặc thống kê, hệ thống phải cập nhật đúng bản ghi trận đấu thay vì tạo một trận mới.

### 5.8. Match Statistics — Thống kê trận đấu
Quản lý các chỉ số thống kê của hai đội trong từng trận đấu. Ví dụ:

| Chỉ số | Home | Away |
| :--- | :---: | :---: |
| Ball possession | 67% | 33% |
| Total shots | 11 | 9 |
| Shots on target | 5 | 4 |
| Corner kicks | 3 | 3 |
| Fouls | 6 | 5 |

* Các chỉ số được liên kết trực tiếp với trận đấu và xác định rõ giá trị thuộc đội chủ nhà hay đội khách.
* Hệ thống cho phép lưu các nhóm thống kê khác nhau tùy theo dữ liệu được cung cấp (không phải mọi trận đấu đều có đủ tất cả chỉ số).
* Cần phân biệt giữa dữ liệu thống kê của **toàn trận**, **hiệp một** và **hiệp hai** (nếu có).

---

## 6. Mối quan hệ giữa các module

Hệ thống không được vận hành như những bảng CRUD độc lập. Các thực thể phải được liên kết để phục vụ truy vấn xuyên suốt:

| Quan hệ | Ý nghĩa |
| :--- | :--- |
| **Federation – Competition** | Liên đoàn tổ chức hoặc quản lý giải đấu |
| **Federation – Team** | Liên đoàn liên quan đến đội tuyển hoặc đội bóng |
| **Competition – Season** | Một giải đấu có nhiều mùa giải |
| **Season – Team** | Một mùa giải có nhiều đội tham dự (Nhiều-Nhiều) |
| **Team – Player** | Quan hệ thông qua Squad, có lịch sử |
| **Team – Match** | Một đội tham gia nhiều trận đấu |
| **Match – Competition/Season** | Một trận thuộc một giải đấu và mùa giải |
| **Match – Match Statistics** | Một trận có các thống kê tương ứng |
| **Player – Squad** | Một cầu thủ có thể thuộc nhiều squad qua thời gian |

### Sơ đồ quan hệ nghiệp vụ tổng quan

```text
[Federation] ──1:N──> [Competition] ──1:N──> [Season]
     │                                          │
    1:N                                        M:N (Tham dự)
     ▼                                          ▼
  [Team] <─────────────────────────────> [Team_Season]
   │  │
   │  └──1:N──> [Squad] <──N:1── [Player]
   │
  1:N (Home / Away)
   ▼
 [Match] ──1:N──> [Match Statistics]
```
*(Minh họa các liên kết chính, chưa phải ERD hoặc UML chính thức).*

### Các nguyên tắc bắt buộc
1. **Định danh ổn định:** Mỗi thực thể phải có định danh ổn định. Không sử dụng tên đội bóng hoặc tên cầu thủ làm định danh duy nhất vì tên có thể trùng hoặc thay đổi.
2. **Phản ánh lịch sử:** Quan hệ phải có khả năng phản ánh lịch sử (cầu thủ từng thi đấu cho đội nào, đội bóng từng tham dự giải nào, mùa nào).
3. **Truy vấn ngược:** Từ một đội bóng có thể tìm lịch sử trận đấu; từ một trận đấu có thể truy cập hai đội, giải đấu và thống kê.
4. **Bảo toàn lịch sử:** Không xóa hoặc ghi đè lịch sử chỉ vì thông tin hiện tại thay đổi.

---

## 7. Lịch sử trận đấu, Recent Form và Head-to-Head

Sprint 1 không xây dựng Recent Form và H2H như những dữ liệu nhập thủ công độc lập, mà phải tổng hợp từ lịch sử các trận đấu đã lưu.

### 7.1. Recent Form (Phong độ gần đây)
* Truy vấn $N$ trận đã kết thúc gần nhất của một đội theo thời gian thi đấu (Ví dụ: 5 trận gần nhất của Arsenal).
* Kết quả bao gồm: đối thủ, tỷ số, thắng/hòa/thua, ngày thi đấu, giải đấu, sân nhà/sân khách và thống kê tương ứng.
* Là cơ sở để tính toán tỷ lệ thắng, hiệu suất ghi bàn/bàn thua, số cú sút trung bình trong tương lai.

### 7.2. Head-to-Head (Lịch sử đối đầu)
* Tự động tìm những trận đấu giữa đội A và đội B, không phụ thuộc đội nào là chủ nhà.
* Hỗ trợ sắp xếp theo thời gian và giới hạn số trận.

### 7.3. Nguyên tắc độ đầy đủ
* Recent Form và H2H chỉ phản ánh những dữ liệu thực sự có trong hệ thống.
* Nếu database chỉ lưu 3 trận gần đây của một đội, hệ thống không được khẳng định đó là toàn bộ lịch sử hoặc 5 trận gần nhất trên thực tế.
* API cần trả về số lượng dữ liệu đã tìm được và metadata phù hợp để phía sử dụng biết dữ liệu có đầy đủ hay không.

---

## 8. Bulk JSON Import — Chức năng trọng tâm

Hệ thống hỗ trợ nhập một hoặc nhiều đối tượng thông qua JSON, sau đó tự kiểm tra, đối chiếu và thực hiện các thao tác dữ liệu cần thiết thay cho các thao tác CRUD thủ công lặp lại.

### 8.1. Quy trình nhập dữ liệu
```text
Admin cung cấp JSON 
  ──> Kiểm tra cấu trúc và dữ liệu (Validation)
  ──> Đối chiếu bản ghi đã tồn tại (Duplicate Detection)
  ──> Xác định: Tạo mới / Cập nhật / Bỏ qua / Báo lỗi
  ──> Lưu dữ liệu và thiết lập quan hệ (Auto-Resolution)
  ──> Trả về báo cáo import (Error & Summary Report)
```

### 8.2. Các yêu cầu bắt buộc
* **Validation:** Kiểm tra cấu trúc JSON, trường bắt buộc, kiểu dữ liệu, giá trị hợp lệ và các quan hệ cần thiết.
* **Duplicate Detection:** Nhận diện dữ liệu đã tồn tại dựa trên ID nội bộ, ID nguồn (`externalId`) hoặc các thông tin định danh phù hợp (không chỉ dựa vào tên).
* **Automatic Relationship Resolution:** Tự động tìm và liên kết các thực thể liên quan (đội bóng, cầu thủ, squad) thay vì yêu cầu admin nhập ID nội bộ.
* **Upsert:** Tạo mới nếu chưa tồn tại hoặc cập nhật bản ghi tương ứng theo quy tắc rõ ràng. Không tự ý ghi đè dữ liệu đáng tin cậy bằng dữ liệu thiếu hoặc không xác minh được.
* **Bulk Processing:** Hỗ trợ nhiều bản ghi trong một lần import, xử lý được các bản ghi có quan hệ phụ thuộc trong cùng một lô.
* **Import Preview:** Cho phép kiểm tra trước những thay đổi dự kiến, đặc biệt khi có nguy cơ trùng hoặc ghi đè dữ liệu.
* **Error Reporting:** Trả về chi tiết bản ghi thành công, thất bại, bị bỏ qua và nguyên nhân cụ thể.
* **Idempotency:** Khi nhập lại cùng một JSON, hệ thống không tạo thêm bản ghi trùng lặp.

### 8.3. Ví dụ JSON nhập trận đấu

```json
{
  "source": "manual-extension",
  "matches": [
    {
      "externalId": "match-001",
      "competition": "Premier League",
      "season": "2025/2026",
      "homeTeam": "Arsenal",
      "awayTeam": "Chelsea",
      "date": "2026-02-01T16:30:00Z",
      "status": "FINISHED",
      "score": {
        "home": 2,
        "away": 1
      },
      "statistics": {
        "shotsOnTarget": {
          "home": 6,
          "away": 4
        }
      }
    }
  ]
}
```
*(Dữ liệu minh họa cấu trúc import. Nếu không đủ thông tin để xác định một đội bóng hoặc giải đấu, hệ thống cần báo trường hợp chưa rõ thay vì tự động liên kết sai).*

### 8.4. Tương thích với Football Extractor
* Hỗ trợ dữ liệu JSON xuất từ Chrome Extension đã phát triển trước đó (gồm định danh trận, hai đội, nhóm thống kê, danh sách cầu thủ theo vị trí, thống kê squad, URL nguồn và thời điểm thu thập).
* Hệ thống cần có bước chuyển đổi (Adapter/Mapper) dữ liệu đầu vào sang cấu trúc thống nhất trước khi lưu để dễ dàng mở rộng cho các nguồn dữ liệu khác trong tương lai.

---

## 9. Data Search & Query

Tất cả module cần hỗ trợ tìm kiếm và lọc nhất quán:

| Đối tượng | Khả năng truy vấn |
| :--- | :--- |
| **Federation** | Theo tên, quốc gia, khu vực |
| **Competition** | Theo tên, quốc gia, liên đoàn, loại giải |
| **Season** | Theo giải đấu, mùa, trạng thái |
| **Team** | Theo tên, tên thay thế, quốc gia, loại đội |
| **Player** | Theo tên, quốc tịch, đội bóng, vị trí |
| **Squad** | Theo đội, mùa giải, vị trí cầu thủ |
| **Match** | Theo đội, giải đấu, mùa, ngày, trạng thái |
| **Statistics** | Theo trận đấu, đội bóng và loại chỉ số |

* Các API danh sách cần hỗ trợ **phân trang (pagination)**, **sắp xếp (sorting)** và **kết hợp nhiều điều kiện lọc**.
* Hỗ trợ truy vấn nghiệp vụ: tìm các đội tham gia một mùa giải, tìm các cầu thủ từng thuộc một đội, lấy lịch sử trận đấu của đội bóng, Recent Form và H2H.

---

## 10. Yêu cầu quản lý chất lượng dữ liệu

1. **Tính nhất quán:** Các bản ghi không được tồn tại trong trạng thái liên kết sai hoặc mâu thuẫn quy tắc nghiệp vụ.
2. **Tính truy xuất nguồn gốc:** Lưu nguồn nhập dữ liệu, thời điểm và phương thức nhập khi phù hợp.
3. **Tính toàn vẹn lịch sử:** Cập nhật dữ liệu hiện tại không làm mất thông tin lịch sử có giá trị.
4. **Tính chống trùng lặp:** Hạn chế tối đa việc tạo nhiều thực thể đại diện cho cùng một đối tượng.
5. **Tính linh hoạt:** Cho phép thiếu các chỉ số không bắt buộc, nhưng phải phân biệt rõ giữa `chưa có dữ liệu (null)`, `không áp dụng (N/A)` và `giá trị bằng 0`.
6. **Tính kiểm tra được:** Phát hiện và báo cáo dữ liệu không hợp lệ, không rõ nguồn hoặc không thể xác định quan hệ.
7. **Tính mở rộng:** Tiếp nhận thêm giải đấu, mùa giải, đội bóng, loại thống kê và nguồn dữ liệu mà không phá vỡ cấu trúc cũ.

---

## 11. Thiết kế phạm vi API theo nghiệp vụ

| Nhóm API | Chức năng dự kiến |
| :--- | :--- |
| **Federation API** | Tạo, cập nhật, xem, tìm kiếm liên đoàn |
| **Competition API** | Quản lý giải đấu và mùa giải |
| **Team API** | Quản lý đội bóng, thông tin và các quan hệ |
| **Player API** | Quản lý cầu thủ |
| **Squad API** | Quản lý danh sách cầu thủ theo đội và giai đoạn |
| **Match API** | Tạo, cập nhật, tìm kiếm trận đấu và kết quả |
| **Statistics API** | Quản lý và truy vấn thống kê trận |
| **History API** | Lấy lịch sử trận đấu, Recent Form và H2H |
| **Import API** | Kiểm tra, xem trước, nhập JSON và xem kết quả xử lý |

* Cần có quy tắc phản hồi (Response/Error Contract) thống nhất cho lỗi validation, dữ liệu trùng, không tìm thấy đối tượng và xung đột quan hệ.
* Chức năng quản trị và cập nhật dữ liệu phải được kiểm soát quyền truy cập (Authentication & Authorization).

---

## 12. Luồng vận hành admin kỳ vọng

**Ví dụ thực tế:** Admin cần bổ sung dữ liệu trận *Vietnam vs Chinese Taipei*.

1. **Thu thập:** Admin thu thập *Stats* bằng extension và thu thập *Squad* của hai đội (nếu hệ thống chưa có hoặc cần cập nhật).
2. **Import:** Admin gửi các tệp JSON vào chức năng Import.
3. **Xử lý tự động:**
   * Hệ thống kiểm tra *Vietnam* và *Chinese Taipei* đã tồn tại hay chưa $\rightarrow$ Tái sử dụng nếu đã có.
   * Tìm trận đấu theo định danh, xác định dữ liệu cần bổ sung và liên kết thống kê tương ứng.
   * Nếu cầu thủ đã có hồ sơ $\rightarrow$ Cập nhật quan hệ squad hoặc thống kê phù hợp thay vì tạo mới.
4. **Kết quả:** Trận đấu được lưu vào lịch sử. Trong tương lai, khi *Vietnam* thi đấu với đội khác, hệ thống tự động sử dụng trận đấu này để tính *Recent Form* mà admin không cần nhập lại.

---

## 13. Tiêu chí hoàn thành Sprint 1 (Definition of Done)

- [ ] Quản lý được các thực thể: `Federation`, `Competition`, `Season`, `Team`, `Player`, `Squad`, `Match` và `Match Statistics`.
- [ ] Các quan hệ giữa thực thể được lưu trữ rõ ràng và truy vấn được.
- [ ] Một đội bóng có thể tham gia nhiều giải đấu, nhiều mùa giải mà không bị tạo trùng.
- [ ] Cầu thủ có thể thay đổi đội bóng nhưng vẫn bảo toàn lịch sử.
- [ ] Trận đấu được liên kết chính xác với giải đấu, mùa giải, đội nhà và đội khách.
- [ ] Hệ thống lưu được kết quả và thống kê trận đấu.
- [ ] Có API tìm kiếm, lọc, phân trang và truy vấn theo quan hệ.
- [ ] Có khả năng truy vấn Recent Form và H2H từ lịch sử đã lưu.
- [ ] Có chức năng nhập JSON hàng loạt và kiểm tra hợp lệ.
- [ ] Import có khả năng phát hiện trùng và tái sử dụng bản ghi cũ.
- [ ] Nhập lại cùng dữ liệu không tạo các bản ghi trùng lặp (Idempotency).
- [ ] Hệ thống báo cáo rõ những bản ghi import thành công, thất bại hoặc cần kiểm tra.
- [ ] Dữ liệu từ *Football Extractor* có thể được chuyển đổi và nhập vào hệ thống.
- [ ] Admin không phải tạo lại đội bóng, cầu thủ hoặc lịch sử trận đấu đã tồn tại khi bổ sung trận đấu mới.

---

## 14. Những vấn đề sẽ được thiết kế chi tiết sau Sprint 1 Overview

Các quyết định kỹ thuật chi tiết sẽ được làm rõ ở giai đoạn thiết kế tiếp theo sau khi thống nhất nghiệp vụ:
* Danh sách field cụ thể của từng entity.
* Quan hệ và multiplicity trong ERD.
* Định danh và cơ chế nhận diện trùng giữa nhiều nguồn dữ liệu.
* Quy tắc tạo, cập nhật và xử lý xung đột khi import.
* Phân biệt thống kê cầu thủ theo mùa giải, giải đấu và toàn bộ mùa bóng.
* Quy định phiên bản dữ liệu, lịch sử thay đổi và nguồn gốc.
* Quy tắc xử lý trận hoãn, trận hủy, trận đấu lại hoặc các trường hợp đặc biệt.
* Cấu trúc request/response và error contract.
* Phân quyền và nhật ký thao tác quản trị (Audit logs).
* Phương án kiểm thử và đánh giá dữ liệu.

---

## 15. Định hướng phát triển sau Sprint 1

Sau khi hoàn thành **Football Data Foundation**, hệ thống sẽ tiến tới các giai đoạn tiếp theo:
1. **Giai đoạn AI:** Xây dựng *AI Analysis Engine* tổng hợp dữ liệu thống kê, nghiên cứu thông tin bổ sung từ Internet, phân tích các yếu tố ảnh hưởng và tạo nhận định có căn cứ.
2. **Giai đoạn khách hàng:** Phát triển giao diện lịch thi đấu, trang chi tiết trận, biểu đồ thống kê, báo cáo AI, bình chọn và chat cộng đồng.
3. **Giai đoạn tự động hóa mở rộng:** Tiến tới tự động thu thập, đồng bộ và cập nhật dữ liệu theo lịch khi có nguồn dữ liệu và quyền sử dụng phù hợp.

---

## 16. Kết luận

Sprint 1 không đặt mục tiêu xây dựng một hệ thống CRUD bóng đá đơn thuần, mà hướng tới một **Football Data Foundation** vững chắc:
* Dữ liệu được tổ chức thành các thực thể có quan hệ chặt chẽ.
* Lịch sử được lưu trữ và tái sử dụng lâu dài.
* Thao tác nhập dữ liệu được tự động hóa tối đa thông qua JSON.
* Các API cung cấp khả năng truy vấn, tìm kiếm và tổng hợp hiệu quả.
* Kiến trúc nghiệp vụ sẵn sàng phục vụ AI và các chức năng khách hàng trong tương lai.

> **Nguyên tắc phát triển xuyên suốt:**  
> **"Collect Once, Store Properly, Reuse Everywhere."**  
> *(Thu thập một lần, lưu trữ đúng cấu trúc, tái sử dụng ở mọi nơi cần thiết).*