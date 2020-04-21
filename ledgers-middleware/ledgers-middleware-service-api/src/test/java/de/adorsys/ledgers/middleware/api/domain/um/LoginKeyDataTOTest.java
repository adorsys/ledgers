package de.adorsys.ledgers.middleware.api.domain.um;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginKeyDataTOTest {

    @Test
    void testFromOpId() {
        // Given
        String userId = "q2tRswcRSr4nXqUaJCXpkQ";
        LocalDateTime time = LocalDateTime.now();
        LoginKeyDataTO loginKeyDataTO = new LoginKeyDataTO(userId, time);
        String opId = loginKeyDataTO.toOpId();
        LoginKeyDataTO lkd = LoginKeyDataTO.fromOpId(opId);
        String userId2 = lkd.getUserId();

        // Then
        assertEquals(userId, userId2);
    }
}
