#!/bin/bash
# Thanks to Gregoire Jeanmart for this script
echo "Removing old containers"
docker rm -f server_kafka_1 || true
docker rm -f server_zookeeper_1 || true
docker rm -f server_mongodb_1 || true
docker rm -f server_eventeum_1 || true
docker rm -f server_parity_1 || true
docker-compose down || true

echo "Removing storage"
sudo rm -rf "$HOME/mongodb/data"
sudo rm -rf "$HOME/parity/data"
sudo rm -rf "$HOME/parity/log"

compose_script="docker-compose.yml"

if [[ "${1:-}" == "rinkeby" ]]; then
   compose_script="docker-compose-rinkeby.yml"
   echo "Running in Rinkeby Infura mode..."
elif [[ "${1:-}" == "infra" ]]; then
   compose_script="docker-compose-infra.yml"
   echo "Running in Infrastructure mode..."
fi

docker-compose -f "$compose_script" build

echo "Start"
docker-compose -f "$compose_script" up &

trap "docker-compose -f \"$compose_script\" kill" INT

wait
