# ==========================================
# GIAI ĐOẠN 1: BUILD ỨNG DỤNG
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy pom.xml trước để Docker cache dependency
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build file JAR
RUN mvn clean package -DskipTests -B


# ==========================================
# GIAI ĐOẠN 2: CHẠY ỨNG DỤNG
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]