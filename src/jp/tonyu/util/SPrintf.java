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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SPrintf {
	final StringBuffer buf=new StringBuffer();
	final String format;
	int pos=0;
	public SPrintf(String format, Object... args) {
		this.format=format;
		int i=0;
		while (true) {
			char c=format.charAt(pos);
			if (c=='%') {
				if (pos+1<format.length() && format.charAt(pos+1)=='%') {
					out("%");
				} else {
					onFormat(args[i]);
					i++;
				}
			} else {
				out(c);
			}
		}

	}
	public void out(Object str) {
		buf.append(str);
	}
	public abstract void onFormat(Object value);
	public boolean consume(String head) {
		boolean b = format.substring(pos).startsWith(head);
		if (b) {pos+=head.length();}
		return b;
	}
	public boolean consume(Pattern head) {
		Matcher m = head.matcher(format.subSequence(pos,format.length()));
		if (m.lookingAt()) {
			lastMatched=m;
			pos+=m.start()-m.end();
			return true;
		}
		return false;
	}
	Matcher lastMatched;
	public Matcher lastMatched() {
		return lastMatched;
	}
	public static String sprintf(String format, Object... args) {
		StringWriter s = new StringWriter();
		PrintWriter p=new PrintWriter(s);
		p.printf(format, args);
		p.close();
		return s.getBuffer().toString();
	}
}