# FU Car Renting System

FU Car Renting System là hệ thống quản lý thuê xe được xây dựng theo kiến trúc Microservices bằng Spring Boot, Spring Cloud Gateway, Netflix Eureka, OpenFeign, PostgreSQL và Docker Compose.

Hệ thống cung cấp API cho hai nhóm người dùng:

- Admin: quản lý Customer, Manufacturer, Supplier, Car và xem báo cáo thuê xe.
- Customer: đăng ký, đăng nhập, quản lý hồ sơ, thuê xe và xem lịch sử giao dịch.

Dự án chỉ bao gồm backend API, không bao gồm frontend.

---

## 1. Công nghệ sử dụng

- Java 21
- Spring Boot 4.1.0
- Spring Cloud 5.0.2
- Spring Cloud Gateway WebFlux
- Netflix Eureka
- OpenFeign
- Spring Security
- JWT HS256
- Spring Data JPA
- PostgreSQL 17
- Springdoc OpenAPI
- Swagger UI
- Docker
- Docker Compose
- Maven
- Lombok

---

## 2. Kiến trúc hệ thống

```text
Postman / Swagger UI
          |
          v
   API Gateway :8080
          |
          +-----------------------------+
          |              |              |
          v              v              v
 Customer Service    Car Service    Renting Service
      :8081             :8082            :8083
          |              |                |
          v              v                v
    customer_db        car_db          rental_db

Renting Service gọi Customer Service và Car Service
thông qua OpenFeign và Netflix Eureka.

Tất cả application service đăng ký với Eureka Server :8761.
```

API Gateway là đầu vào chính của hệ thống. Client không gọi trực tiếp các microservice nghiệp vụ.

---

## 3. Các module

| Module | Chức năng | Cổng |
|---|---|---:|
| `eureka-server` | Service Discovery | 8761 |
| `api-gateway` | Routing, xác thực JWT và phân quyền | 8080 |
| `customer-service` | Authentication và quản lý Customer | 8081 |
| `car-service` | Quản lý Car, Manufacturer và Supplier | 8082 |
| `renting-service` | Giao dịch thuê, lịch sử và báo cáo | 8083 |

Khi chạy bằng Docker, các cổng `8081`, `8082` và `8083` chỉ được sử dụng trong Docker network.

Client truy cập hệ thống thông qua:

```text
http://localhost:8080
```

---

## 4. Database per Service

Mỗi microservice nghiệp vụ sở hữu database riêng.

| Service | Database | Cổng trên máy host |
|---|---|---:|
| Customer Service | `customer_db` | 5433 |
| Car Service | `car_db` | 5434 |
| Renting Service | `rental_db` | 5435 |

Nguyên tắc triển khai:

- Customer Service chỉ truy cập `customer_db`.
- Car Service chỉ truy cập `car_db`.
- Renting Service chỉ truy cập `rental_db`.
- Không sử dụng quan hệ JPA giữa các database.
- Renting Service lấy thông tin Customer và Car thông qua OpenFeign.
- Không truy cập trực tiếp database của service khác.

---

## 5. Service Discovery với Netflix Eureka

Eureka Server chạy tại:

```text
http://localhost:8761
```

Các application đăng ký với Eureka gồm:

```text
API-GATEWAY
CUSTOMER-SERVICE
CAR-SERVICE
RENTING-SERVICE
```

API Gateway định tuyến bằng tên service:

```text
lb://customer-service
lb://car-service
lb://renting-service
```

Renting Service sử dụng OpenFeign:

```java
@FeignClient(name = "customer-service")
```

```java
@FeignClient(name = "car-service")
```

Nhờ đó các service không cần sử dụng địa chỉ IP hoặc cổng cố định để gọi nhau.

---

## 6. Cấu trúc Docker

Docker Compose khởi động tổng cộng 8 container:

```text
eureka-server
api-gateway
customer-service
car-service
renting-service
customer-db
car-db
rental-db
```

Các database có health check để application service chỉ khởi động sau khi PostgreSQL sẵn sàng.

---

## 7. Yêu cầu trước khi chạy

Máy cần cài đặt:

- Docker Desktop
- Docker Compose
- Git nếu tải dự án từ repository

Không bắt buộc cài Java, Maven hoặc IntelliJ để chạy hệ thống bằng Docker.

Các cổng sau phải chưa bị ứng dụng khác sử dụng:

```text
8080
8761
5433
5434
5435
```

---

## 8. Chạy hệ thống trên máy mới

### Bước 1: Mở Docker Desktop

Chờ Docker Engine chạy hoàn toàn.

### Bước 2: Mở terminal tại thư mục dự án

Di chuyển vào thư mục chứa file `docker-compose.yml`.

Ví dụ:

```powershell
cd E:\Ky8\MSS301\mss301-assignment1\fu_car_renting_system_andche186895
```

### Bước 3: Tạo file môi trường

Nếu repository có `.env.example`, chạy:

```powershell
Copy-Item .env.example .env
```

### Bước 4: Build và khởi động toàn bộ hệ thống

Lần chạy đầu tiên:

```powershell
docker compose up -d --build
```

Lệnh này sẽ:

- Build image của các Spring Boot service.
- Tạo Docker network.
- Tạo ba PostgreSQL database.
- Khởi động Eureka Server.
- Khởi động các microservice.
- Khởi động API Gateway.

### Bước 5: Kiểm tra container

```powershell
docker compose ps
```

Kết quả cần có:

```text
api-gateway        Running
eureka-server      Running
customer-service   Running
car-service        Running
renting-service    Running
customer-db        Healthy
car-db             Healthy
rental-db          Healthy
```

Các application service có thể cần một khoảng thời gian ngắn để đăng ký với Eureka.

### Những lần chạy sau

Nếu không sửa code:

```powershell
docker compose up -d
```

Nếu có sửa code:

```powershell
docker compose up -d --build
```

---

## 9. Dừng hệ thống

Dừng container nhưng giữ lại dữ liệu database:

```powershell
docker compose down
```

Dừng container và xóa toàn bộ Docker volume:

```powershell
docker compose down -v
```

Lưu ý: `docker compose down -v` sẽ xóa toàn bộ Customer, Car và Renting Transaction đã tạo.

---

## 10. Xem log

Xem log toàn hệ thống:

```powershell
docker compose logs -f
```

Xem log riêng từng service:

```powershell
docker compose logs -f eureka-server
```

```powershell
docker compose logs -f api-gateway
```

```powershell
docker compose logs -f customer-service
```

```powershell
docker compose logs -f car-service
```

```powershell
docker compose logs -f renting-service
```

Xem 200 dòng log gần nhất:

```powershell
docker compose logs --tail=200
```

---

## 11. Các địa chỉ kiểm tra

### API Gateway

```text
http://localhost:8080
```

### Gateway Health

```text
http://localhost:8080/actuator/health
```

Kết quả:

```json
{
  "status": "UP"
}
```

### Eureka Dashboard

```text
http://localhost:8761
```

### Swagger UI

```text
http://localhost:8080/swagger-ui.html
```

Trình duyệt có thể tự chuyển sang:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## 12. OpenAPI Documentation

Swagger UI tập trung tại API Gateway và cung cấp tài liệu cho ba microservice.

### Customer Service OpenAPI

```text
http://localhost:8080/customer-service/v3/api-docs
```

### Car Service OpenAPI

```text
http://localhost:8080/car-service/v3/api-docs
```

### Renting Service OpenAPI

```text
http://localhost:8080/renting-service/v3/api-docs
```

Trong Swagger UI có thể chọn:

```text
Customer Service
Car Service
Renting Service
```

---

## 13. Tài khoản Admin mặc định

Tài khoản Admin được cấu hình trong `application.yml` hoặc biến môi trường.

```text
Email: admin@example.com
Password: admin
```

Admin không cần tồn tại trong bảng `customers`.

Customer không được tạo sẵn khi database mới được khởi tạo. Customer có thể:

- Tự đăng ký bằng `/customers/register`.
- Hoặc được Admin tạo bằng `/admin/customers`.

---

## 14. Authentication và JWT

### Đăng nhập Admin

```http
POST http://localhost:8080/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@example.com",
  "password": "admin"
}
```

Kết quả:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "role": "ADMIN",
  "customerId": null,
  "email": "admin@example.com"
}
```

### Đăng nhập Customer

```http
POST http://localhost:8080/auth/login
Content-Type: application/json
```

```json
{
  "email": "customer@example.com",
  "password": "123456"
}
```

Kết quả:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "role": "CUSTOMER",
  "customerId": 1,
  "email": "customer@example.com"
}
```

Các API được bảo vệ phải gửi:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

Client không được tự gửi:

```text
X-Customer-Id
X-Role
```

API Gateway đọc `customerId` và `role` từ JWT rồi tự tạo các header nội bộ này trước khi chuyển request đến microservice.

---

## 15. Sử dụng JWT trong Swagger

Để gọi API được bảo vệ bằng Swagger:

1. Chọn `Customer Service`.
2. Gọi `POST /auth/login`.
3. Copy giá trị `accessToken`.
4. Nhấn nút `Authorize`.
5. Dán token.
6. Nhấn `Authorize`.
7. Chuyển sang service cần kiểm thử.
8. Gọi API.

Chỉ dán token:

```text
eyJhbGciOiJIUzI1NiJ9...
```

Không cần tự thêm chữ `Bearer`, vì Swagger tự thêm vào request.

---

## 16. Danh sách API

### Authentication và Customer

| Method | Endpoint | Quyền |
|---|---|---|
| POST | `/auth/login` | Public |
| POST | `/customers/register` | Public |
| GET | `/customers/me` | Customer |
| PUT | `/customers/me` | Customer |
| POST | `/admin/customers` | Admin |
| GET | `/admin/customers` | Admin |
| GET | `/admin/customers/{id}` | Admin |
| PUT | `/admin/customers/{id}` | Admin |
| DELETE | `/admin/customers/{id}` | Admin |

Xóa Customer là xóa mềm bằng cách chuyển trạng thái sang:

```text
INACTIVE
```

Customer ở trạng thái `INACTIVE`:

- Vẫn còn trong database.
- Không thể đăng nhập.
- Không đủ điều kiện thuê xe.
- Vẫn giữ lịch sử giao dịch.
- Có thể được Admin chuyển lại thành `ACTIVE`.

---

### Manufacturer

| Method | Endpoint | Quyền |
|---|---|---|
| GET | `/manufacturers` | Authenticated |
| POST | `/manufacturers` | Admin |

---

### Supplier

| Method | Endpoint | Quyền |
|---|---|---|
| GET | `/suppliers` | Authenticated |
| POST | `/suppliers` | Admin |

---

### Car

| Method | Endpoint | Quyền |
|---|---|---|
| GET | `/cars` | Authenticated |
| GET | `/cars/available` | Authenticated |
| GET | `/cars/{id}` | Authenticated |
| POST | `/cars` | Admin |
| PUT | `/cars/{id}` | Admin |
| DELETE | `/cars/{id}` | Admin |

Xóa Car là xóa mềm bằng cách chuyển trạng thái xe sang:

```text
INACTIVE
```

Xe `INACTIVE` không xuất hiện trong danh sách xe có thể thuê.

---

### Renting

| Method | Endpoint | Quyền |
|---|---|---|
| POST | `/rentings` | Customer |
| GET | `/rentings/history` | Customer |
| GET | `/rentings/{id}` | Customer |
| GET | `/rentings/reports` | Admin |

---

## 17. Quy trình kiểm thử từ database trắng

Sau khi chạy:

```powershell
docker compose down -v
docker compose up -d --build
```

Thực hiện theo thứ tự:

```text
1. Đăng nhập Admin
2. Tạo Manufacturer
3. Tạo Supplier
4. Tạo Car
5. Đăng ký hoặc tạo Customer
6. Đăng nhập Customer
7. Xem danh sách xe khả dụng
8. Tạo giao dịch thuê
9. Xem lịch sử thuê
10. Xem chi tiết giao dịch
11. Đăng nhập Admin
12. Xem báo cáo
```

Có thể thực hiện bằng:

- Swagger UI.
- Postman.
- PowerShell.
- curl.

---

## 18. Đăng ký Customer

```http
POST http://localhost:8080/customers/register
Content-Type: application/json
```

```json
{
  "customerName": "Nguyen Van An",
  "telephone": "0901234567",
  "email": "customer@example.com",
  "customerBirthday": "2002-01-01",
  "password": "123456"
}
```

Kết quả thành công:

```json
{
  "customerId": 1,
  "customerName": "Nguyen Van An",
  "telephone": "0901234567",
  "email": "customer@example.com",
  "customerBirthday": "2002-01-01",
  "customerStatus": "ACTIVE"
}
```

---

## 19. Admin tạo Customer

```http
POST http://localhost:8080/admin/customers
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json
```

```json
{
  "customerName": "Nguyen Van Test",
  "telephone": "0901234567",
  "email": "test@example.com",
  "customerBirthday": "2002-01-01",
  "password": "123456"
}
```

Customer mới có trạng thái mặc định:

```text
ACTIVE
```

---

## 20. Admin thay đổi trạng thái Customer

```http
PUT http://localhost:8080/admin/customers/1
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json
```

Khóa Customer:

```json
{
  "customerStatus": "INACTIVE"
}
```

Mở lại Customer:

```json
{
  "customerStatus": "ACTIVE"
}
```

Xóa mềm:

```http
DELETE http://localhost:8080/admin/customers/1
Authorization: Bearer <ADMIN_TOKEN>
```

Kết quả:

```text
204 No Content
```

Sau khi xóa mềm, Customer có trạng thái `INACTIVE`.

---

## 21. Tạo giao dịch thuê

```http
POST http://localhost:8080/rentings
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json
```

```json
{
  "details": [
    {
      "carId": 1,
      "startDate": "2027-02-10",
      "endDate": "2027-02-12"
    }
  ]
}
```

Không gửi:

```text
customerId
X-Customer-Id
X-Role
```

API Gateway tự lấy Customer ID từ JWT.

Kết quả thành công:

```json
{
  "rentingTransactionId": 1,
  "rentingDate": "2026-07-18T09:00:00",
  "totalPrice": 2550000.00,
  "customerId": 1,
  "rentingStatus": "COMPLETED",
  "details": [
    {
      "carId": 1,
      "startDate": "2027-02-10",
      "endDate": "2027-02-12",
      "price": 2550000.00
    }
  ]
}
```

Số ngày thuê được tính bao gồm ngày bắt đầu và ngày kết thúc:

```text
Số ngày thuê = EndDate - StartDate + 1
```

Ví dụ:

```text
10/02 đến 12/02 = 3 ngày
```

---

## 22. Các kiểm tra nghiệp vụ thuê xe

Hệ thống kiểm tra:

- Customer ID phải hợp lệ.
- Customer phải có trạng thái `ACTIVE`.
- Request phải có ít nhất một Renting Detail.
- Một Car không được xuất hiện nhiều lần trong cùng transaction.
- `startDate` không được ở quá khứ.
- `endDate` không được trước `startDate`.
- Car phải tồn tại.
- Car phải có trạng thái `AVAILABLE`.
- Giá thuê mỗi ngày phải lớn hơn 0.
- Car không được trùng lịch với giao dịch đang hoạt động.
- Tổng giá được tính ở backend.
- Customer chỉ xem được giao dịch của chính mình.

---

## 23. Xem lịch sử thuê

```http
GET http://localhost:8080/rentings/history
Authorization: Bearer <CUSTOMER_TOKEN>
```

Chỉ các giao dịch có trạng thái:

```text
COMPLETED
```

được trả về.

Danh sách được sắp xếp theo `rentingDate` giảm dần.

---

## 24. Xem chi tiết giao dịch

```http
GET http://localhost:8080/rentings/1
Authorization: Bearer <CUSTOMER_TOKEN>
```

Customer chỉ được xem giao dịch thuộc về chính mình.

Nếu truy cập giao dịch của Customer khác, hệ thống trả:

```text
403 Forbidden
```

hoặc phản hồi tương ứng theo cấu hình hiện tại.

---

## 25. Báo cáo Admin

```http
GET http://localhost:8080/rentings/reports?startDate=2026-07-01&endDate=2027-12-31
Authorization: Bearer <ADMIN_TOKEN>
```

Kết quả:

```json
{
  "startDate": "2026-07-01",
  "endDate": "2027-12-31",
  "totalTransactions": 1,
  "totalRevenue": 2550000.00,
  "transactions": [
    {
      "rentingTransactionId": 1,
      "rentingDate": "2026-07-18T09:00:00",
      "totalPrice": 2550000.00,
      "customerId": 1,
      "rentingStatus": "COMPLETED",
      "details": []
    }
  ]
}
```

Báo cáo:

- Chỉ lấy giao dịch `COMPLETED`.
- Lọc theo `rentingDate`.
- Bao gồm toàn bộ ngày `endDate`.
- Sắp xếp `rentingDate` giảm dần.
- Tính tổng số giao dịch.
- Tính tổng doanh thu.

Khoảng thời gian được xử lý theo nguyên tắc:

```text
startDate 00:00 <= rentingDate < ngày sau endDate 00:00
```

---

## 26. Saga Orchestration

Renting Service đóng vai trò Saga Orchestrator trong quy trình tạo giao dịch thuê.

Luồng thành công:

```text
PENDING
   |
   v
EXECUTING
   |
   v
COMPLETED
```

Luồng thất bại:

```text
PENDING
   |
   v
EXECUTING
   |
   v
FAILED
   |
   v
COMPENSATING
   |
   v
COMPENSATED
```

Quy trình tạo giao dịch:

1. Tạo Renting Transaction với trạng thái `PENDING`.
2. Chuyển transaction sang `EXECUTING`.
3. Kiểm tra Customer thông qua Customer Service.
4. Kiểm tra Car thông qua Car Service.
5. Kiểm tra trạng thái Car.
6. Kiểm tra trùng lịch thuê.
7. Tính giá từng Renting Detail.
8. Tính tổng tiền.
9. Lưu Renting Detail.
10. Chuyển transaction sang `COMPLETED`.
11. Nếu xảy ra lỗi, chuyển sang luồng xử lý thất bại và bù trừ.

Đây là Saga Orchestration dạng đơn giản, trong đó Renting Service điều phối và quản lý trạng thái của quy trình thuê xe.

---

## 27. Phân quyền

| Chức năng | Public | Customer | Admin |
|---|:---:|:---:|:---:|
| Đăng nhập | ✅ | ✅ | ✅ |
| Đăng ký Customer | ✅ | ✅ | ✅ |
| Xem hồ sơ cá nhân | ❌ | ✅ | ❌ |
| Sửa hồ sơ cá nhân | ❌ | ✅ | ❌ |
| Tạo giao dịch thuê | ❌ | ✅ | ❌ |
| Xem lịch sử thuê | ❌ | ✅ | ❌ |
| Quản lý Customer | ❌ | ❌ | ✅ |
| Tạo Manufacturer | ❌ | ❌ | ✅ |
| Tạo Supplier | ❌ | ❌ | ✅ |
| Tạo, sửa và xóa Car | ❌ | ❌ | ✅ |
| Xem báo cáo | ❌ | ❌ | ✅ |

---

## 28. Mã trạng thái HTTP

| Mã | Ý nghĩa |
|---:|---|
| 200 | Request thành công |
| 201 | Tạo dữ liệu thành công |
| 204 | Xử lý thành công và không có response body |
| 400 | Request hoặc dữ liệu không hợp lệ |
| 401 | Chưa đăng nhập hoặc JWT không hợp lệ |
| 403 | Không đủ quyền hoặc tài khoản bị khóa |
| 404 | Không tìm thấy dữ liệu |
| 409 | Xung đột dữ liệu, xe không khả dụng hoặc trùng lịch |
| 502 | Service nội bộ trả về phản hồi không hợp lệ |
| 503 | Service nội bộ không hoạt động |

---

## 29. Kiểm tra bảo mật nhanh

Gọi API được bảo vệ mà không có token:

```powershell
curl.exe -i http://localhost:8080/cars/available
```

Kết quả mong đợi:

```text
HTTP/1.1 401 Unauthorized
```

Dùng Customer token gọi API báo cáo:

```http
GET http://localhost:8080/rentings/reports?startDate=2026-01-01&endDate=2027-12-31
Authorization: Bearer <CUSTOMER_TOKEN>
```

Kết quả mong đợi:

```text
403 Forbidden
```

---

## 30. Kiểm tra nhanh bằng PowerShell

### Kiểm tra Gateway

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/actuator/health"
```

### Đăng nhập Admin

```powershell
$adminBody = @{
    email = "admin@example.com"
    password = "admin"
} | ConvertTo-Json

$adminResult = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/auth/login" `
    -ContentType "application/json" `
    -Body $adminBody

$adminResult
```

### Tạo Authorization header

```powershell
$adminHeaders = @{
    Authorization = "Bearer $($adminResult.accessToken)"
}
```

### Xem Customer

```powershell
Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/admin/customers" `
    -Headers $adminHeaders
```

---


## 31. Cấu trúc thư mục chính

```text
fu_car_renting_system_andche186895/
├── api-gateway/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── car-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── customer-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── eureka-server/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── renting-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── .env.example
├── .gitignore
├── docker-compose.yml
└── README.md
```

---

## 32. Thành viên thực hiện

```text
Họ và tên: Đinh Công An
Mã sinh viên: HE186895
Lớp: SE1912-JV
Môn học: MSS301
Assignment: Assignment 01
```