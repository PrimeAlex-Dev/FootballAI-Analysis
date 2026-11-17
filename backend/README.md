# AI Football Analysis — Backend Foundation

## Phạm vi hiện tại

Java 21, Spring Boot 4.1.2, Maven Wrapper 3.9.13 (wrapper plugin 3.3.4, only-script). Giữ groupId com.alexstudio.ai và artifactId footballAI-backend từ skeleton ban đầu.

- Spring MVC: khởi động HTTP server cho backend.
- Actuator: cung cấp GET /actuator/health, chỉ expose health và không hiển thị chi tiết.
- Spring Boot Starter Test: kiểm tra ứng dụng khởi động trên cổng ngẫu nhiên, health trả HTTP 200 và status UP.

Chưa có database, JPA, Flyway, API nghiệp vụ, authentication, CORS hoặc Reference Module được duyệt. Foundation chưa phải cấu hình production hoàn chỉnh. Dockerfile còn là placeholder.

## Yêu cầu

Cài JDK 21 và cấu hình JAVA_HOME trỏ đến JDK. Lần đầu cần Internet tải Maven và dependency; không cần cài Maven riêng. Wrapper được sinh bằng Maven Wrapper Plugin chính thức.

## Build và test

Trong thư mục backend, PowerShell:

```powershell
.\mvnw.cmd --version
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

Linux/macOS:

```sh
sh ./mvnw --version
sh ./mvnw --batch-mode --no-transfer-progress clean verify
```

Báo cáo test nằm ở target/surefire-reports/. JAR thực thi được tạo tại target/footballAI-backend-1.0-SNAPSHOT.jar.

## Chạy và kiểm tra HTTP

```powershell
.\mvnw.cmd spring-boot:run
```

Hoặc sau khi build:

```sh
java -jar target/footballAI-backend-1.0-SNAPSHOT.jar
```

Trong terminal khác:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Kỳ vọng HTTP 200 và JSON status UP. Dừng bằng Ctrl+C. Health hiện chỉ xác nhận ứng dụng đang chạy, chưa kiểm tra database. Đường dẫn / chưa có controller nên có thể trả 404.

## Cấu hình môi trường

application.properties chứa cấu hình chung; SERVER_PORT mặc định 8080. Spring Boot đọc biến môi trường của tiến trình, không tự nạp file .env root. .env.example chỉ là tài liệu mẫu.

Ví dụ đổi cổng trong PowerShell:

```powershell
$env:SERVER_PORT = '8081'
.\mvnw.cmd spring-boot:run
```

Hoặc Linux/macOS: SERVER_PORT=8081 sh ./mvnw spring-boot:run. Chưa tạo profile local/test/production rỗng khi chưa có khác biệt cấu hình; các profile database sẽ được thiết kế ở task persistence.

## Quy tắc

Theo [AGENTS.md](../AGENTS.md), [Tech Stack](../docs/architecture/AI_Football_Analysis_Sprint_1_Tech_Stack.md) và [Coding Standards](../docs/standards/AI_Football_Analysis_Sprint_1_Coding_Standards.md). Chưa thêm các package controller/service/repository/entity rỗng trước khi có module nghiệp vụ.

Nguồn chọn cấu hình: [Spring Boot requirements](https://docs.spring.io/spring-boot/system-requirements.html), [Actuator HTTP](https://docs.spring.io/spring-boot/reference/actuator/monitoring.html), [Maven Wrapper](https://maven.apache.org/wrapper/).
