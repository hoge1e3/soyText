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
import java.util.HashSet;
import java.util.Set;

import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.soytext2.servlet.FileWorkspace;
import jp.tonyu.util.SFile;

public class RestoreFromRealtimeBackup {
	public static void main(String[] args) throws Exception  {
		FileWorkspace workspace=new FileWorkspace(new SFile("."));
		String dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);
		SDB s=workspace.getDB(dbid);
		restore(s);
		s.close();
	}
	// Why this is a method of SDB? because it creates a DocumentLoader.
	public static void restore(SDB s) throws SQLException, NotInWriteTransactionException {
		SFile rd=s.realtimeBackupDir();
		DocumentRecord d=new DocumentRecord();
		Set<String> updated=new HashSet<String>();
		for (SFile r:rd) {
			try {
				if (r.isDir()) continue;
				s.restoreFromRealtimeBackup(r, updated);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		DocumentLoader l=new DocumentLoader(s);
		for (String id:updated) {
			Log.d("Restore", "Refresh index of "+id);
			DocumentScriptable ds = l.byIdOrNull(id);
			if (ds==null) Log.d("Restore", "Why!? "+id+" is not exist!!");
			ds.refreshIndex();
		}
		s.close();
	}
}