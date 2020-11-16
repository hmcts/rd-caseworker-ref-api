#!/usr/bin/env sh

GRADLE_CLEAN=true
GRADLE_INSTALL=true

# Test S2S key - not used in any HMCTS key vaults or services
export S2S_SECRET=SZDUA3L7N32PE2IS
export S2S_MICROSERVICE=rd_professional_api

clean_old_docker_artifacts() {
    docker stop rd-professional-api
    docker stop rd-caseworker-ref-api-db
    docker stop service-token-provider

    docker rm rd-professional-api
    docker rm rd-caseworker-ref-api-db
    docker rm service-token-provider

    docker rmi hmcts/rd-professional-api
    docker rmi hmcts/rd-caseworker-ref-api-db
    docker rmi hmcts/service-token-provider

    docker volume rm rd-caseworker-ref-api_rd-caseworker-ref-api-db-volume
}

execute_script() {

  clean_old_docker_artifacts

    docker-compose down -v

    docker system prune -a -f

    ./gradlew clean assemble

    pwd

    chmod +x bin/*

    docker-compose up
}

execute_script
