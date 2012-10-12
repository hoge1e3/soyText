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

public class LogRecord extends JDBCRecord {


	/*static String schema="CREATE TABLE "+LOG_1+" (\n"+
    "   id INTEGER NOT NULL PRIMARY KEY,\n"+
    "   date TEXT NOT NULL,\n"+
    "   action TEXT,\n"+
    "   target TEXT,\n"+
    "   option TEXT\n"+
    ")\n"+
    "";*/
	boolean inDB=false;
	public int id;
	public String date;
	public String action,target,option;
	@Override
	public String[] columnOrder() {
		return new String[]{"id","date","action","target","option"};
	}
	@Override
	public String tableName() {
		return "LogRecord";
	}

	/*public LogRecord(int id) {
		super();
		this.id = id;
	}*/
	public static LogRecord create(int id) {
		LogRecord res = new LogRecord();
		res.id=id;
		return res;
	}


}