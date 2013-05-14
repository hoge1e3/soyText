package jp.tonyu.soytext2.js;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.PairSet;

public class IndexUpdateContext implements Wrappable {
	PairSet<String,String> idx;
	DocumentScriptable target;
	public IndexUpdateContext(DocumentScriptable target, PairSet<String, String> idx) {
	    this.target=target;
		this.idx=idx;
	}
	public DocumentScriptable getTarget() {
        return target;
    }
	public void add(String key, String value) {
		idx.put(key, value);
	}

}
