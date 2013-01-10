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

package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.util.Context;
import jp.tonyu.util.SFile;

public class FileWorkspace implements Workspace {
	public static final String MAIN_DB = "main.db";
	public static final String PRIMARY_DBID_TXT = "primaryDbid.txt";
	SFile home;

	public FileWorkspace(SFile home) {
		super();
		this.home = home;
	}
	public boolean isSkel() {
	    if (primaryDBFile().exists()) return false;
	    return multiDBHome().rel("skel").isDir();
	}
	public SFile multiDBHome() {
		SFile res=home.rel("db");
		res.mkdirs(true);
		//if (res.exists()) return res;
		return res;
	}
	// if directory not exists returns null, if directory exists and main.db does not exist, main.db is created
	/* (非 Javadoc)
	 * @see jp.tonyu.soytext2.servlet.SystemContext#getDB(java.lang.String)
	 */
	@Override
	public SDB getDB(String dbid) throws SQLException,IOException, ClassNotFoundException {
		//if (getPrimaryDBID().equals(dbid)) return getPrimaryDB();
		SFile db=singleDBHome(dbid);
		if (!db.exists()) return null;
		SFile f=db.rel(MAIN_DB);
		return dbFromFile(f,dbid);
	}
	/* (非 Javadoc)
	 * @see jp.tonyu.soytext2.servlet.SystemContext#getPrimaryDB()
	 */
	@Override
	public SDB getPrimaryDB() throws SQLException,IOException, ClassNotFoundException {
		/*SFile f=dbHome().rel(getPrimaryDBID());
		return dbFromFile(f);*/
		return getDB(getPrimaryDBID());
	}
	static Map<File, SDB> cache=new HashMap<File, SDB>();
	private SDB dbFromFile(SFile f,String dbid) throws SQLException, ClassNotFoundException {
		File ff=f.javaIOFile();
		SDB res=cache.get(ff);
		if (res!=null) return res;
		res=new SDB(this,dbid);
		cache.put(ff, res);
		return res;
	}
	/* (非 Javadoc)
	 * @see jp.tonyu.soytext2.servlet.SystemContext#getPrimaryDBID()
	 */
	@Override
	public String getPrimaryDBID() throws IOException {
		SFile id=primaryDBFile();
		return id.lines()[0];
	}
	private SFile primaryDBFile() {
		return multiDBHome().rel(PRIMARY_DBID_TXT);
	}
	public static final String DB_INIT_PATH = "jp/tonyu/soytext2/servlet/init/db";
	void setupDB() throws IOException {
		if (!isEmpty()) return;

		//final SFile dbDir=dbDir();
		ClassLoader cl=this.getClass().getClassLoader();
		//SFile dbIdFile=dbDir.rel(SDB.PRIMARY_DBID_TXT);
		InputStream in=cl.getResourceAsStream(DB_INIT_PATH+"/"+PRIMARY_DBID_TXT);
		if (in==null) Log.die("Database folder did not set up");
		Scanner s=new Scanner(in);
		String primaryDbid=s.nextLine();
		s.close();

		primaryDBFile().text(primaryDbid);
		//dbIdFile.readFrom(in);
		//String dbId=dbIdFile.text();
		SFile dbDir_in=singleDBHome(primaryDbid); //dbDir.rel(dbid);
		SFile dbFile=dbDir_in.rel(MAIN_DB);
		if (!dbFile.exists()) {
			in=cl.getResourceAsStream(DB_INIT_PATH+"/"+MAIN_DB);
			dbFile.readFrom(in);
		}
	}
	public SFile singleDBHome(String dbid) {
		return multiDBHome().rel(dbid);
	}
	/* (非 Javadoc)
	 * @see jp.tonyu.soytext2.servlet.SystemContext#getDBFile(java.lang.String)
	 */
	public SFile getDBFile(String dbid) {
		return singleDBHome(dbid).rel(MAIN_DB);
	}
	private boolean isEmpty() {
		return !primaryDBFile().exists();
	}
	/* (非 Javadoc)
	 * @see jp.tonyu.soytext2.servlet.SystemContext#closeDB(java.lang.String)
	 */
	@Override
	public void closeDB(String dbid) throws SQLException, IOException, ClassNotFoundException {
		getDB(dbid).close();
		cache.remove(getDBFile(dbid).javaIOFile());

	}
    /* (非 Javadoc)
	 * @see jp.tonyu.soytext2.servlet.SystemContext#setPrimaryDBID(java.lang.String)
	 */
    @Override
	public void setPrimaryDBID(String dbid) throws FileNotFoundException {
        primaryDBFile().text(dbid);
    }
	@Override
	public SFile getConfig(Class<?> klass) {
		return confHome().rel(klass.getCanonicalName());
	}
	private SFile confHome() {
		return home.rel("conf");
	}

}