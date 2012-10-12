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

package jp.tonyu.soytext2.extjs;

import java.util.HashSet;

import jp.tonyu.soytext2.js.DocumentScriptable;

public class ClassDef {
	public final HashSet<String> fields=new HashSet<String>();
	public final HashSet<String> methods=new HashSet<String>();
	public final DocumentScriptable src;
	public final ClassDef superClass;
	public ClassDef(DocumentScriptable src, ClassDef superClass) {
		this.src=src;
		this.superClass=superClass;
	}
	public boolean isField(String name) {
		return fields.contains(name) || superClass!=null && superClass.isField(name);
	}
	public boolean isMethod(String name) {
		return methods.contains(name) || superClass!=null && superClass.isMethod(name);
	}
	@Override
	public String toString() {
		StringBuffer b=new StringBuffer("{");
		if (superClass!=null) {
			b.append("SuperClass =");
			b.append(superClass);
		}
		b.append(", Fields=");
		b.append(fields);
		b.append(", Methods=");
		b.append(methods);
		return b+"}";
	}
}