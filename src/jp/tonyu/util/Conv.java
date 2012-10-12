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

public class Conv {
	public static <T> T convert(Object src, Class<T> type) {
		if (type.isAssignableFrom(Integer.class)) {
			return (T)(Object)Integer.parseInt(src+"");
		}
		return (T)src;
	}
	public static void main(String[] args) {
		int x=convert("3",Integer.class);
		System.out.println(x+5);
		System.out.println((String.class).isAssignableFrom(Object.class));
		System.out.println((Object.class).isAssignableFrom(String.class));
	}
}