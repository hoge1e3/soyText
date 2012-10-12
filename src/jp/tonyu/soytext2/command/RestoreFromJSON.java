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

import jp.tonyu.soytext2.document.SDB;


public class RestoreFromJSON {
    // usage: java RestoreFromJSON [DBID]
    //   restores from newest ./db/DBID/backup/*.json into ./db/DBID/main.db
    //   default value of DBID is set in ./db/primaryDBID.txt

	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		/*Workspace workspace=new Workspace(new SFile("."));
		String dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);

		SFile dbFile = workspace.getDBFile(dbid);//  new SFile( dbFilef );
		if (dbFile.exists()) {
			boolean res=dbFile.moveAsBackup("backup"); // can not move to other dir
			if (!res) {
				System.out.println("Move fail");
				return;
			}
		}*/

		Common.parseArgs(args);
		Common.backupDB();
		SDB s=Common.getDB();
		s.restoreFromNewestJSON();
		s.close();
	}
}