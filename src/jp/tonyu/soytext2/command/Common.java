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
import java.sql.SQLException;

import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.soytext2.servlet.FileWorkspace;
import jp.tonyu.util.SFile;

public class Common {
	static String dbid;
	static FileWorkspace workspace;
	/**
	 * Parse args with
	 *   java App [DBID]
	 *     DB Dir is pwd/db/DBID/
     *     DB File is pwd/db/DBID/main.db
	 * @param args
	 * @throws IOException
	 * @throws SQLException
	 */
	static void parseArgs(String []args) throws IOException, SQLException {
		workspace=new FileWorkspace(new SFile("."));
		dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);
	}
	static private SDB _sdb;
	static SDB getDB() throws SQLException, IOException, ClassNotFoundException {
		if (_sdb!=null) return _sdb;
		return _sdb=workspace.getDB(dbid);
	}
	static void backupDB() throws IOException {
		SFile dbFile = workspace.getDBFile(dbid);//  new SFile( dbFilef );
		if (dbFile.exists()) {
			boolean res=dbFile.moveAsBackup("backup"); // can not move to other dir
			if (!res) {
				throw new IOException("Move "+dbFile+" fail");
			}
		}
	}
	static void closeDB() throws SQLException, IOException, ClassNotFoundException, NotInReadTransactionException {
		workspace.closeDB(dbid);
		_sdb=null;

	}
}