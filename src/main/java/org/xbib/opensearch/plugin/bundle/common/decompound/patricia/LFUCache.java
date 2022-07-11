package org.xbib.opensearch.plugin.bundle.common.decompound.patricia;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * LFU cache implementation based on http://dhruvbird.com/lfu.pdf, with some notable differences:
 * <ul>
 * <li>
 * Frequency list is stored as an array with no next/prev pointers between nodes:
 * looping over the array should be faster and more CPU-cache friendly than
 * using an ad-hoc linked-pointers structure.
 * </li>
 * <li>
 * The max frequency is capped at the cache size to avoid creating more and more frequency list entries,
 * and all elements residing in the max frequency entry are re-positioned in the frequency entry linked set
 * in order to put most recently accessed elements ahead of less recently ones,
 * which will be collected sooner.
 * </li>
 * <li>
 * The eviction factor determines how many elements (more specifically, the percentage of) will be evicted.
 * </li>
 * </ul>
 * As a consequence, this cache runs in amortized O(1) time (considering the worst case of having
 * the lowest frequency at 0 and having to evict all elements).
 */
public class LFUCache<K, V> implements Map<K, V> {

    private final Map<K, CacheNode<K, V>> cache;

    private final Set<CacheNode<K, V>>[] frequencyList;

    private int lowestFrequency;

    private int maxFrequency;

    private final int maxCacheSize;

    private final float evictionFactor;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public LFUCache(int maxCacheSize, float evictionFactor) {
        if (evictionFactor <= 0 || evictionFactor >= 1) {
            throw new IllegalArgumentException("eviction factor must be greater than 0 and less than or equal to 1");
        }
        this.cache = new HashMap<>(maxCacheSize);
        this.frequencyList = new LinkedHashSet[maxCacheSize];
        this.lowestFrequency = 0;
        this.maxFrequency = maxCacheSize - 1;
        this.maxCacheSize = maxCacheSize;
        this.evictionFactor = evictionFactor;
        initFrequencyList();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public V put(K k, V v) {
        V oldValue = null;
        CacheNode<K, V> currentNode = cache.get(k);
        if (currentNode == null) {
            if (cache.size() == maxCacheSize) {
                doEviction();
            }
            Set<CacheNode<K, V>> nodes = frequencyList[0];
            currentNode = new CacheNode(k, v, 0);
            nodes.add(currentNode);
            cache.put(k, currentNode);
            lowestFrequency = 0;
        } else {
            oldValue = currentNode.v;
            currentNode.v = v;
        }
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> me : map.entrySet()) {
            put(me.getKey(), me.getValue());
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public V get(Object k) {
        CacheNode<K, V> currentNode = cache.get(k);
        if (currentNode != null) {
            int currentFrequency = currentNode.frequency;
            if (currentFrequency < maxFrequency) {
                int nextFrequency = currentFrequency + 1;
                Set<CacheNode<K, V>> currentNodes = frequencyList[currentFrequency];
                Set<CacheNode<K, V>> newNodes = frequencyList[nextFrequency];
                moveToNextFrequency(currentNode, nextFrequency, currentNodes, newNodes);
                cache.put((K) k, currentNode);
                if (lowestFrequency == currentFrequency && currentNodes.isEmpty()) {
                    lowestFrequency = nextFrequency;
                }
            } else {
                Set<CacheNode<K, V>> nodes = frequencyList[currentFrequency];
                nodes.remove(currentNode);
                nodes.add(currentNode);
            }
            return currentNode.v;
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public V remove(Object k) {
        CacheNode<K, V> currentNode = cache.remove(k);
        if (currentNode != null) {
            Set<CacheNode<K, V>> nodes = frequencyList[currentNode.frequency];
            nodes.remove(currentNode);
            if (lowestFrequency == currentNode.frequency) {
                findNextLowestFrequency();
            }
            return currentNode.v;
        } else {
            return null;
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i <= maxFrequency; i++) {
            frequencyList[i].clear();
        }
        cache.clear();
        lowestFrequency = 0;
    }

    @Override
    public Set<K> keySet() {
        return this.cache.keySet();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return this.cache.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return this.cache.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return false;
    }

    public int frequencyOf(K k) {
        CacheNode<K, V> node = cache.get(k);
        if (node != null) {
            return node.frequency + 1;
        } else {
            return 0;
        }
    }

    private void initFrequencyList() {
        for (int i = 0; i <= maxFrequency; i++) {
            frequencyList[i] = new LinkedHashSet<CacheNode<K, V>>();
        }
    }

    @SuppressWarnings("unchecked")
    private void doEviction() {
        int currentlyDeleted = 0;
        float target = maxCacheSize * evictionFactor;
        while (currentlyDeleted < target) {
            Set<CacheNode<K, V>> nodes = frequencyList[lowestFrequency];
            if (nodes.isEmpty()) {
                throw new IllegalStateException("lowest frequency constraint violated");
            } else {
                Iterator<CacheNode<K, V>> it = nodes.iterator();
                while (it.hasNext() && currentlyDeleted++ < target) {
                    CacheNode<K, V> node = it.next();
                    it.remove();
                    cache.remove(node.k);
                }
                if (!it.hasNext()) {
                    findNextLowestFrequency();
                }
            }
        }
    }

    private void moveToNextFrequency(CacheNode<K, V> currentNode, int nextFrequency,
                                     Set<CacheNode<K, V>> currentNodes,
                                     Set<CacheNode<K, V>> newNodes) {
        currentNodes.remove(currentNode);
        newNodes.add(currentNode);
        currentNode.frequency = nextFrequency;
    }

    private void findNextLowestFrequency() {
        while (lowestFrequency <= maxFrequency && frequencyList[lowestFrequency].isEmpty()) {
            lowestFrequency++;
        }
        if (lowestFrequency > maxFrequency) {
            lowestFrequency = 0;
        }
    }

    private static class CacheNode<K, V> {

        final K k;
        V v;
        int frequency;

        CacheNode(K k, V v, int frequency) {
            this.k = k;
            this.v = v;
            this.frequency = frequency;
        }

    }
}
