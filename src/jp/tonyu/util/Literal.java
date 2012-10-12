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

package jp.tonyu.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Literal {
	public static final Pattern DQ = Pattern.compile("\"(.*)\"");
	public static final Pattern SQ = Pattern.compile("'(.*)'");

	public static String toLiteralPreserveCR(String value) {
		return "\""+value.toString().replaceAll("\\\\","\\\\\\\\")
        .replaceAll("\r", "\\\\r")
        .replaceAll("\n", "\\\\n")
        .replaceAll("\"", "\\\\\"")+"\"";
	}
	public static String toLiteral(String value) {
		return "\""+value.toString().replaceAll("\\\\","\\\\\\\\")
        .replaceAll("\r", "")
        .replaceAll("\n", "\\\\n")
        .replaceAll("\"", "\\\\\"")+"\"";
	}
	public static String fromLiteral(String literal) {
		Matcher m = DQ.matcher(literal);
		if (m.matches()) {
			return fromQuoteStrippedLiteral(m.group(1));
		}
		m = SQ.matcher(literal);
		if (m.matches()) {
			return fromQuoteStrippedLiteral(m.group(1));
		}
		return null;
	}
	public static String fromQuoteStrippedLiteral(String strippedLiteral) {
		StringBuilder b=new StringBuilder();
		for (int i=0 ; i<strippedLiteral.length(); i++) {
			char c=strippedLiteral.charAt(i);
			if (c=='\\' && i+1<strippedLiteral.length()) {
				char d=strippedLiteral.charAt(i+1);
				if (d=='n') b.append("\n");
				else if (d=='t') b.append("\t");
				else if (d=='r') b.append("\r");
				else b.append(d);
				i++;
			} else b.append(c);
		}
		return b.toString();
	}
}