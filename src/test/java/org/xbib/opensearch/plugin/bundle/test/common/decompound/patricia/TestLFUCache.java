package org.xbib.opensearch.plugin.bundle.test.common.decompound.patricia;

import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import org.junit.Ignore;
import org.junit.Test;
import org.xbib.opensearch.plugin.bundle.common.decompound.patricia.LFUCache;

import static org.junit.Assert.assertEquals;

@Ignore
public class TestLFUCache {

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
