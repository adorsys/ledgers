FROM jboss/keycloak:11.0.0
LABEL "Custom-KeyCloak"="KK"

COPY target/keycloak-token-exchange.jar /opt/jboss/keycloak/standalone/deployments/keycloak-token-exchange.jar

ENV KEYCLOAK_USER=admin
ENV KEYCLOAK_PASSWORD=admin123

EXPOSE 8080