# ---------- BUILD STAGE ----------
FROM eclipse-temurin:8-jdk AS build
WORKDIR /app

# Copy toàn bộ project
COPY . .

# Cấp quyền và build (dùng mvnw để đảm bảo version Maven)
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# ---------- RUN STAGE ----------
FROM eclipse-temurin:8-jdk
WORKDIR /app

# Copy file JAR từ giai đoạn build
COPY --from=build /app/target/*.jar app.jar

# Thiết lập biến môi trường
ENV PORT=8080
EXPOSE 8080

# Lệnh chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]