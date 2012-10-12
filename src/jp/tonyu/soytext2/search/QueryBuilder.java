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

package jp.tonyu.soytext2.search;

import java.util.SortedSet;
import java.util.TreeSet;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.soytext2.search.expr.InstanceofExpr;
import jp.tonyu.soytext2.search.expr.QueryExpression;

public class QueryBuilder {
	String cond;
//	DocumentSet documentSet;
	boolean emptyCond=false;
	SortedSet<AttrExpr> tmpls=new TreeSet<AttrExpr>();
	private QueryBuilder(String cond) {
		this.cond=cond;
		if (cond==null) {
			emptyCond=true;
			this.cond="";
		}
		//this.documentSet=documentSet;
	}
	public static QueryBuilder create(String cond) {
		return new QueryBuilder(cond);
	}
	public QueryBuilder tmpl(String name, Object value, AttrOperator op) {
		tmpls.add(new AttrExpr(name,value,op));
		if (emptyCond) {
			cond+=name+AttrOperator.toString(op)+"? ";
		}
		Log.d(this, "Cur Conds -"+cond+" tmpls - "+tmpls);
		return this;
	}
	public void addCond(String c) {
		cond+=c+" ";
	}
	/*public Query toQuery() {
		return new Query(cond, tmpls);
	}*/
}