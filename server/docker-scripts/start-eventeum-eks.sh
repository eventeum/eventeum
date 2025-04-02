#!/bin/bash

# -Dmicronaut.server.port=${JAVA_PORT}
command="java -Xms${JAVA_MEM_XMS} -Xmx${JAVA_MEM_XMX} -Djava.security.egd=file:/dev/./urandom -XX:MaxMetaspaceSize=${JAVA_MEM_META} ${JAVA_EXTRA_OPTS} -jar eventeum-server.jar"
if [[ -n "$CONF" ]]; then
  command="$command --spring.config.additional-location=$CONF"
fi

echo "Starting eventeum with command: $command"
eval "$command"
