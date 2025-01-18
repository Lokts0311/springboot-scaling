FROM amazoncorretto:21-alpine-full as build

WORKDIR /app

COPY target/springboot-0.0.1-SNAPSHOT.jar /app/my-app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/app/my-app.jar"]