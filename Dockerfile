ARG APP_INSIGHTS_AGENT_VERSION=3.5.2
ARG PLATFORM=""
# Application image

FROM hmctspublic.azurecr.io/base/java${PLATFORM}:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/rd-caseworker-ref-api.jar /opt/app/

EXPOSE 8095
CMD [ "rd-caseworker-ref-api.jar" ]
