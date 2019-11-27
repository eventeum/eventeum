#!/bin/bash
command="java -Dcom.sun.management.jmxremote \
              -Dcom.sun.management.jmxremote.port=1234 \
              -Dcom.sun.management.jmxremote.ssl=false \
              -Dcom.sun.management.jmxremote.authenticate=false \
              -javaagent:./jmx_prometheus_javaagent.jar=${JMX_EXPORTER_PORT}:jmx-config.yml \
              -jar eventeum-server.jar"

if [[ -z JVM_OPS ]]; then
  command="$command $JVM_OPS"
fi

if [[ -z CONF ]]; then
  command="$command --spring.config.additional-location=$CONF"
fi

echo "Starting eventeum with command: $command"
eval $command
