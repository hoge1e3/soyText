package jp.tonyu.util;

import java.util.Map;

public class H {
	public static <K2,V2> HashBuilder<K2,V2> p(K2 key,V2 value) {
		return new HashBuilder<K2, V2>().p(key, value);
	}
	public static <K2,V2> HashBuilder<K2,V2> p(Map<K2,V2> hash) {
		return new HashBuilder<K2, V2>().p(hash);
	}
}
