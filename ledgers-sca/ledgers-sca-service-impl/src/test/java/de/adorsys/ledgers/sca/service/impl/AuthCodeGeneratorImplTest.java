package de.adorsys.ledgers.sca.service.impl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class AuthCodeGeneratorImplTest {

    private static final int TAN_LENGTH = 6;

    @Test
    void generate() {
        // Given
        AuthCodeGeneratorImpl generator = new AuthCodeGeneratorImpl();

        // When
        String tan1 = generator.generate();

        // Then
        assertThat(tan1, is(notNullValue()));
        assertThat(tan1.length(), is(TAN_LENGTH));

        // When
        String tan2 = generator.generate();

        // Then
        assertThat(tan2, is(not(tan1)));
        assertThat(tan2.length(), is(TAN_LENGTH));
    }
}