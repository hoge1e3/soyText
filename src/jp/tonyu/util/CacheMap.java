package jp.tonyu.util;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;


public class CacheMap<K, V> implements Map<K,V> {
    HashMap<K, SoftReference<V>> h=new HashMap<K, SoftReference<V>>();

    public int size() {
        pack();
        return h.size();
    }

    public boolean isEmpty() {
        pack();
        return h.isEmpty();
    }

    public V get(Object key) {
        SoftReference<V> softReference = h.get(key);
        if (softReference==null) return null;
        return softReference.get();
    }

    public boolean equals(Object o) {
        return h.equals(o);
    }

    public boolean containsKey(Object key) {
        return get(key)!=null;
    }

    public V put(K key, V value) {
        if (value==null) throw new RuntimeException("Cannot put null key="+key);
        h.put(key, new SoftReference<V>(value) );
        return value;
    }

    public int hashCode() {
        return h.hashCode();
    }

    public String toString() {
        return h.toString();
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e:m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public V remove(Object key) {
        return h.remove(key).get();
    }

    public void clear() {
        h.clear();
    }

    public boolean containsValue(Object value) {
        return h.containsValue(value);
    }

    public Object clone() {
        return h.clone();
    }

    public Set<K> keySet() {
        return h.keySet();
    }

    public Collection<V> values() {
        final Collection<SoftReference<V>> values = h.values();
        return new Collection<V>() {
            @Override
            public int size() {
                return values.size();
            }
            @Override
            public boolean isEmpty() {
                return values.isEmpty();
            }
            @Override
            public boolean contains(Object o) {
                for (SoftReference<V> e:values) {
                    if (e.equals(o)) return true;
                }
                return false;
            }
            @Override
            public Iterator<V> iterator() {
                final Iterator<SoftReference<V>> it = values.iterator();
                return new Iterator<V>() {
                    V cur;
                    @Override
                    public boolean hasNext() {
                        while (it.hasNext()) {
                            cur = it.next().get();
                            if (cur!=null) return true;
                            it.remove();
                        }
                        return false;
                    }
                    @Override
                    public V next() {
                        if (cur==null && !hasNext()) throw new NoSuchElementException();
                        return cur;
                    }

                    @Override
                    public void remove() {
                    }
                };
            }

            @Override
            public Object[] toArray() {
                throw new RuntimeException("Stub!");
            }

            @Override
            public <T> T[] toArray(T[] a) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean add(V e) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean remove(Object o) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean addAll(Collection<? extends V> c) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public void clear() {
                values.clear();
            }
        };
    }

    public Set<Map.Entry<K, V>> entrySet() {
        final Set<Entry<K, SoftReference<V>>> s = h.entrySet();
        return new Set<Map.Entry<K,V>>() {

            @Override
            public int size() {
                return s.size();
            }

            @Override
            public boolean isEmpty() {
                return s.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                final Iterator<Entry<K, SoftReference<V>>> it = s.iterator();
                return new Iterator<Map.Entry<K,V>>() {
                    Map.Entry<K, SoftReference<V>> cur;
                    @Override
                    public boolean hasNext() {
                        while (it.hasNext()) {
                            cur=it.next();
                            if (cur!=null) return true;
                            it.remove();
                        }
                        return false;
                    }

                    @Override
                    public Map.Entry<K, V> next() {
                        if (cur==null && !hasNext()) throw new NoSuchElementException();
                        final Entry<K, SoftReference<V>> e = cur;
                        return new Map.Entry<K, V>() {

                            @Override
                            public K getKey() {
                                return e.getKey();
                            }

                            @Override
                            public V getValue() {
                                return e.getValue().get();
                            }

                            @Override
                            public V setValue(V value) {
                                return CacheMap.this.put(e.getKey(), value);
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }

            @Override
            public Object[] toArray() {
                throw new RuntimeException("Stub!");
            }

            @Override
            public <T> T[] toArray(T[] a) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean add(java.util.Map.Entry<K, V> e) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean remove(Object o) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean addAll(
                    Collection<? extends java.util.Map.Entry<K, V>> c) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new RuntimeException("Stub!");
            }

            @Override
            public void clear() {
                throw new RuntimeException("Stub!");
            }
        };
    }
    public int pack() {
        int c=0;
        Iterator<K> it = h.keySet().iterator();
        while (it.hasNext()) {
            K k = it.next();
            if (!containsKey(k)) {c++; it.remove();}
        }
        return c;
    }

}
