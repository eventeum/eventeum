FROM openjdk:8-jdk-alpine
ADD target/eventeum-exec.jar app.jar
EXPOSE 8060
CMD ["java", "-jar", "app.jar"]