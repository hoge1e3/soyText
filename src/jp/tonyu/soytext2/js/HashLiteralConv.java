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

package jp.tonyu.soytext2.js;

import java.util.Scanner;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.HashBlob;
import jp.tonyu.util.Maps;
import jp.tonyu.util.Resource;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class HashLiteralConv {
	private static final String GENERATE_CONTENT = "generateContent";

	public static BuiltinFunc decompile=new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			Function fun=(Function)args[0];
			int indent=((Number)args[1]).intValue();
			Log.d("hashlit", " decomp with ind="+indent);
			return cx.decompileFunction(fun, indent);
		}
	};
	public static BuiltinFunc isDocument=new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			Object fun=args[0];
			return fun instanceof DocumentScriptable;
		}
	};
	   public static BuiltinFunc isHashBlob=new BuiltinFunc() {

	        @Override
	        public Object call(Context cx, Scriptable scope, Scriptable thisObj,
	                Object[] args) {
	            Object fun=args[0];
	            if (fun instanceof NativeJavaObject) {
                    NativeJavaObject n=(NativeJavaObject) fun;
                    Object o=n.unwrap();
                    Log.d("IsHashBlob", o);
                    return o instanceof HashBlob;
                }
	            return false;
	        }
	    };

	public static BuiltinFunc isJavaNative=new BuiltinFunc() {
		public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length==0) return null;
            if  (args[0] instanceof NativeJavaObject) return args[0].getClass().getCanonicalName() ;
			if  (args[0] instanceof Wrappable) return args[0].getClass().getCanonicalName() ;
			return null;
		}
	};
	static BuiltinFunc debug=new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args[0] instanceof NativeJavaArray) {
				NativeJavaArray jar = (NativeJavaArray) args[0];
				Object[] r= Scriptables.toArray(jar);
				for (Object o :r ){
					Log.d("TOHASH b", o);
				}
			}
			Log.d("TOHASHLIT", args[0]);
			return null;
		}
	};
	public static String toHashLiteral(Object res) {
		JSSession jss = DocumentLoader.curJsSesssion();
		Scriptable u=jss.utils;
		Function f=(Function)ScriptableObject.getProperty(u, GENERATE_CONTENT);
		return jss.call(f, new Object[]{res})+"";
	}


}