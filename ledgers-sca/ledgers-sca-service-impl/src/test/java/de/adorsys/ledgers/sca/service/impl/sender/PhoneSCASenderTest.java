package de.adorsys.ledgers.sca.service.impl.sender;

import org.junit.Test;

import static org.junit.Assert.*;

public class PhoneSCASenderTest {

    @Test(expected = UnsupportedOperationException.class)
    public void send() {
        new PhoneSCASender().send("+380933434344","myAuthCode");
    }
}