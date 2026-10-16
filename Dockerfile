# ==========================================
# Giai đoạn 1: Build file JAR với Maven & Java 21
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Sao chép pom.xml để tải dependencies trước (tận dụng Docker layer cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Sao chép mã nguồn và đóng gói ứng dụng
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Giai đoạn 2: Runtime môi trường Java 21 + Python AI
# ==========================================
FROM eclipse-temurin:21-jre-noble

WORKDIR /app

# Cài đặt Python 3, pip, venv và các thư viện hệ thống cần thiết cho OpenCV / PyTorch
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 \
    python3-pip \
    python3-venv \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Tạo môi trường ảo Python và cài đặt PyTorch (bản CPU nhẹ) + Ultralytics YOLO
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu && \
    pip install --no-cache-dir ultralytics

# Sao chép file JAR đã build từ stage 1
COPY --from=builder /build/target/*.jar app.jar

# Sao chép thư mục script AI và mô hình YOLO best.pt
COPY ai-service/ /app/ai-service/
COPY src/main/resources/static/best.pt /app/models/best.pt

# Tạo thư mục lưu trữ ảnh người dùng tải lên
RUN mkdir -p /app/uploads

# Thiết lập biến môi trường cấu hình BeanAI
ENV BEANAI_PYTHON_PATH=python3 \
    BEANAI_SCRIPT_PATH=/app/ai-service/predict.py \
    BEANAI_MODEL_PATH=/app/models/best.pt \
    BEANAI_UPLOAD_DIR=/app/uploads \
    SERVER_PORT=8080

# Cổng truy cập ứng dụng
EXPOSE 8080

# Phân vùng lưu trữ ảnh upload
VOLUME ["/app/uploads"]

# Lệnh khởi chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
