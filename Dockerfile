## stage maven
FROM maven:3.6-jdk-8 as BUILDER
WORKDIR /app
COPY . .
RUN mvn clean package

## stage build
FROM openjdk:8-jre
ENV CONF ""
COPY --from=BUILDER /app/server/target/eventeum-server.jar .
COPY --from=BUILDER /app/server/docker-scripts/start-eventeum.sh .

RUN chmod +x start-eventeum.sh

EXPOSE 8060

CMD ["./start-eventeum.sh"]