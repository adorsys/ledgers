/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.util.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.util.Base16;

public class HashGeneratorImpl implements HashGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HashGeneratorImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
                                                             .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public <T> String hash(HashItem<T> hashItem) throws HashGenerationException {

        // Get string value including hash
        MessageDigest digest;
        byte[] valueAsBytes;
        try {
            String alg = StringUtils.isBlank(hashItem.getAlg()) ? DEFAULT_HASH_ALG : hashItem.getAlg();
            digest = MessageDigest.getInstance(alg);
            valueAsBytes = objectMapper.writeValueAsBytes(hashItem.getItem());
        } catch (NoSuchAlgorithmException | JsonProcessingException e) {
            logger.error("Can't generate the hash", e);
            throw new HashGenerationException("Can't generate the hash", e);
        }

        return Base16.encode(digest.digest(valueAsBytes));
    }
}
