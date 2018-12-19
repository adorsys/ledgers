package de.adorsys.ledgers.util.hash;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.ledgers.util.Base16;
import de.adorsys.ledgers.util.PasswordEnc;
import de.adorsys.ledgers.util.SerializationUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class HashGeneratorImplTest {

    private static final String PLAIN_TEXT = "my simple string";
    private static final String ENCODED_TEXT = "6C629DBB529DA292F35C3A5A84711228";

    @Test
    public void hash() throws HashGenerationException, JsonProcessingException {

        HashGeneratorImpl hashGenerator = new HashGeneratorImpl();

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

        assertThat(hash, is(ENCODED_TEXT));
    }

    @Test(expected = HashGenerationException.class)
    public void hashWithException() throws HashGenerationException {

        HashGeneratorImpl hashGenerator = new HashGeneratorImpl();

        String hash = hashGenerator.hash(new HashItem<String>() {
            @Override
            public String getAlg() {
                return "UNKNOWN_ALG";
            }

            @Override
            public String getItem() {
                return PLAIN_TEXT;
            }
        });
    }
}