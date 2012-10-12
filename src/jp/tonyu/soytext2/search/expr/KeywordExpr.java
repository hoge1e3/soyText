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

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;

public class KeywordExpr extends QueryExpression {
	String keyword;
	@Override
	public QueryResult matches(DocumentScriptable d) {
		if (d==null) Log.die("Query d is null");
		if (d.getDocument()==null) Log.die(d+" getDocument is null");
		if (d.getDocument().content==null) Log.die(d+"/"+ d.getDocument()+"  getDocument.content is null");
		return new QueryResult( d.getDocument().content.toLowerCase().indexOf(keyword)>=0 );
	}
	public KeywordExpr(String keyword) {
		super();
		this.keyword = keyword.toLowerCase();
	}
	@Override
	public String toString() {
		return keyword;
	}
	public String getKeyword() {
		return keyword;
	}

}