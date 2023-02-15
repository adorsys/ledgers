/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.utils;

import de.adorsys.ledgers.postings.db.domain.HashRecord;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;

public class RecordHashHelper {

    private static final String POSTING_CONTAINS_A_RECORD_HASH = "Posting contains a record hash. Set record hash to null before calling this method";
    private static final String MISSING_RECORD_ANTECEDANT = "Missing record antecedant hash. Must be set to hash of %s prior saving.";
    private static final String DEFAULT_HASH_ALG = "SHA-256";

    public String computeRecHash(HashRecord hashRecord) throws HashGenerationException {
        if (hashRecord.getAntecedentId() != null && hashRecord.getAntecedentHash() == null) {
            throw new IllegalStateException(String.format(MISSING_RECORD_ANTECEDANT, hashRecord.getAntecedentId()));
        }
        // Reset Record hash if not null.
        if (hashRecord.getHash() != null) {
            throw new IllegalStateException(POSTING_CONTAINS_A_RECORD_HASH);
        }
        // Check hash algo
        if (hashRecord.getHashAlg() == null) {
            hashRecord.setHashAlg(DEFAULT_HASH_ALG);
        }

        // Get string value including hash
        return new HashGeneratorImpl().hash(hashRecord);
    }
}
