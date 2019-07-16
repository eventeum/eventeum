FROM openjdk:8-jdk-alpine
ADD target/eventeum-server.jar eventeum-server.jar
ENV CONF ""
EXPOSE 8060
CMD java -jar eventeum-server.jar if [ "$CONF" != "" ] ; then echo " --spring.config.additional-location=$CONF" ; fi