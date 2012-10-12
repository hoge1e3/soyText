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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jp.tonyu.util.Ref;
import jp.tonyu.util.Util;


public class Genereflect {
	public Ref<String> s;
	public Ref<Integer> i;
	public static <T> T go(T t) {
		Genereflect g = new Genereflect();
		return (T) g;
	}
	public static void main(String[] args) throws SecurityException, NoSuchFieldException {
		ParameterizedType t = (ParameterizedType) Genereflect.class.getField("s").getGenericType();
		System.out.println(Util.join(",", t.getActualTypeArguments()));
//		System.out.println(t.getClass());
//		System.out.println(Util.join(",", t));
		/*System.out.println(Long.class.isAssignableFrom(Integer.class));
		System.out.println(Integer.class.isAssignableFrom(Long.class));
		Ref<Integer> i=Ref.create(3);
		Ref x=i;
		x.set("baka");
		System.out.println(x.get());
		int t=i.get();
		System.out.println(t);
		*/
		
	}
}