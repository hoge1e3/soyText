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

package jp.tonyu.soytext2.js;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.Vector;

import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.TransactionMode;
import jp.tonyu.debug.Log;
import jp.tonyu.js.AllPropAction;
import jp.tonyu.js.NumberPropAction;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.HashBlob;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.LooseWriteAction;
import jp.tonyu.soytext2.document.PairSet;
import jp.tonyu.soytext2.file.ReadableBinData;
import jp.tonyu.soytext2.search.QueryBuilder;
import jp.tonyu.soytext2.search.QueryExpressionParser;
import jp.tonyu.soytext2.search.expr.AttrOperator;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * DBHelper is referenced by 'db' in content
 * @author shinya
 *
 */
public class DBHelper implements Wrappable{
	public final DocumentLoader loader;

	public Object q(Object value) {
		if (value instanceof String) {
			return new AndDBSearcher(this, QueryExpressionParser.parse((String)value));
		} else if (value instanceof DocumentScriptable){
			DocumentScriptable ds=(DocumentScriptable)value;
			return new AndDBSearcher(this).is(ds.id());
		} else {
			Log.die("Not a query value - "+value);
			return null;
		}
	}
	public AndDBSearcher is(Object value) {
		if (value instanceof DocumentScriptable){
			DocumentScriptable ds=(DocumentScriptable)value;
			return new AndDBSearcher(this).is(ds.id());
		}
		Log.die("db.is fail "+value+" is not document");
		return null;
	}
	public AndDBSearcher backlinks(DocumentScriptable value) {
		return new AndDBSearcher(this).backlinks(value.id());
	}
	public AndDBSearcher q(String name, Object value) {
		return new AndDBSearcher(this).q(name, value);
	}
	public DBHelper(DocumentLoader loader) {
		super();
		this.loader = loader;
	}
	public AndDBSearcher qe(String name, Object value) {
		return new AndDBSearcher(this).qe(name, value);
	}
	/*public Object find(Function iter) {
		loader.search("", null, iter);
		return this;
	}*/

	public Object insert(Scriptable obj) {
		DocumentScriptable d = loader.newDocument(obj);
		return d;
	}
	/**
	 *   db.byId(id) returns null if id is not found.
         while $.byId(id) returns some DocumentScriptable even if it is not exist.
         it is for lazy loading of DocumentRecord to avoid much queries.
	 * @param id
	 * @return
	 */
	public Object byId(String id) {
		return loader.byIdOrNull(id);
	}
	public void debug(Object o) {
		Log.d("db.debug", o);
	}
	public Object byIdOrCreate(String id) {
		Object res=loader.byIdOrNull(id);
		if (res!=null) return res;
		return loader.newDocument(id);

	}
	public String getContent(DocumentScriptable d) {
		return d.getDocument().content;
	}
	public void setContentAndSave(DocumentScriptable d,String newContent) {
		d.setContentAndSave(newContent);
	}
	public String dbid() {
		return loader.getDocumentSet().getDBID();
	}
	public Object exportDocument(DocumentScriptable d) throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
	    Map<String, Object> m=d.getDocument().toMap();
	    return new MapScriptable(m);
	}
	public void importDocuments(final Scriptable scr) {
	    // must be imported updated record at once ( To avoid 404 link )
	    final Vector<DocumentRecord> recs=new Vector<DocumentRecord>();
	    Scriptables.each(scr, new AllPropAction() {
            @Override
            public void run(Object key, Object value) {
                if (value instanceof Scriptable) {
                    DocumentRecord d=new DocumentRecord();
                    Scriptable scr=(Scriptable) value;
                    Map<String, Object> m=Scriptables.toStringKeyMap(scr);
                    d.copyFrom(m);
                    recs.add(d);
                }
            }
        });
	    loader.ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                try {
                    loader.importDocuments(recs);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.die(e);
                }
            }
        });
	}

    public HashBlob hashBlob(String hash) {
        return loader.hashBlob(hash);
    }
    public HashBlob writeHashBlob(ReadableBinData i) throws IOException {
        return loader.writeHashBlob(i);
    }
    public HashBlob writeHashBlob(InputStream i) throws IOException {
        return loader.writeHashBlob(i);
    }
    public Object transactionStatus() {
        TransactionMode mode=loader.ltr.thisThreadMode();
        if (mode==null) return mode;
        return mode+"";
    }
    public void all(Function iter) {
        loader.all(iter);
    }
}