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
            "/swagger-resources/**",
            "/swagger-resources",
            "/swagger-ui.html**",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/webjars/**",
            "favicon.ico",
            "/error",
            "/*.js", "/*.css", "/*.ico", "/*.json", "/webjars/**", "/lib/*",

            "/auth/**",
            "/sso/**",
            "/csrf/**",

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
            "/staff-access/users/admin/authorize/user",
            "/staff-access/data/branch",
            "/password",
            "/users/validate",
            "/oauth/**",
            "/emails/email",
            "/sca/login"
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
