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

package jp.tonyu.js;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

public class Args implements Wrappable {
	static Pattern func=Pattern.compile("function\\s*[\\$\\w\\d]*\\(([^\\)]*)\\)");
	public static String[] getArgs(Function f) {
		boolean ent=(Context.getCurrentContext()==null);
		if (ent) Context.enter();
		String fs=""+f.getDefaultValue(String.class);
		if (ent) Context.exit();
		Matcher m=func.matcher(fs);
		if (m.find()) {
			String[] res=m.group(1).split(",");
			for (int i=0 ; i<res.length; i++) {
				res[i]=res[i].replaceAll("[^\\$\\w\\d]", "");
			}
			return res;
		}
		//System.out.println(fs+" not found");
		return null;
	}
	public static void main(String[] args) {
		Object res=RunScript.eval("res=function (A, b, c) {}; ", new HashMap<String,Object>());
		String[] a=getArgs((Function)res);
		for (int i=0 ; i<a.length; i++) {
			System.out.println(a[i]);
		}
	}
}