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

public class Pair<K, V> {
	public final K key;
	public final V value;
	public Pair(K k, V v) {
		super();
		this.key = k;
		this.value = v;
	}
	public static <K,V> Pair<K,V> create(K k,V v) {
		return new Pair<K,V>(k,v);
	}
	@Override
	public int hashCode() {
		return key.hashCode()+value.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair) {
			Pair t = (Pair) obj;
			return key.equals(t.key) && value.equals(t.value);
		}
		return false;
	}
	@Override
	public String toString() {
		return key+": "+value;
	}
}