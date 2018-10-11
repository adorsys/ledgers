package de.adorsys.ledgers.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MD5UtilTest {

    private static final String RAW_PASSWORD = "myPassword";
    private static final String MD5_PASSWORD = "deb1536f480475f7d593219aa1afd74c";

    @Test
    public void encode() {
        String encodedPassword = MD5Util.encode(RAW_PASSWORD);
        assertThat(encodedPassword, is(MD5_PASSWORD));
    }

    @Test
    public void verify() {
        assertThat("Verify source and md5 version of string", MD5Util.verify(RAW_PASSWORD, MD5_PASSWORD));
    }
}
