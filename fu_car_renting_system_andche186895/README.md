# FU Car Renting System

Hệ thống thuê xe được xây dựng theo kiến trúc Microservices bằng Spring Boot, Spring Cloud Gateway, Eureka Server, OpenFeign, PostgreSQL và Docker Compose.

## 1. Thông tin dự án

- Java: 21
- Spring Boot: 4.1.0
- Spring Cloud: 5.0.2
- PostgreSQL: 17
- API Gateway: Spring Cloud Gateway WebFlux
- Service Discovery: Netflix Eureka
- Giao tiếp nội bộ: OpenFeign
- Authentication: JWT HS256
- Transaction Pattern: Saga Orchestration
- Deployment: Docker Compose

## 2. Kiến trúc hệ thống

```text
Client / Postman
        |
        v
API Gateway :8080
        |
        +-----------------------+
        |                       |
        v                       v
Customer Service           Car Service
     :8081                    :8082
        |                       |
        v                       v
 customer_db                 car_db
        |
        +-----------------------+
                    |
                    v
             Renting Service
                  :8083
                    |
                    v
                rental_db

Tất cả service đăng ký với Eureka Server :8761
```

## 3. Các module

| Module | Chức năng | Cổng nội bộ |
|---|---|---:|
| `eureka-server` | Service Discovery | 8761 |
| `api-gateway` | Routing, JWT, phân quyền | 8080 |
| `customer-service` | Customer, đăng nhập, hồ sơ | 8081 |
| `car-service` | Xe, hãng xe, nhà cung cấp | 8082 |
| `renting-service` | Giao dịch thuê, lịch sử, báo cáo | 8083 |

Client chỉ truy cập thông qua:

```text
http://localhost:8080
```

Eureka Dashboard:

```text
http://localhost:8761
```

## 4. Database per Service

| Service | Database | Cổng host |
|---|---|---:|
| Customer Service | `customer_db` | 5433 |
| Car Service | `car_db` | 5434 |
| Renting Service | `rental_db` | 5435 |

Mỗi microservice sở hữu database riêng. Không sử dụng quan hệ JPA giữa các database.

## 5. Chạy hệ thống bằng Docker

### Yêu cầu

- Docker Desktop
- Docker Compose
- Các cổng `8080`, `8761`, `5433`, `5434`, `5435` chưa bị ứng dụng khác sử dụng

### Khởi động

Mở PowerShell tại thư mục dự án:

```powershell
cd E:\Ky8\MSS301\mss301-assignment1\fu_car_renting_system_andche186895
```

Build toàn bộ image:

```powershell
docker compose build
```

Khởi động hệ thống:

```powershell
docker compose up -d
```

Kiểm tra container:

```powershell
docker compose ps
```

Kết quả cần có:

```text
api-gateway        Up
eureka-server      Up
customer-service   Up
car-service        Up
renting-service    Up
customer-db        Up (healthy)
car-db             Up (healthy)
rental-db          Up (healthy)
```

### Xem log

```powershell
docker compose logs -f
```

Xem riêng một service:

```powershell
docker compose logs -f api-gateway
docker compose logs -f customer-service
docker compose logs -f car-service
docker compose logs -f renting-service
```

### Dừng hệ thống

Giữ lại dữ liệu database:

```powershell
docker compose down
```

Xóa cả container và dữ liệu database:

```powershell
docker compose down -v
```

## 6. Tài khoản mặc định

### Admin

```text
Email: admin@example.com
Password: admin
```

### Customer dùng để kiểm thử

```text
Email: customer@example.com
Password: 123456
```

Tài khoản Customer cần tồn tại trong `customer_db` và có trạng thái `ACTIVE`.

## 7. Authentication

Đăng nhập:

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

API Gateway đọc `customerId` và `role` từ JWT rồi tự tạo các header nội bộ này.

## 8. Danh sách API

### Authentication và Customer

| Method | Endpoint | Quyền |
|---|---|---|
| POST | `/auth/login` | Public |
| POST | `/customers/register` | Public |
| GET | `/customers/me` | Customer |
| PUT | `/customers/me` | Customer |
| GET | `/admin/customers` | Admin |
| GET | `/admin/customers/{id}` | Admin |
| PUT | `/admin/customers/{id}` | Admin |
| DELETE | `/admin/customers/{id}` | Admin |

### Manufacturer

| Method | Endpoint | Quyền |
|---|---|---|
| GET | `/manufacturers` | Authenticated |
| POST | `/manufacturers` | Admin |

### Supplier

| Method | Endpoint | Quyền |
|---|---|---|
| GET | `/suppliers` | Authenticated |
| POST | `/suppliers` | Admin |

### Car

| Method | Endpoint | Quyền |
|---|---|---|
| GET | `/cars` | Authenticated |
| GET | `/cars/available` | Authenticated |
| GET | `/cars/{id}` | Authenticated |
| POST | `/cars` | Admin |
| PUT | `/cars/{id}` | Admin |
| DELETE | `/cars/{id}` | Admin |

Xóa xe được thực hiện theo hình thức logical delete bằng cách chuyển trạng thái xe sang `INACTIVE`.

### Renting

| Method | Endpoint | Quyền |
|---|---|---|
| POST | `/rentings` | Customer |
| GET | `/rentings/history` | Customer |
| GET | `/rentings/{id}` | Customer |
| GET | `/rentings/reports` | Admin |

## 9. Tạo giao dịch thuê

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

Số ngày thuê được tính bao gồm cả ngày bắt đầu và ngày kết thúc:

```text
Số ngày = EndDate - StartDate + 1
```

## 10. Saga Orchestration

Renting Service đóng vai trò Saga Orchestrator.

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

Quá trình tạo giao dịch gồm:

1. Tạo transaction trạng thái `PENDING`.
2. Chuyển sang `EXECUTING`.
3. Kiểm tra Customer thông qua Customer Service.
4. Kiểm tra Car thông qua Car Service.
5. Kiểm tra trùng lịch thuê.
6. Tính tổng tiền.
7. Lưu Renting Detail.
8. Chuyển transaction sang `COMPLETED`.
9. Nếu có lỗi, thực hiện compensation.

## 11. Báo cáo Admin

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
  "transactions": []
}
```

Chỉ các giao dịch `COMPLETED` được đưa vào báo cáo. Danh sách được sắp xếp theo `rentingDate` giảm dần.

## 12. Mã trạng thái HTTP

| Mã | Ý nghĩa |
|---:|---|
| 200 | Request thành công |
| 201 | Tạo mới thành công |
| 400 | Request hoặc dữ liệu không hợp lệ |
| 401 | Chưa đăng nhập hoặc JWT không hợp lệ |
| 403 | Không đủ quyền hoặc Customer bị khóa |
| 404 | Không tìm thấy dữ liệu |
| 409 | Xe không khả dụng hoặc trùng lịch thuê |
| 502 | Service nội bộ trả lỗi |
| 503 | Service nội bộ không hoạt động |

## 13. Kiểm tra nhanh

Gateway:

```text
http://localhost:8080
```

Gateway health:

```text
http://localhost:8080/actuator/health
```

Eureka:

```text
http://localhost:8761
```

Kiểm tra API không có JWT:

```powershell
curl.exe -i http://localhost:8080/cars/available
```

Kết quả mong đợi:

```text
HTTP/1.1 401 Unauthorized
```

## 14. Thành viên thực hiện

```text
Họ và tên: Đinh Công An
Mã sinh viên: HE186895
Môn học: MSS301
```