FROM eclipse-temurin:21-jdk-alpine
# thư mục làm việc
WORKDIR /app
# tạo 1 user -> cho tất cả file cat-service sinh ra đều thuộc user này
# lệnh addgroup của alpine sẽ tạo 1 nhóm người dung mới
# -S nhóm system, -g 10001 là id của nhóm, app là tên nhóm
# lệnh adduser của alpine sẽ tạo 1 user mới
# -S user system, -D không tạo home directory, -H không tạo home directory
# -u 10001 là id của user, -G app là nhóm mà user này thuộc về, app là tên user
RUN addgroup -S -g 10001 app && adduser -S -D -H -u 10001 -G app app

#copy file jar vào thư mục làm việc
COPY target/se1905-api-gateway.jar /app/application.jar

# quy định sẽ chạy container dưới quyền user
# quy định user làm owner cho các dữ liệu dc sinh ra khi chạy container
USER 10001:10001

# báo cho biết khi container chạy, nó lắng nghe port 8080(port trong container)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]