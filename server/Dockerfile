FROM openjdk:12
ENV JMX_EXPORTER_VERSION=0.12.0
ENV JMX_EXPORTER_PORT=8081
ADD https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/${JMX_EXPORTER_VERSION}/jmx_prometheus_javaagent-${JMX_EXPORTER_VERSION}.jar /jmx_prometheus_javaagent.jar
ADD jmx-config.yml jmx-config.yml
ADD target/eventeum-server.jar eventeum-server.jar
ADD docker-scripts/start-eventeum.sh start-eventeum.sh
ENV CONF ""
EXPOSE 8060 8081
CMD chmod +x start-eventeum.sh && ./start-eventeum.sh
