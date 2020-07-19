#!/bin/bash
command="java -jar eventeum-server.jar"
if [[ -n "$CONF" ]]; then
  command="$command --spring.config.additional-location=$CONF"
fi

echo "Starting eventeum with command: $command"
eval $command
