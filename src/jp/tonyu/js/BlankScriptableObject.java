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


import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class BlankScriptableObject extends ScriptableObject {

	/**
	 *
	 */
	private static final long serialVersionUID = 3588030671162977794L;

	public BlankScriptableObject(Scriptable scope) {
		super(scope, null);
	}
	public BlankScriptableObject() {
		super();
	}

	@Override
	public String getClassName() {
		return "Blank";
	}
	/*public void put(String key,Object value) {
		put(key,this,value);
	}
	public Object get(String key) {
		return get(key,this);
	}*/
}