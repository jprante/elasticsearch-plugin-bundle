package org.xbib.opensearch.plugin.bundle.test;

import java.util.Collection;
import java.util.Set;

public interface MultiMap<K, V> {

    void clear();

    int size();

    boolean isEmpty();

    boolean containsKey(K key);

    Collection<V> get(K key);

    Set<K> keySet();

    Collection<Set<V>> values();

    Collection<V> put(K key, V value);

    Collection<V> remove(K key);

    Collection<V> remove(K key, V value);

    void putAll(K key, Collection<V> values);

}
