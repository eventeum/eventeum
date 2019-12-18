#!/bin/bash

TYPE=$1
echo Release type: $TYPE

GIT_COMMIT=$(git rev-parse --short HEAD)
GIT_COMMIT_DESC=$(git log --format=oneline -n 1 $CIRCLE_SHA1)

echo Git commit message: $GIT_COMMIT_DESC
echo Git PR Number: $CIRCLE_PR_NUMBER

EVENTEUM_VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)

push_image() {
    echo building docker with version: ${VERSION}
    set -e
    docker build -t adharaprojects/eventeum:${VERSION} -f server/Dockerfile server/.
    docker push adharaprojects/eventeum:${VERSION}
}

case $TYPE in
  "master")
    VERSION=$(echo "${EVENTEUM_VERSION}-adhara.${GIT_COMMIT}" | sed "s/-SNAPSHOT//g")
    push_image
    ;;

  "snapshot")
    VERSION="${EVENTEUM_VERSION}-adhara.${GIT_COMMIT}"
    push_image
    ;;

  "tag")
    GIT_TAG=$(git describe --tags --always)
    VERSION="${GIT_TAG}"
    push_image
    ;;
esac
