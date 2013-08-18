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
import java.util.Iterator;
import java.util.Set;

import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.soytext2.servlet.FileWorkspace;
import jp.tonyu.util.ArgsOptions;
import jp.tonyu.util.SFile;

public class RebuildIndex {
	public static void main(String[] args) throws Exception {
		FileWorkspace workspace=new FileWorkspace(new SFile("."));
		ArgsOptions opt=new ArgsOptions(args);
		String dbid=(opt.args.length==0?workspace.getPrimaryDBID():opt.args[0]);

        JSSession.optimize=false;
        final SDB s=workspace.getDB(dbid);// new SDB(f);
        final DocumentLoader d = new DocumentLoader(s);
        Set<String> ids = d.allIds();
		workspace.closeDB(dbid);
		Iterator<String> it = ids.iterator();
		while (it.hasNext()) {
            final SDB s2=workspace.getDB(dbid);
            final DocumentLoader d2 = new DocumentLoader(s2);
            d2.rebuildIndex(it, 1000);
            workspace.closeDB(dbid);
		}
	}
}