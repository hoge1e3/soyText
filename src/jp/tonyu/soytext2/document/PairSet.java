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

package jp.tonyu.soytext2.document;

import java.util.HashSet;
import java.util.Iterator;

public class PairSet<K,V> implements Iterable<Pair<K,V>>{
	HashSet<Pair<K, V>> h=new HashSet<Pair<K,V>>();
	public void put(final K key,final V value) {
		h.add(Pair.create(key,value));
	}
	@Override
	public Iterator<Pair<K, V>> iterator() {
		return h.iterator();
	}
	@Override
	public String toString() {
		StringBuilder b=new StringBuilder();
		for (Pair<K,V> p:h) {
			b.append(p+", ");
		}
		return "{"+b+"}";
	}
}