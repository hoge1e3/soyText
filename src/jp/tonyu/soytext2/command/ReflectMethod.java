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
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.util.Maps;


public class ReflectMethod implements Wrappable {
	/*public void foo24(int x,int y,int z,int t){
		System.out.println("foo24-4");
	}*/
	public void foo24(int x,String y){
		System.out.println("foo24is-2:"+(x+y));
	
	}
	public void foo24(String x,int y){
		System.out.println("foo24si-2:"+(x+y));
	
	}
	/*public static void main(String[] args) {
		JSSession s = JSSession.get("test");
		//; 
		Object r = s.eval("test","a=(function (a,b) {return a+b;})+\"baka\";", Maps.create("b",(Object)new ReflectMethod()) );
		System.out.println(r);

	}*/
}