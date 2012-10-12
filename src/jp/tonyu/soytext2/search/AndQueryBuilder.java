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
import jp.tonyu.soytext2.search.expr.AndExpr;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.soytext2.search.expr.BackLinkExpr;
import jp.tonyu.soytext2.search.expr.InstanceofExpr;
import jp.tonyu.soytext2.search.expr.KeywordExpr;
import jp.tonyu.soytext2.search.expr.QueryExpression;

public class AndQueryBuilder {
	AndExpr cond;
	public AndQueryBuilder(AndExpr cond) {
	    this.cond=cond;
	}
	public AndQueryBuilder() {
	    this(new AndExpr());
	}
	public AndQueryBuilder instof(String klassId) {
		cond.add(new InstanceofExpr(klassId));
		return this;
	}
	public AndQueryBuilder backlinks(String docId) {
		cond.add(new BackLinkExpr(docId));
		return this;
	}
	public AndQueryBuilder attr(String name, Object value, AttrOperator op) {
		cond.add(new AttrExpr(name,value,op));
		return this;
	}
	public AndQueryBuilder keyword(String keyword) {
		cond.add(new KeywordExpr(keyword));
		return this;
	}

	public QueryExpression toQueryExpression() {
	    return cond;
	}
	/*public Query toQuery() {
		return new Query(new QueryTemplate(cond));
	}*/
}