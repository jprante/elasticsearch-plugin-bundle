package org.xbib.elasticsearch.common.decompound.patricia;

import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LFUCacheTest {

    @SuppressForbidden(value = "execute this to test LFU cache")
    @Test
    public void testCache() {
        LFUCache<Integer, Integer> cache = new LFUCache<>(100, 0.90f);
        for (int i = 0; i < 500; i++) {
            cache.computeIfAbsent(i, f -> f % 2);
        }
        assertEquals(50, cache.size());
    }
}
