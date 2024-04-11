/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util.hash;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashGeneratorImplTest {

    private static final String PLAIN_TEXT = "my simple string";
    private static final String ENCODED_TEXT = "6C629DBB529DA292F35C3A5A84711228";

    @Test
    void hash() throws HashGenerationException {
        // Given
        HashGeneratorImpl hashGenerator = new HashGeneratorImpl();

        // When
        String hash = hashGenerator.hash(new HashItem<String>() {
            @Override
            public String getAlg() {
                return "MD5";
            }

            @Override
            public String getItem() {
                return PLAIN_TEXT;
            }
        });

        // Then
        assertThat(hash, is(ENCODED_TEXT));
    }

    @Test
    void hashWithException() throws HashGenerationException {
        // Given
        HashGeneratorImpl hashGenerator = new HashGeneratorImpl();
        HashItem<String> hashItem = new HashItem<>() {
            @Override
            public String getAlg() {
                return "UNKNOWN_ALG";
            }

            @Override
            public String getItem() {
                return PLAIN_TEXT;
            }
        };
        // Then
        assertThrows(HashGenerationException.class, () -> hashGenerator.hash(hashItem));
    }
}
