/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.tonyu.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapBuilder<K,V> implements Map<K,V> {
	public MapBuilder(K key, V value) {
		put(key, value);
	}
	public MapBuilder<K, V> p(K key,V value) {
		src.put(key, value);
		return this;
	}
	public final Map<K,V> src=new HashMap<K, V>();
	
	public void clear() {
		src.clear();
	}
	public boolean containsKey(Object key) {
		return src.containsKey(key);
	}
	public boolean containsValue(Object value) {
		return src.containsValue(value);
	}
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return src.entrySet();
	}
	public boolean equals(Object o) {
		return src.equals(o);
	}
	public V get(Object key) {
		return src.get(key);
	}
	public int hashCode() {
		return src.hashCode();
	}
	public boolean isEmpty() {
		return src.isEmpty();
	}
	public Set<K> keySet() {
		return src.keySet();
	}
	public V put(K key, V value) {
		return src.put(key, value);
	}
	public void putAll(Map<? extends K, ? extends V> m) {
		src.putAll(m);
	}
	public V remove(Object key) {
		return src.remove(key);
	}
	public int size() {
		return src.size();
	}
	public Collection<V> values() {
		return src.values();
	}
	
}