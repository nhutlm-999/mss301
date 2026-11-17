FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

RUN addgroup -S -g 10001 app && adduser -S -D -H -u 10001 -G app app

COPY target/config-server-0.0.1-SNAPSHOT.jar /app/application.jar

USER 10001:10001

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
