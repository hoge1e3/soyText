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
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.ReadAction;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.util.SFile;


public class DumpIndex {
	public static void main(String[] args) throws SQLException, ClassNotFoundException, NotInReadTransactionException, IOException {
		Common.parseArgs(args);
	    final SDB sdb=Common.workspace.getDB(Common.dbid);
	    SFile bkf=Common.workspace.getBackupDir(Common.dbid).rel("index.dump.txt");
	    final PrintStream st=new PrintStream(bkf.outputStream());
		sdb.readTransaction(new ReadAction() {
			@Override
			public void run(JDBCHelper db) throws SQLException, NotInReadTransactionException {
				IndexRecord ir = new IndexRecord();
				JDBCRecordCursor<IndexRecord> cur=sdb.indexTable().all();
				while (cur.next()) {
					ir=cur.fetch();
					st.println(ir.id+"\t"+ir.document+"\t"+ir.name+"\t"+ir.value);
					/*if (ir.name.equals("INDEX_CLASS")) {
						System.out.println(ir.document+"の"+ir.name+"は"+ir.value+"だ");
					}*/
				}
				cur.close();
			}
		});
		st.close();
		sdb.close();
	}
}