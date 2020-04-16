package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MobileSCASenderTest {

    @Test
    void send() {
        // Then
        assertThrows(ScaModuleException.class, () -> new MobileSCASender().send("+380933434344", "myAuthCode"));
    }
}
