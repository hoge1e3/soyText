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

package jp.tonyu.parser;

import jp.tonyu.js.Wrappable;



public class ParseError extends RuntimeException implements Wrappable {
	final CharSequence source;
	int p; 
	public ParseError(CharSequence src, int p,String mesg) {
		super(mesg+" at "+p);
		this.source=src+"";
		this.p=p;
	}
	public CharSequence getSource() {
		return source;
	}
	public int getPos() {
		return p;
	}
}