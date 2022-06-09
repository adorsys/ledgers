FROM quay.io/keycloak/keycloak:17.0.0
LABEL maintainer=https://git.adorsys.de/adorsys/xs2a/ledgers

COPY ./keycloak-token-exchange/target/keycloak-token-exchange.jar /opt/keycloak/providers/keycloak-token-exchange.jar

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT [ "/opt/keycloak/bin/kc.sh", "start-dev" ]