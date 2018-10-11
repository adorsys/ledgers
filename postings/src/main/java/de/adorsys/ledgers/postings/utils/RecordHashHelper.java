package de.adorsys.ledgers.postings.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingTrace;
import de.adorsys.ledgers.util.Base16;
import de.adorsys.ledgers.util.SerializationUtils;

public class RecordHashHelper {

    private static final String POSTING_CONTAINS_A_RECORD_HASH = "Posting contains a record hash. Set record hash to null before calling this method";
    private static final String MISSING_RECORD_ANTECEDANT = "Missing record antecedant hash. Must be set to hash of %s prior saving.";
    private static final String MISSING_RECORD_ANTECEDANT_HASH = "Missing record antecedant hash. Must be set to hash of %s prior saving.";
    private static final String POSTING_TRACE_CONTAINS_A_HASH = "PostingTrace contains a hash. Set hash to null before calling this method";
    private static final String DEFAULT_HASH_ALG = "SHA-256";

    public String computeRecHash(Posting posting) throws NoSuchAlgorithmException, JsonProcessingException {
        if (posting.getRecordAntecedentId() != null && posting.getRecordAntecedentHash() == null) {
            throw new IllegalStateException(String.format(MISSING_RECORD_ANTECEDANT, posting.getRecordAntecedentId()));
        }
        // Reset Record hash if not null.
        if (posting.getRecordHash() != null) {
            throw new IllegalStateException(POSTING_CONTAINS_A_RECORD_HASH);
        }
        // Check hash algo
        if (posting.getRecordHashAlg() == null) {
            posting.setRecordHashAlg(DEFAULT_HASH_ALG);
        }

        // Get string value including hash
        MessageDigest digest = MessageDigest.getInstance(posting.getRecordHashAlg());
        byte[] valueAsBytes = SerializationUtils.writeValueAsBytes(posting);
        return Base16.encode(digest.digest(valueAsBytes));
    }

    public String computeRecHash(PostingTrace postingTrace) throws NoSuchAlgorithmException, JsonProcessingException {
        if (postingTrace.getAntTraceId() != null && postingTrace.getAntTraceHash() == null) {
            throw new IllegalStateException(String.format(MISSING_RECORD_ANTECEDANT_HASH, postingTrace.getAntTraceId()));
        }
        // Reset Record hash if not null.
        if (postingTrace.getHash() != null) {
            throw new IllegalStateException(POSTING_TRACE_CONTAINS_A_HASH);
        }
        // Check hash algo
        if (postingTrace.getHashAlg() == null) {
            postingTrace.setHashAlg(DEFAULT_HASH_ALG);
        }

        // Get string value including hash
        MessageDigest digest = MessageDigest.getInstance(postingTrace.getHashAlg());
        byte[] valueAsBytes = SerializationUtils.writeValueAsBytes(postingTrace);
        return Base16.encode(digest.digest(valueAsBytes));
    }
}
