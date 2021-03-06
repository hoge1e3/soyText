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

import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.LooseWriteAction;
import jp.tonyu.soytext2.search.AndQueryBuilder;
import jp.tonyu.soytext2.search.expr.AndExpr;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.soytext2.search.expr.QueryExpression;
import jp.tonyu.util.Ref;
import jp.tonyu.util.Resource;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class AndDBSearcher implements Wrappable {
	public final DBHelper dbscr;
	public AndDBSearcher(DBHelper dbscr) {
		super();
		this.dbscr = dbscr;
		qb=new AndQueryBuilder();
	}
	public AndDBSearcher(DBHelper dbscr, AndExpr andExpr) {
        super();
        this.dbscr = dbscr;
        qb=new AndQueryBuilder(andExpr);
	}
    private AndQueryBuilder qb;
	public void eachUpdate(final Function iter) {
		dbscr.loader.ltr.write(new LooseWriteAction() {

			@Override
			public void run() throws NotInWriteTransactionException {
				dbscr.loader.searchByQuery(qb.toQueryExpression(), iter);
			}
		});
	}

	public void each(Function iter) {
		dbscr.loader.searchByQuery(qb.toQueryExpression(), iter);
	}
	public Object template(final Function tmpl) {
		Scriptable r=(Scriptable)DocumentLoader.curJsSesssion().eval("dbtmp",
				Resource.text(DBTemplate.class,".js"));
		final Function add=(Function)ScriptableObject.getProperty(r, "add");
		dbscr.loader.searchByQuery(qb.toQueryExpression(), new BuiltinFunc() {

			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				Object res=tmpl.call(cx, scope, thisObj, args);
				if (res==null || res instanceof Undefined) return true;
				add.call(cx, scope, thisObj, new Object[]{res});
				return false;
			}
		});
		return ScriptableObject.getProperty(r, "node");
	}
	public AndDBSearcher q(String name, Object value) {
		qb=qb.attr(name,value,AttrOperator.ge);
		return this;
	}
	public AndDBSearcher q(String name) {
		qb.keyword(name);
		return this;
	}
	public AndDBSearcher qe(String name, Object value) {
		qb=qb.attr(name,value,AttrOperator.exact);
		return this;
	}

	public Object find1() {
		final Ref<Object> res=new Ref<Object>();
		QueryExpression query = qb.toQueryExpression();
		Log.d(this, "Find1 : "+query);
		dbscr.loader.searchByQuery(query, new BuiltinFunc() {

			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				res.set(args[0]);
				Log.d(this,"Find 1: found "+args[0]);
				return true;
			}
		});
		if (res.isSet()) return res.get();
		return null;
	}
	public AndDBSearcher backlinks(String docId) {
		qb.backlinks(docId);// attr(IndexRecord.INDEX_REFERS, value, AttrOperator.exact);
		return this;
	}
	public AndDBSearcher is(String klassId) {
		qb.instof(klassId);
		return this;
	}
}