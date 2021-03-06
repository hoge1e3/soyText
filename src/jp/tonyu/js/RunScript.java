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


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.js.SafeWrapFactory;
import jp.tonyu.util.Resource;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


class RunScript {
	 static Scriptable initObjCache=null;
	 public static Scriptable initObject(Context cx) {
		 if (initObjCache!=null) return initObjCache;
		 ScriptableObject o=cx.initStandardObjects();
		 String[] builtinsa=new String[] {"String","Boolean","Number","Function","Object","Array","RegExp","undefined","null","true","false"
				 ,"NaN","Infinity","Date","Math","parseInt","TypeError","InternalError"};
		 Set<String> builtins=new HashSet<String>();
		 for (String s:builtinsa) {
			 builtins.add(s);
		 }
		 for (Object i: o.getAllIds()) {
			 if (!builtins.contains(i)) {
				 o.delete((String)i);
			 } else {
				 Object object = o.get(i.toString(),null);
				 if (object instanceof NativeJavaObject) {
					NativeJavaObject j = (NativeJavaObject) object;
					Log.w("Runscript","Native java Object:"+j);
				}
			 }
		 }
		 cx.evaluateString(o, Resource.text(Prototype.class, ".js"), "<prototype>", 1, null);
		 initObjCache=o;
		 return o;
	 }

	 public static Object eval (String s, Map<String , Object> vars) {
         Context cx = Context.enter();
         try {
        	  Scriptable root=initObject(cx);
          	Scriptable scope=new ScriptableObject(root,root) {
				@Override
				public String getClassName() {
					return "scope";
				}
			};
             for (String k:vars.keySet()) {
                 scope.put(k, scope, vars.get(k));
             }
             cx.setWrapFactory(new SafeWrapFactory());
             Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
             return result;
         } catch (EvaluatorException e) {
        	 Log.die("JS -error at ||"+e.lineSource()+"|| "+e.details());
        	 return null;
         } finally {
             Context.exit();
         }
	 }
	 public static Object call(Function f, Object[] args) {
         Context cx = Context.enter();
         try {
     		 cx.setWrapFactory(new SafeWrapFactory());
        	 Scriptable scope=initObject(cx);
             Object result = f.call(cx, scope, scope, args);;
             return result;
         } finally {
             Context.exit();
         }
	 }
 }