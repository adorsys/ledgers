FROM quay.io/keycloak/keycloak:20.0.5
LABEL maintainer=https://git.adorsys.de/adorsys/xs2a/ledgers

COPY ./keycloak-token-exchange/target/keycloak-token-exchange.jar /opt/keycloak/providers/keycloak-token-exchange.jar
RUN echo 'Creating directories for Keycloak...' && mkdir -p /opt/keycloak/data && chmod 777 /opt/keycloak/data

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT [ "/opt/keycloak/bin/kc.sh", "start-dev" ]
