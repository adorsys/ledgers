FROM jboss/keycloak:11.0.0
LABEL maintainer=https://git.adorsys.de/adorsys/xs2a/ledgers

COPY ./keycloak-token-exchange/target/keycloak-token-exchange.jar /opt/jboss/keycloak/standalone/deployments/keycloak-token-exchange.jar

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT [ "/opt/jboss/tools/docker-entrypoint.sh" ]

CMD ["-b", "0.0.0.0"]