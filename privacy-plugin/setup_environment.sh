#!/bin/bash
DATABASE=${1:-"mongo"}

if [ "$DATABASE" != "mongo" ]  && [ "$DATABASE" != "sql"  ];
then
  echo "ERROR: Database can only be mongo or sql"
  exit 1;
fi

DOCKER_COMPOSE_DATABASE="docker-compose-$DATABASE.yaml"
echo "$DOCKER_COMPOSE_DATABASE"
docker-compose -f "docker-compose.yaml" -f "$DOCKER_COMPOSE_DATABASE" up -d