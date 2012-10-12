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

package jp.tonyu.soytext2.search.expr;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;



public class AndExpr extends QueryExpression implements Iterable<QueryExpression> {
	List<QueryExpression> conditions=new Vector<QueryExpression>();
	@Override
	public Iterator<QueryExpression> iterator() {
		return conditions.iterator();
	}
	@Override
	public QueryResult matches(DocumentScriptable d) {
		boolean fm=true, tm=true;
		//boolean debugsw=toString() .indexOf("aiu")>=0;//SearchLog".equals( d.str("type") );
		for (QueryExpression c:conditions) {
			QueryResult r=c.matches(d);
			fm &= r.filterMatched;
			tm &= r.templateMatched;
			//if (debugsw) {
			//}
			if (fm==false && tm==false) break;
		}
		return new QueryResult(fm,tm);
	}
	public void add(QueryExpression cond) {
		conditions.add(cond);
	}
	@Override
	public String toString() {
		StringBuilder b=new StringBuilder();
		for (QueryExpression c:conditions) {
			b.append(c+" ");
		}
		return b.toString();
	}
}