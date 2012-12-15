package jp.tonyu.util;

import java.util.Hashtable;
import java.util.Map;

public class HashBuilder<K,V> extends  Hashtable<K, V>  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -85903288338835957L;
	//public final Hashtable<K, V> h=new Hashtable<K, V>();
	public static <K2,V2> HashBuilder<K2,V2> s(K2 key,V2 value) {
		return new HashBuilder<K2, V2>().p(key, value);
	}
	public HashBuilder<K, V> p(K key, V value) {
		put(key, value);
		return this;
	}
	public HashBuilder<K, V> p(Map<K,V> h) {
		for (K k: h.keySet()) {
			p(k,h.get(k));
		}
		return this;
	}
	
}
