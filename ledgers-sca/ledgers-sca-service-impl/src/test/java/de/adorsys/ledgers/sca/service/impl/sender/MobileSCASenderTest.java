package de.adorsys.ledgers.sca.service.impl.sender;

import org.junit.Test;

public class MobileSCASenderTest {

    @Test(expected = UnsupportedOperationException.class)
    public void send() {
        new MobileSCASender().send("+380933434344", "myAuthCode");
    }
}