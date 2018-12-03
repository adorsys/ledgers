package de.adorsys.ledgers.util;

import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class PasswordEncTest {

    private static final String RAW_PASSWORD = "myPassword";
    private static final String ENCODED_PASSWORD = "$2a$10$O0tvcIfGm/XJMe13rDUsPerMJ/6CDEJWrCvMG/2jqx7CDhmkh3p.O";
    private static final String USER_ID = "XRouZLSnkfe54YTHRHLl1j";

    @Test
    public void verify() {
    	PasswordEnc passwordEnc = new PasswordEnc();
        assertThat("Verify source and encoded version of string", passwordEnc.verify(USER_ID, RAW_PASSWORD, ENCODED_PASSWORD));
    }
}
