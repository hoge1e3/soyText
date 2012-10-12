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


public class MinMaxStr {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String t=new String(new char[]{32767,32767,32767});
		String t2=new String(new char[]{0});
		System.out.println(SDB.MIN_STRING.compareTo(t2)); // MIN_STR<t2
		System.out.println(SDB.MAX_STRING.compareTo(t));  // MAX_STR>t
		System.out.println(SDB.MAX_STRING);
		System.out.println(SDB.MAX_STRING.length());

	}

}