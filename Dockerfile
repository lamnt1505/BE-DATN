# ---------- BUILD STAGE ----------
FROM eclipse-temurin:1.8-jdk-alpine AS build
WORKDIR /app

# Copy toàn bộ mã nguồn vào container
COPY . .

# Cấp quyền chạy mvnw và build project
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# ---------- RUN STAGE ----------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy file JAR từ giai đoạn build
COPY --from=build /app/target/*.jar app.jar

# Cấu hình biến môi trường
ENV PORT=8080
EXPOSE 8080

# Lệnh chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
