package de.adorsys.ledgers.postings.db.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.adorsys.ledgers.postings.db.domain.HashRecord;
import de.adorsys.ledgers.util.Base16;
import de.adorsys.ledgers.util.SerializationUtils;

public class RecordHashHelper {

    private static final String POSTING_CONTAINS_A_RECORD_HASH = "Posting contains a record hash. Set record hash to null before calling this method";
    private static final String MISSING_RECORD_ANTECEDANT = "Missing record antecedant hash. Must be set to hash of %s prior saving.";
    private static final String DEFAULT_HASH_ALG = "SHA-256";

    public String computeRecHash(HashRecord hashRecord) throws NoSuchAlgorithmException, JsonProcessingException {
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
        MessageDigest digest = MessageDigest.getInstance(hashRecord.getHashAlg());
        byte[] valueAsBytes = SerializationUtils.writeValueAsBytes(hashRecord);
        return Base16.encode(digest.digest(valueAsBytes));
    }
}
