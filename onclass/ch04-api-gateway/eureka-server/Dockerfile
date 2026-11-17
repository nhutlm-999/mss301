FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

RUN addgroup -S -g 10001 app && adduser -S -D -H -u 10001 -G app app

COPY target/se1905-eureka-server.jar /app/application.jar

USER 10001:10001

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
