#! /bin/bash

command="java -jar eventeum-server.jar"

if [[ -n ${JMX_EXPORTER_ENABLED} ]]; then
  command="$command -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1234 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -javaagent:./jmx_prometheus_javaagent.jar=${JMX_EXPORTER_PORT}:jmx-config.yml"
fi

if [[ -n "${JVM_OPTS}" ]]; then
  command="$command $JVM_OPTS"
fi

if [[ -n "${CONF}"  ]]; then
  command="$command --spring.config.additional-location=$CONF"
fi

echo "Starting eventeum with command: $command"
eval $command
