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

package jp.tonyu.soytext2.command;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;


public class JSTest {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Common.parseArgs(new String[]{"2.2010.tonyu.jp"});
		SDB s=Common.getDB();
		DocumentLoader l=new DocumentLoader(s);
		l.byId("9633@1.2010.tonyu.jp");
		//
		//l.byId("19389@2.2010.tonyu.jp");
		//l.byId("19387@2.2010.tonyu.jp"); //"956@1.2010.tonyu.jp");
		//l.byId("19336@2.2010.tonyu.jp");
		//l.byId("19325@2.2010.tonyu.jp");
		//l.byId("19323@2.2010.tonyu.jp");
		/*Context cx=Context.enter();
		ScriptableObject sscope = cx.initStandardObjects();
		//BlankScriptableObject sscope = new BlankScriptableObject();
		ScriptableObject.putProperty(sscope, "$", l);
		cx.evaluateString(sscope,
				//"var DyconsWeek=$.byId(\"956@1.2010.tonyu.jp\");"+
				//"var TClass=$.byId(\"2695@1.2010.tonyu.jp\");"+
				//"var HttpHelper=$.byId(\"165@1.2010.tonyu.jp\");"+
				"var dyconsTop=$.byId(\"938@1.2010.tonyu.jp\");"+
				""
						, "sourceName", 1, null);
		Context.exit();
*/
		//l.byId("950@1.2010.tonyu.jp");
		//l.byId("956@1.2010.tonyu.jp"); // NoEx
		//l.byId("2695@1.2010.tonyu.jp");  //NoEx


		/*
		SDB s=new SDB(new File("test.db"));//, Origin.uid);
		DocumentLoader l=new DocumentLoader(s);
		l.search("", null, new BuiltinFunc() {

			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				Scriptable s=(Scriptable)args[0];
				System.out.println(s.get("id", s)+" - "+ s.get("_body", s));
				return null;
			}
		});*/
		s.close();
	}

}