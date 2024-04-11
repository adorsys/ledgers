/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util.hash;

public interface HashGenerator {
    String DEFAULT_HASH_ALG = "SHA-256";

    <T> String hash(HashItem<T> hashItem) throws HashGenerationException;
}
