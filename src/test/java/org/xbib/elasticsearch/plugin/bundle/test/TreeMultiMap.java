package org.xbib.elasticsearch.plugin.bundle.test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TreeMultiMap<K, V> implements MultiMap<K, V> {

    private final Map<K, Set<V>> map = new TreeMap<>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Set<V>> values() {
        return map.values();
    }

    @Override
    public Collection<V> put(K key, V value) {
        Set<V> set = map.get(key);
        if (set == null) {
            set = new TreeSet<>();
        }
        set.add(value);
        return map.put(key, set);
    }

    @Override
    public void putAll(K key, Collection<V> values) {
        Set<V> set = map.get(key);
        if (set == null) {
            set = new LinkedHashSet<>();
            map.put(key, set);
        }
        set.addAll(values);
    }

    @Override
    public Collection<V> get(K key) {
        return map.get(key);
    }

    @Override
    public Set<V> remove(K key) {
        return map.remove(key);
    }

    @Override
    public Set<V> remove(K key, V value) {
        Set<V> set = map.get(key);
        if (set != null) {
            set.remove(value);
        }
        return set;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof TreeMultiMap && map.equals(((TreeMultiMap) obj).map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
