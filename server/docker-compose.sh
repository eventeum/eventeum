#!/usr/bin/env bash

#Thanks to Gregoire Jeanmart for this script
echo "removing old containers"
docker-compose down

echo "removing storage"
sudo rm -rf "$HOME"/mongodb/data "$HOME"/parity/data/ "$HOME"/parity/log

case "$1" in
"rinkeby")
   composescript="docker-compose-rinkeby.yml"
   echo "Running in Rinkeby Infura mode..."
   ;;
"infra")
   composescript="docker-compose-infra.yml"
   echo "Running in Infrastructure mode..."
   ;;
*)
   composescript="docker-compose.yml"
   ;;
esac

echo "Build..."
docker-compose -f "$composescript" build || exit $?

echo "Start..."
docker-compose -f "$composescript" up -d || exit $?

trap "docker-compose -f "$composescript" kill" INT
