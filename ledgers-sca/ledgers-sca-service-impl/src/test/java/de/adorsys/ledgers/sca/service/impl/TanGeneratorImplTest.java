package de.adorsys.ledgers.sca.service.impl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class TanGeneratorImplTest {

    private static final int TAN_LENGTH = 6;

    @Test
    public void generate() {
        TanGeneratorImpl generator = new TanGeneratorImpl();

        String tan1 = generator.generate();
        assertThat(tan1, is(notNullValue()));
        assertThat(tan1.length(), is(TAN_LENGTH));

        String tan2 = generator.generate();
        assertThat(tan2, is(not(tan1)));
        assertThat(tan2.length(), is(TAN_LENGTH));
    }
}