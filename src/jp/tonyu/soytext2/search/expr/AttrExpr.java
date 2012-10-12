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

import org.mozilla.javascript.ScriptableObject;


public class AttrExpr extends QueryExpression implements Comparable<AttrExpr> {
	final public String name;
	final Object searchValue;
	public String getKey() {
		return name;
	}
	public Object getValue() {
		return searchValue;
	}
	AttrOperator op;
	@Override
	public int compareTo(AttrExpr other) {
		return name.compareTo(other.name);
	}
	public String name() {return name;}
	public AttrExpr(String name, Object value, AttrOperator op) {
		super();
		if (name.length()==0) Log.die("name is empty");
		if(value==null) Log.d(this,"searchValue is null");
		this.name = name;
		this.searchValue = value;
		this.op = op;
	}
	@Override
	public QueryResult matches(DocumentScriptable d) {
		Object actualValue=ScriptableObject.getProperty(d,name);
		return new QueryResult(matches(actualValue));
	}
	public boolean matches(Object actualValue) {
		boolean res;
		if (actualValue==null) return false;
		if (op==AttrOperator.exact) {
			res= actualValue.equals(searchValue);
		} else if (op==AttrOperator.ge) {
			String srcStr=searchValue+"";
			String actStr=actualValue.toString();
			// avalue > svalue     name:svalue
			res=actStr.indexOf(srcStr)>=0;
		} else {
			String srcStr=searchValue+"";
			String actStr=actualValue.toString();
			// avalue<svalue   name:<svalue
			res=srcStr.indexOf(actStr)>=0;
		}
		return res;
	}
	@Override
	public String toString() {
		return name+AttrOperator.toString(op)+searchValue;
	}
}