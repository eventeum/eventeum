#!/bin/bash
command="java ${JAVA_OPTS} -jar eventeum-server.jar"
if [[ -z CONF ]]; then
  command="$command --spring.config.additional-location=$CONF"
fi

echo "Starting eventeum with command: $command"
eval $command
