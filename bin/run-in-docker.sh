#!/usr/bin/env sh

GRADLE_CLEAN=true
GRADLE_INSTALL=true


clean_old_docker_artifacts() {
    docker stop rd-caseworker-ref-api-db
    docker rm rd-caseworker-ref-api-db
    docker rmi rd-caseworker-ref-api-db
    docker-compose stop -v
    docker system prune -af
}

execute_script() {

  clean_old_docker_artifacts

  cd $(dirname "$0")/..

  ./gradlew clean assemble

  chmod +x bin/*

  docker-compose up
}

execute_script