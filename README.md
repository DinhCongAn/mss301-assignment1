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
