package de.adorsys.ledgers.middleware.api.domain;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final String SCOPE_SCA = "sca";
    public static final String SCOPE_PARTIAL_ACCESS = "partial_access";
    public static final String SCOPE_FULL_ACCESS = "full_access";

    public static final List<String> ALL_SCOPES = Arrays.asList(SCOPE_SCA, SCOPE_PARTIAL_ACCESS, SCOPE_FULL_ACCESS);

    private Constants() {
    }
}
