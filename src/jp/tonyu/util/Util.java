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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Util {
	public static <T> T toSingleton(Collection<T> l) {
		return toSingleton(l.iterator());
	}
	public static <T> T toSingleton(Iterator<T> it) {
		if (!it.hasNext()) return null;
		return it.next();
	}
	public static <T> T toSingleton(T[] l) {
		if (l.length==0) return null;
		return l[0];
	}
	public static String encodeTabJoin(Object[] objects) {
		String sep="";
		StringBuffer res=new StringBuffer();
		for (Object e:objects) {
			res.append(sep);sep="\t";
			res.append(encodeTab(e+""));
		}
		return res.toString();
	}
	/*public static <T> T[] copyArray(T[] dst, List<T> src) {
		return dst;
	}*/
	private static String encodeTab(String t) {
		return t
		 .replaceAll("\\\\", "\\\\\\\\")
		 .replaceAll("\\r" , "\\\\r")
		 .replaceAll("\\t" , "\\\\t")
		 .replaceAll("\\n" , "\\\\n")
		;
	}
	
	public static String[] decodeTabJoin(String line) {
		String []lines=line.split("\t");
		String []res=new String[lines.length];
		for (int i=0 ; i<lines.length ; i++) {
			res[i]=decodeTab(lines[i]);
		}
		return res;
	}
	private static String decodeTab(String t) {
		return t
		 .replaceAll("\\\\r" , "\\r")
		 .replaceAll("\\\\n" , "\\n")
		 .replaceAll("\\\\t" , "\\t")
		 .replaceAll("\\\\\\\\", "\\\\")
	    ;
	}
	public static Object join(String sep, Object[] array) {
		String ss="";
		StringBuilder b=new StringBuilder();
		for (Object e: array) {
			b.append(ss+e);
			ss=sep;
		}
		return b.toString();
	}
}