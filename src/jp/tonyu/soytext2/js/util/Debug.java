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

package jp.tonyu.soytext2.js.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.LineInfo;

public class Debug implements Wrappable {
	public Object[] stackTrace(Object eo) {
		String s = rawStackTrace(eo);
		Vector<Object> res=new Vector<Object>();
		for (String l : s.split("\n")) {
			Matcher m = docscr.matcher(l);
			if (m.find()) {
				LineInfo li=new LineInfo();
				li.id=m.group(1);
				li.lineNo=Integer.parseInt(m.group(2));
				res.add(li);
			}
		}
		return res.toArray();
	}
	public String rawStackTrace(Object eo) {
		Exception e = extractException(eo);
		if (e==null) {
			Log.d("js.Debug",eo+"("+eo.getClass()+") has no exception info");
			return e+"";
		}
		StringWriter w=new StringWriter();
		e.printStackTrace(new PrintWriter(w));
		try {
			w.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String s=w.getBuffer().toString();
		return s;
	}
	public Exception extractException(Object eo) {
		Exception e=null;
		if (eo instanceof Exception) {
			e = (Exception) eo;
		} else if (eo instanceof Scriptable) {
			Scriptable es = (Scriptable) eo;
			Object eo2=ScriptableObject.getProperty(es, "rhinoException");
			if (eo2 instanceof Exception) {
				e = (Exception) eo2;
			} else if (eo2 instanceof NativeJavaObject){
				NativeJavaObject jo=(NativeJavaObject)eo2;
				Object wno=jo.unwrap();
				if (wno instanceof Exception) {
					e = (Exception) wno;
				} else {
					Log.d("js.Debug","wno="+wno);
				}
			} else {
				Scriptables.each(es, new StringPropAction() {

					@Override
					public void run(String key, Object value) {
						Log.d("js.Debug",key+"="+value);
					}
				});
			}

		}
		return e;
	}
	public void d(Object tag,Object o) {
	    Log.d(tag, o);
	}
	static Pattern docscr=Pattern.compile("Docscr ([^\\(\\)]+)[^:]*:(\\d+)");

}