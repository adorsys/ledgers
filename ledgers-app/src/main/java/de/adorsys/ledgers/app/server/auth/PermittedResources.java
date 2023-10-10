/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.server.auth;

public class PermittedResources {

    protected static final String[] SWAGGER_WHITELIST = {
            "/swagger-resources/**",
            "/swagger-resources",
            "/swagger-ui.html**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v2/api-docs",
            "/api-docs",
            "/api-docs/swagger-config",
            "/error",
            "/webjars/**"
    };

    protected static final String[] INDEX_WHITELIST = {
            "/index.css",
            "/img/*",
            "/favicon.ico",
            "/index.html"
    };

    protected static final String[] APP_WHITELIST = {
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
            "/users/reset/password/**",
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
