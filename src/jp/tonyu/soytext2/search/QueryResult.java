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

package jp.tonyu.soytext2.search;

import jp.tonyu.debug.Log;


public class QueryResult {
 	public final boolean filterMatched, templateMatched;
	// matched                  T                T   <-  name：hoge    に  "name:?", {name:hoge} をマッチ
	// templateMatchable        F                T   <-  name:fuga    に "name:?", {name:hoge} をマッチ
	// notMatched;              F                F   <-  nameがないDoc に  "name:?"  をマッチ
 	//                          T                F   (とりあえず禁止  "-name:?" )

 	public QueryResult(boolean filterMatched,
 			boolean templateMatched) {
 		super();
 		this.filterMatched = filterMatched;
 		this.templateMatched = templateMatched;
 		if (filterMatched && !templateMatched) Log.die("This combination not allowed");
 	}
 	public QueryResult(boolean filterMatched) {
 		this(filterMatched,filterMatched); 
 	}
					
}
