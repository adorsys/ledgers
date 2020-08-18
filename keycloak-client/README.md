# Keycloak-client

Ledgers client for communication with Keycloak IDP. Main purposes:

- login to IDP;
- exchange token;
- validate token.

Please note, that Keycloak base path is configured in the root application.yml file (`keycloak` part, variable name is `auth-server-url`).