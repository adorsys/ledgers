package de.adorsys.ledgers.app.server.auth;

public class PermittedResources {

    public static final String[] SWAGGER_WHITELIST = {
            "/swagger-resources",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/webjars/**"
    };

    public static final String[] INDEX_WHITELIST = {
            "/index.css",
            "/img/*",
            "/favicon.ico"
    };

    public static final String[] APP_WHITELIST = {
            "/",
            "/management/app/admin",
            "/management/app/ping",
            "/users/login",
            "/users/register",
            "/users/loginForConsent",
            "/data-test/upload-mockbank-data",
            "/data-test/db-flush",
            "/staff-access/users/register",
            "/staff-access/users/login",
            "/password"
    };

    public static final String[] CONSOLE_WHITELIST = {
            "/console/**"
    };

    private PermittedResources() {}
}
