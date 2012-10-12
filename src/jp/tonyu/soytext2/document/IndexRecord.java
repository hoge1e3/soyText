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

package jp.tonyu.soytext2.document;

import jp.tonyu.db.JDBCRecord;

public class IndexRecord extends JDBCRecord {
	public static final String NAME_VALUE_LAST_UPDATE = "name,value,-lastUpdate";
	public int id;
	public String document,name,value;
	public long lastUpdate;
	// value = "s{String}";  value="d{ID}";
	public static final String DEFINED_INDEX_NAMES="#DEFINED_INDEX_NAMES";
	public static final String INDEX_REFERS="#REFERS";
	public static final String INDEX_INSTANCEOF="#INSTANCEOF";

	//public static final boolean useIndex=false;
	public IndexRecord() {
		super();
	}
	@Override
	public String tableName() {
		return "IndexRecord";
	}
	@Override
	public String[] indexSpecs() {
		return q(NAME_VALUE_LAST_UPDATE,"document");
		// "document" needs on removing indexes, removing needed on updating document
	}

}