package de.adorsys.ledgers.util;

import org.junit.Assert;
import org.junit.Test;


public class IdsTest {

    @Test
    public void test() {
        Assert.assertEquals(22, Ids.id().length());

        for (int i = 0; i < 1000; i++) {
            String id = Ids.id();
            Assert.assertFalse(id.contains("/"));
        }
    }

    @Test
    public void test_url_encoded() {
        for (int i = 0; i < 1000; i++) {
            String id = Ids.id();
            Assert.assertFalse(id.contains("/"));
            Assert.assertFalse(id.contains("+"));
        }
    }
}
