package de.adorsys.ledgers.middleware.rest.utils;

public abstract class Constants {
    public static final String API_KEY = "apiKey";
    public static final String OAUTH2 = "oAuth2";

    public static final String ID = "id";
    public static final String AUTH_HEADER_NAME = "Authorization";
    public static final String UNPROTECTED_ENDPOINT = "LDG015 - Unprotected Endpoints";

    public static final String ACCOUNT_ID = "accountId";
    public static final String IBAN = "ibanParam";
    public static final String CURRENCY = "currency";
    public static final String ACCOUNT_IDENTIFIER_TYPE = "accountIdentifierType";
    public static final String ACCOUNT_IDENTIFIER = "accountIdentifier";

    public static final String USER_ID = "userId";
    public static final String USER_LOGIN = "userLogin";
    public static final String LOGIN = "login";
    public static final String PIN = "pin";
    public static final String PASSWORD = "password";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String CODE = "code";
    public static final String EMAIL ="email";
    public static final String VERIFICATION_TOKEN ="verificationToken";

    public static final String AUTH_ID = "authorisationId";
    public static final String SCA_METHOD_ID = "scaMethodId";
    public static final String AUTH_CODE = "authCode";
    public static final String AUTH_CONF_CODE = "authConfirmCode";
    public static final String AUTH_CONFIRMED = "authCodeConfirmed";

    public static final String PAYMENT_ID = "paymentId";
    public static final String PAYMENT_TYPE = "paymentType";
    public static final String CONSENT_ID = "consentId";
    public static final String TRANSACTION_ID = "transactionId";

    public static final String BRANCH = "branch";
    public static final String BRANCH_ID = "branchId";
    public static final String BRANCH_LOGIN = "branchLogin";
    public static final String ROLE = "role";
    public static final String ROLES = "roles";
    public static final String COUNTRY_CODE = "country";

    public static final String QUERY_PARAM = "queryParam";
    public static final String BLOCKED = "blockedParam";
    public static final String PAGE = "page";
    public static final String SIZE = "size";

    public static final String LOCAL_DATE_YYYY_MM_DD_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TO_QUERY_PARAM = "dateTo";
    public static final String DATE_FROM_QUERY_PARAM = "dateFrom";

    private Constants() {
    }
}
