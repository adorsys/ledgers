package de.adorsys.ledgers.app.server.auth;

public class PermittedResources {

    protected static final String[] SWAGGER_WHITELIST = {
            "/swagger-resources",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/webjars/**"
    };

    protected static final String[] INDEX_WHITELIST = {
            "/index.css",
            "/img/*",
            "/favicon.ico"
    };

    protected static final String[] APP_WHITELIST = {
            "/",
            "/management/app/admin",
            "/management/app/ping",
            "/users/login",
            "/users/register",
            "/users/loginForConsent",
            "/users/multilevel",
            "/data-test/upload-mockbank-data",
            "/data-test/db-flush",
            "/staff-access/users/register",
            "/staff-access/users/login",
            "/password",
            "/users/validate",
            "/oauth/**",
            "/emails/email"
    };

    protected static final String[] CONSOLE_WHITELIST = {
            "/console/**"
    };

    protected static final String[] ACTUATOR_WHITELIST = {
            "/actuator/info",
            "/actuator/health"
    };

    private PermittedResources() {
    }
}
