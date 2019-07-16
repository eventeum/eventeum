#!/bin/bash
#Thanks to Gregoire Jeanmart for this script
echo "removing old containers"
docker rm -f server_kafka_1
docker rm -f server_zookeeper_1
docker rm -f server_mongodb_1
docker rm -f server_eventeum_1
docker rm -f server_parity_1
docker-compose down

echo "removing storage"
sudo rm -rf $HOME/mongodb/data
sudo rm -rf $HOME/parity/data:/root/
sudo rm -rf $HOME/parity/log

echo "Build"
mvn clean install -f ../pom.xml $1
[ $? -eq 0 ] || exit $?;


docker-compose build
[ $? -eq 0 ] || exit $?;


echo "Start"
docker-compose up
[ $? -eq 0 ] || exit $?;

trap "docker-compose kill" INT
