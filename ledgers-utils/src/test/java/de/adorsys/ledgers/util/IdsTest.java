/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IdsTest {

    @Test
    void test() {
        assertEquals(22, Ids.id().length());

        for (int i = 0; i < 1000; i++) {
            String id = Ids.id();
            assertFalse(id.contains("/"));
        }
    }

    @Test
    void test_url_encoded() {
        for (int i = 0; i < 1000; i++) {
            String id = Ids.id();
            assertFalse(id.contains("/"));
            assertFalse(id.contains("+"));
        }
    }
}
