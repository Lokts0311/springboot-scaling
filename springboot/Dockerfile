FROM amazoncorretto:21-alpine-full as build

WORKDIR /app

COPY out/artifacts/springboot_jar/springboot.jar /app/my-app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "/app/my-app.jar"]