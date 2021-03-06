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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.parser.Parser;
import jp.tonyu.soytext2.search.expr.AndExpr;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.soytext2.search.expr.BackLinkExpr;
import jp.tonyu.soytext2.search.expr.InstanceofExpr;
import jp.tonyu.soytext2.search.expr.KeywordExpr;
import jp.tonyu.soytext2.search.expr.NotExpr;
import jp.tonyu.soytext2.search.expr.QueryExpression;

public class QueryExpressionParser {
	Parser p;
	public QueryExpressionParser(CharSequence s) {
		p=new Parser(s);
	}
	public AndExpr parse() {
		if (parseCond() && p.endOfSource()) {
			return (AndExpr)lastCond;
		}
		throw p.getLastError();
	}
	public boolean parseCond() {
		// query := condition
		// condition:= andcond
		return parseAndCond();
	}
	QueryExpression lastCond;
	public boolean parseAndCond() {
		// andcond := (singlecond)*
		AndExpr a=new AndExpr();
		while (parseNotSingleCond()) {
			a.add(lastCond);
			Log.d(this,"Adding cond = "+lastCond.getClass()+" : "+lastCond);
		}
		lastCond=a;
		return true;
	}
	static final Pattern notPat=Pattern.compile("-");
	//  notsinglecond := -?  singlecond
	public boolean parseNotSingleCond() {
		boolean negate=false;
		if (p.read(notPat)) {
			negate=true;
		}
		if (!parseSingleCond()) return false;
		if (negate) {
		    /*if (lastCond instanceof TemplateExpr) {
				Log.die("-"+lastCond+" not allowed");
			}*/
			lastCond=new NotExpr(lastCond);
		}
		return true;
	}
	public boolean parseSingleCond() {
		// singlecond := templateCond | attrcond | keywordcond
		return /*parseTemplateCond() ||*/ parseAttrCond() || parseKeywordCond();
	}
	static final Pattern keywordCondPat=Pattern.compile("[^\\s]+");
	private boolean parseKeywordCond() {
		// keywordcond := [^\s]+
		// TODO Auto-generated method stub
		Matcher m=p.matcher(keywordCondPat);
		if (m.lookingAt()) {
			//Debug.syslog("PArsed!! ("+m.group()+")");
			//if (m.group().indexOf(" ")>=0) Debug.die(m.group()+";Whey space contain?");
			lastCond=new KeywordExpr( m.group() );
			return true;
		}
		return false;
	}
	static final Pattern tmplCondPat=Pattern.compile("([^:\\s]+):[=<]?\\?");
	/*public boolean parseTemplateCond() {
		// tmplcond := ([^:]+):?
		Matcher m=p.matcher(tmplCondPat);
		if (m.lookingAt()) {
			String attrName=m.group(1);
			lastCond=new TemplateExpr(attrName);
			templates.add((TemplateExpr)lastCond);
			return true;
		}
		return false;
	}*/
	static final Pattern attrCondPat=Pattern.compile("([^:\\s]+)(:[=<]?)([^\\s]+)");
	public static final String BACKLINK="backlink";
	public static final String CLASS="class";
	public boolean parseAttrCond() {
		// attrcond := [^\:]+:[=<]?[^\s]+
		Matcher m=p.matcher(attrCondPat);
		if (m.lookingAt()) {
			String attrName=m.group(1);
			String compType=m.group(2);
			String attrValue=m.group(3);
			if (BACKLINK.equals(attrName)) {
				lastCond=new BackLinkExpr(attrValue);
			} else if (CLASS.equals(attrName)) {
				lastCond=new InstanceofExpr(attrValue);
			} else {
				lastCond=new AttrExpr(attrName, attrValue, AttrOperator.fromString(compType));
			}
			return true;
		}
		return false;
	}
	public static void main(String[] args) {
		System.out.println( new QueryExpressionParser("type:=SavedSearch condition:?").parse() );

	}
	public static AndExpr parse(String string) {
		return new QueryExpressionParser(string).parse();
	}
}