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
import java.util.Set;

import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.soytext2.servlet.FileWorkspace;
import jp.tonyu.util.SFile;


public class BackupToJSON {
    // usage: java BackupToJSON [DBID]
    //   backups ./db/DBID/main.db into ./db/DBID/backup/*.json
    //   default value of DBID is set in ./db/primaryDBID.txt
	public static void main(String[] args) throws SQLException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {
		FileWorkspace w=new FileWorkspace(new SFile("."));
		SDB s= args.length==0 ? s=w.getPrimaryDB() : w.getDB(args[0]) ;
		Set<String> ids = s.backupToJSON();
		SFile rbd=s.realtimeBackupDir();
		s.close();
		for (SFile rbf:rbd) {
			if (ids.contains( rbf.name() )) rbf.moveAsBackup("backup");
		}
	}
}