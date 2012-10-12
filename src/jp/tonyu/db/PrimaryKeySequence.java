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

package jp.tonyu.db;

import java.sql.SQLException;


public class PrimaryKeySequence {
	int lastNumber;
	public PrimaryKeySequence(final JDBCTable<? extends JDBCRecord> tbl) throws SQLException, NotInReadTransactionException {
		super();
		//JDBCHelper db = tbl.getDB();
		lastNumber=tbl.max(tbl.primaryKeyName());
		/*db.readTransaction(new DBAction() {

			@Override
			public void run(JDBCHelper db) throws SQLException {
			    OrderBy ord=new OrderBy().desc(tbl.primaryKeyName());
				JDBCRecordCursor<T> cur = tbl.order(ord);
				lastNumber=0;
				while (cur.next()) {
					JDBCRecord r = cur.fetch();
					Object v;
					try {
						v = r.getField(r.primaryKeyName()).get(r);
						if (v instanceof Integer) {
							Integer num = (Integer) v;
							if (num>lastNumber) lastNumber=num;
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					}
				}
			}
		},-1);*/
	}
	public int next() {
		lastNumber++;
		return lastNumber;
	}
	public int current() {
		return lastNumber;
	}
}