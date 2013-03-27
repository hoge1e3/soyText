package jp.tonyu.soytext2.js;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.PairSet;

public class IndexUpdateContext implements Wrappable {
	PairSet<String,String> idx;
	public IndexUpdateContext(PairSet<String, String> idx) {
		this.idx=idx;
	}
	public void add(String key, String value) {
		idx.put(key, value);
	}

}
