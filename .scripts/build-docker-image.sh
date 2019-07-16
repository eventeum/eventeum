#!/bin/bash
TYPE=$1
echo Release type: $TYPE

GIT_COMMIT_DESC=$(git log --format=oneline -n 1 $CIRCLE_SHA1)
echo Git commit message: $GIT_COMMIT_DESC
echo Git PR Number: $CIRCLE_PR_NUMBER

if [[ "$TYPE" == "latest" ]]; then
  VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec | sed 's/-SNAPSHOT//g')
fi

if [[ "$TYPE" == "snapshot" ]]; then
  VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)
fi

if [[ -v VERSION ]]; then
    docker login -u ${DOCKER_HUB_USER_ID} -p ${DOCKER_HUB_PWD}

    echo building docker with version: ${VERSION}
    set -e

    docker build -t eventeum/eventeum:${VERSION} -f server/Dockerfile server/.
    docker build -t eventeum/eventeum:${TYPE} -f server/Dockerfile server/.
    docker push eventeum/eventeum:${VERSION}
    docker push eventeum/eventeum:${TYPE}
fi