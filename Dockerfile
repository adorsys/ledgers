FROM adorsys/java:11
LABEL maintainer=https://git.adorsys.de/adorsys/xs2a/ledgers

ENV SERVER_PORT 8088
ENV JAVA_OPTS -Xmx1024m
ENV JAVA_TOOL_OPTIONS -Xmx1024m

WORKDIR /opt/ledgers

COPY ./ledgers-app/target/ledgers-app.jar /opt/ledgers/ledgers-app.jar

EXPOSE 8088
# hadolint ignore=DL3025
CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/ledgers/ledgers-app.jar
