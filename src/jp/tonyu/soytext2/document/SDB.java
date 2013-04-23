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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.JDBCRecord;
import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.JDBCTable;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.PrimaryKeySequence;
import jp.tonyu.db.ReadAction;
import jp.tonyu.db.TransactionMode;
import jp.tonyu.db.WriteAction;
import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.servlet.FileWorkspace;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.util.MD5;
import jp.tonyu.util.SFile;
import jp.tonyu.util.TDate;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

public class SDB implements DocumentSet {
    // public static final String PRIMARY_DBID_TXT = "primaryDbid.txt";
    JDBCHelper helper;
    static final int version=3;
    TransactionMode mode;
    Workspace workspace;
    @Override
    public synchronized void transaction(TransactionMode mode) {
        if (this.mode!=null) Log.die("Already in "+mode);
        this.mode=mode;
        Log.d(this, "Enter trans mode="+mode);
    }
    /*
     * @Override public Object transactionMode() { return mode; }
     */
    @Override
    public void commit() {
        try {
            //Log.d(this, "Commit from1 "+mode);
            synchronized (this) {
                if (mode==null) Log.die("Not in transaction");
                Log.d(this, "Commit from2 "+mode);
                mode=null;
            }
            helper.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.die(e);
        }
    }
    @Override
    public void rollback() {
        try {
            Log.d(this, "Rollback from1 "+mode);
            synchronized (this) {
                if (mode==null) Log.die("Not in transaction");
                Log.d(this, "Rollback from2 "+mode);
                mode=null;
            }
            helper.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.die(e);
        }
    }
    /*
     * public void transaction(Object mode, final Runnable action) { try { if
     * ("read".equalsIgnoreCase(mode+"")) { readTransaction(new DBAction() {
     *
     * @Override public void run(JDBCHelper db) throws SQLException {
     * action.run(); } }, -1); } else if ("write".equalsIgnoreCase(mode+"")) {
     * writeTransaction(new DBAction() {
     *
     * @Override public void run(JDBCHelper db) throws SQLException {
     * action.run(); } }, -1); } else {
     * Log.die("Invalid transaction mode: "+mode); } } catch (SQLException e) {
     * e.printStackTrace(); }
     *
     * }
     */
    // public static final String UID_IMPORT =
    // "77a729a1-5c5d-4d09-9141-72108ee9b634";
    // public static final String UID_EXISTENT_FILE =
    // "86e08ee0-0bd5-4d1f-a7f5-66c2251e60ad";
    public JDBCHelper getHelper() {
        return helper;
    }
    String dbid;
    final SFile dbFile;
    SFile blobDir;
    SFile backupDir;
    SFile homeDir;
    public static Map<SFile, SDB> insts=new HashMap<SFile, SDB>();
    public static int instc=0;
    public SDB(FileWorkspace workspace , String dbid) throws SQLException, ClassNotFoundException {
    	dbFile=workspace.getDBFile(dbid);
    	this.workspace=workspace;
    	Log.d(this, "DB file="+dbFile);
        // looseTransaction=new LooseTransaction(this);
        Class.forName("org.sqlite.JDBC");
        Connection conn=DriverManager.getConnection("jdbc:sqlite:"+dbFile);
        conn.setAutoCommit(false);
        helper=new JDBCHelper(conn, version) {
            @Override
            public Class<? extends JDBCRecord>[] tables(int version) {
                return q(DocumentRecord.class, IndexRecord.class, IdSerialRecord.class);
            }
        };
        conn.setAutoCommit(false);
        // open(file, version);
        //dbFile=file;
        homeDir=dbFile.parent();
        blobDir=homeDir.rel("blob");
        backupDir=homeDir.rel("backup");
        readTransaction(new ReadAction() {
            @Override
            public void run(JDBCHelper jdbcHelper) throws NotInReadTransactionException, SQLException {
                logManager=new LogManager(SDB.this);
                idxSeq=new PrimaryKeySequence(indexTable());
            }
        });
        this.dbid=dbid;//getDBIDFromFile(new SFile(file.getAbsoluteFile()));
        instc++;
        insts.put(dbFile, this);
    }
    Pattern dbidPat=Pattern.compile("\\w*\\.\\w+\\.");
    /*private String getDBIDFromFile(SFile file) {
        try {
            SFile parent=file.parent();
            Matcher m=dbidPat.matcher(parent.name());
            if (m.lookingAt())
                return parent.name();
            parent=parent.parent();
            m=dbidPat.matcher(parent.name());
            if (m.lookingAt())
                return parent.name();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/
    public SFile getBlobDir() {
        return blobDir;
    }
    public File getBlob(String id) {
        return blobDir.rel(id).javaIOFile();
    }
    public SFile getHashBlobDir() {
        return homeDir.rel("hashBlob");
    }
    @Override
    public HashBlob getHashBlob(final String hash) {
        final SFile f=getHashBlobFile(hash);
        return new HashBlob() {
            @Override
            public InputStream getInputStream() throws IOException {
                return f.inputStream();
            }

            @Override
            public String getHash() {
                return hash;
            }

            @Override
            public boolean exists() {
                return f.exists();
            }
            @Override
            public String text() {
                try {
                    return f.text();
                } catch(IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }
    private SFile getHashBlobFile(final String hash) {
        return getHashBlobDir().rel(hash);
    }
    @Override
    public HashBlob writeHashBlob(InputStream i) {
        SFile tmp=getHashBlobDir().rel(Math.random()+"");
        try {
            tmp.readFrom(i);
            String h=MD5.crypt(tmp.inputStream());
            HashBlob res=getHashBlob(h);
            if (!res.exists()) {
                SFile f=getHashBlobFile(h);
                tmp.copyTo(f);
            }
            tmp.delete();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;

    }

    public String toString() {
        return "(SDB dbid="+dbid+" home="+homeDir+")";
    }
    // Map<String,DocumentRecord> cache=new HashMap<String, DocumentRecord>();
    private LogManager logManager;
    private PrimaryKeySequence idxSeq;
    @Override
    public void all(final UpdatingDocumentAction action) throws NotInWriteTransactionException {
        try {
            JDBCRecordCursor<DocumentRecord> cur=allCursor(true);
            while (cur.next()) {
                DocumentRecord d=cur.fetch();
                if (action.run(d))
                    break;
            }
            cur.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void all(final DocumentAction action) throws NotInReadTransactionException {
        try {
            JDBCRecordCursor<DocumentRecord> cur=allCursor(true);
            while (cur.next()) {
                DocumentRecord d=cur.fetch();
                if (action.run(d))
                   break;
            }
            cur.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void all(final UpdatingDocumentAction action, final boolean fromNewest) throws SQLException,
            NotInWriteTransactionException {
        JDBCRecordCursor<DocumentRecord> cur=allCursor(fromNewest);
        while (cur.next()) {
            DocumentRecord d=cur.fetch();
            if (action.run(d))
                break;
        }
        cur.close();
    }
    private JDBCRecordCursor<DocumentRecord> allCursor(final boolean fromNewest) throws SQLException,
            NotInReadTransactionException {
        JDBCRecordCursor<DocumentRecord> cur;
        if (fromNewest)
            cur=docTable().order(DocumentRecord.LASTUPDATE_DESC);
        else
            cur=docTable().order(DocumentRecord.LASTUPDATE);
        return cur;
    }
    /*
     * public void all(final DocumentAction action,final boolean fromNewest) {
     * try { allWithoutTransaction(action, fromNewest); /*readTransaction(new
     * DBAction() {
     *
     * @Override public void run(JDBCHelper db) throws SQLException { } },-1);
     */
    /*
     * } catch (SQLException e) { e.printStackTrace(); } }
     */
    @Override
    public DocumentRecord byId(final String id) throws NotInReadTransactionException {
        try {
            DocumentRecord d;
            d=docTable().find1("id", id);
            return d;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }/*
          * synchronized (cache) { if (!cache.containsKey(id)) { try {
          * readTransaction(new DBAction() {
          *
          * @Override public void run(JDBCHelper db) throws SQLException {
          * DocumentRecord d=find1(documentRecord, null, id); if (d!=null) {
          * cache.put(id, d); }
          *
          * } },-1); } catch (SQLException e) { e.printStackTrace(); } }
          * DocumentRecord res = cache.get(id); //if (res.content==null)
          * Log.die(res.id+" has null content"); return res; }
          */
    }
    /*
     * public void save(DocumentRecord d) { save(d,new
     * HashMap<String,String>()); }
     */
    @Override
    public void save(final DocumentRecord d, final PairSet<String, String> indexValues)
            throws NotInWriteTransactionException {
        try {
            save(d, indexValues, true);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private void save(final DocumentRecord d, final PairSet<String, String> indexValues, boolean realtimeBackup)
            throws SQLException, NotInWriteTransactionException {
    	//LogRecord log=logManager.write("save", d.id);
        d.lastUpdate=logManager.timeStamp(); //log.id;
        d.version=d.lastUpdate+"@"+dbid;
        if (realtimeBackup) {
            try {
                realtimeBackup(d);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*
         * reserveWriteTransaction(new DBAction() {
         *
         * @Override public void run(JDBCHelper db) throws SQLException {
         */
        saveRaw(d);
        updateIndexInTransaction(d, indexValues);
        /*
         * } });
         */
    }
	private void saveRaw(final DocumentRecord d) throws SQLException,
			NotInWriteTransactionException {
		JDBCTable<DocumentRecord> t=docTable();
        DocumentRecord dd=t.find1("id", d.id);
        Log.d("SAVE", d);
        if (dd!=null) {
            t.update(d);
        } else {
            t.insert(d);
        }
        Log.d("SAVE", d+": done");
	}
    @Override
    public void setVersion(DocumentRecord d, String version)
    		throws NotInWriteTransactionException {
    	d.version=version;
    	try {
			saveRaw(d);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    public boolean createdAtThisDB(DocumentRecord d) {
        return dbid.equals(dbidPart(d));
    }
    public static String dbidPart(DocumentRecord d) {
        if (d==null||d.id==null)
            return null;
        String id=d.id;
        return dbIdPart(id);
    }
	public static String dbIdPart(String id) {
		String[] s=id.split("\\.",2); // TODO: @.
        if (s.length<=1)
            return null;
        return s[1];
	}
    public static int serialPart(DocumentRecord d) {
        if (d==null||d.id==null)
            return 0;
        String id=d.id;
        return serialPart(id);
    }
	public static int serialPart(String id) {
		String[] s=id.split("\\.",2); // TODO: @.
        if (s.length<=0)
            return 0;
        try {
            return Integer.parseInt(s[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
	}
    public void restoreFromRealtimeBackup(SFile src, Set<String> updated) throws SQLException, JSONException,
            FileNotFoundException, IOException, NotInWriteTransactionException {
        DocumentRecord d=new DocumentRecord();
        Map<String, Object> m=(Map) JSON.decode(src.inputStream());
        d.copyFrom(m);
        DocumentRecord dst=byId(d.id);
        if (dst==null) {
            dst=newDocument(d.id);
        } else {
            if (dst.lastUpdate==d.lastUpdate)
                return;
        }
        d.copyTo(dst);
        save(dst, new PairSet<String, String>(), false);
        updated.add(dst.id);
    }
    private void realtimeBackup(final DocumentRecord d) throws FileNotFoundException, IOException,
            NoSuchFieldException, IllegalAccessException {
        OutputStream outputStream=realtimeBackupFile(d).outputStream();
        JSON json=new JSON();
        json.setPrettyPrint(true);
        json.format(d.toMap(), outputStream);
        outputStream.close();
    }
    private SFile realtimeBackupFile(DocumentRecord d) {
        return realtimeBackupDir().rel(d.id);
    }
    public int docCount() throws SQLException, NotInReadTransactionException {
        JDBCTable<DocumentRecord> t=docTable();
        // JDBCRecordCursor<DocumentRecord> cur2=t.order();
        return t.rowCount();
    }

    public JDBCTable<DocumentRecord> docTable() throws SQLException {
        return table(DocumentRecord.class);
    }
    public JDBCTable<IndexRecord> indexTable() throws SQLException {
        return table(IndexRecord.class);
    }
    @Override
    public DocumentRecord newDocument() throws NotInWriteTransactionException {
        try {
            DocumentRecord d=new DocumentRecord();
            int id=logManager.newSerial();
            d.id=id+"."+dbid;  // TODO: @.
            d.lastUpdate=id;
            d.lastAccessed=id;
            // cache.put(d.id, d);
            return d;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    @Override
    public DocumentRecord newDocument(String id) throws NotInWriteTransactionException {
        if (byId(id)!=null)
            Log.die(id+" already exists.");
        LogRecord log;
        try {
            //log=logManager.write("create", id);
            DocumentRecord d=new DocumentRecord();
            d.id=id;
            if (createdAtThisDB(d)) {
                logManager.liftUpLastNumber(serialPart(d));
            }
            d.lastAccessed=d.lastUpdate=logManager.timeStamp();  //log.id;
             //log.id;
            // cache.put(d.id, d);
            return d;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    /*public void printLog() throws NotInReadTransactionException {
        logManager.printAll();
    }
    public void importLog(LogRecord curlog) throws SQLException, NotInWriteTransactionException {
        logManager.importLog(curlog);
    }*
     * public void all(LogAction action) { logManager.all(action); }
     *
    @Override
    public int log(String date, String action, String target, String option) throws NotInWriteTransactionException {
        LogRecord l;
        try {
            l=logManager.write(action, target);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return l.id;
    	return 0;
    }*/
    private void addIndexValue(DocumentRecord d, String name, String value) throws SQLException,
            NotInWriteTransactionException {
        // use in writetransaction
        IndexRecord i=new IndexRecord();
        i.id=idxSeq.next();
        i.document=d.id;
        i.name=name;
        i.value=value;
        i.lastUpdate=d.lastUpdate;
        indexTable().insert(i);
    }
    private void removeIndexValues(final DocumentRecord d) throws SQLException, NotInWriteTransactionException {
        // use in writetransaction
        indexTable().delete("document", d.id, d.id);

    }

    @Override
    public void updateIndex(final DocumentRecord d, final PairSet<String, String> indexValues)
            throws NotInWriteTransactionException {
        try {
            /*
             * reserveWriteTransaction(new DBAction() {
             *
             * @Override public void run(JDBCHelper db) throws SQLException {
             */
            updateIndexInTransaction(d, indexValues);
            /*
             * } });
             */
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private void updateIndexInTransaction(final DocumentRecord d, final PairSet<String, String> indexValues)
            throws SQLException, NotInWriteTransactionException {
        Log.d("updateIndex", "updateIndexIntrans "+d+" with "+indexValues);
        removeIndexValues(d);
        for (Pair<String, String> e : indexValues) {
            addIndexValue(d, e.key, e.value);
        }
    }
    /*
     * Boolean _useIndex=null; public boolean useIndex() { if (_useIndex!=null)
     * return _useIndex; return _useIndex=indexTable().exists(); }
     */
    public static final String MIN_STRING="", MAX_STRING=new String(new char[] { 65535, 65535, 65535 });

    public SFile newestBackupFile() {
        SFile src=null;
        Log.d("newestBackup", "Search backup "+backupDir+" *.json");
        for (SFile txt : backupDir) {
            if (!txt.name().endsWith(".json"))
                continue;
            if (src==null||txt.lastModified()>src.lastModified()) {
                src=txt;
            }
        }
        if (src==null)
            Log.die("Backup not found in "+backupDir);
        return src;
    }
    public SFile newBackupFile() {
        String d=new TDate().toString("yyyy_MMdd_hh_mm_ss");
        return backupDir.rel("main.db."+d+".json");
    }
    public Set<String> backupToJSON() throws SQLException, IOException {
        HashSet<String> backupedIDs=new HashSet<String>();
        Map<String, List<Map<String, Object>>> b=backup();
        List<Map<String, Object>> docs=b.get(new DocumentRecord().tableName());
        for (Map<String, Object> doch : docs) {
            backupedIDs.add(doch.get("id")+"");
        }
        JSON json=new JSON();
        json.setPrettyPrint(true);
        OutputStream out=newBackupFile().outputStream();
        json.format(b, out);
        out.close();
        return backupedIDs;
    }
    private static final String ROOTSKEL="root.skel";
    public void restoreFromNewestJSON() throws IOException, SQLException {
        SFile src=newestBackupFile();
        InputStream in=src.inputStream();
        Map b=(Map) JSON.decode(in);
        List<Map<String,Object>> docs=(List)b.get(new DocumentRecord().tableName());
        for (Map<String,Object> doc:docs) {
        	if (doc.get("id").equals(ROOTSKEL)) {
        		String newID="root."+dbid;     // TODO: @.
        		doc.put("id", newID);
        		break;
        	}
        }
        in.close();
        restore(b);
    }
    public void cloneWithFilter(final SDB dest, String[] ids) throws SQLException, NotInWriteTransactionException {
        final Set<String> idss=new HashSet<String>();
        for (String id : ids) {
            DocumentRecord d=byId(id);
            idss.add(id);
            dest.save(d, new PairSet<String, String>());
        }
        /*
         * readTransaction(new DBAction() {
         *
         * @Override public void run(JDBCHelper db) throws SQLException {
         */
        JDBCTable<IndexRecord> t=table(IndexRecord.class);
        JDBCRecordCursor<IndexRecord> cur=t.order();
        while (cur.next()) {
            final IndexRecord indexRecord=cur.fetch();
            if (idss.contains(indexRecord.id)) {
                /*
                 * dest.reserveWriteTransaction(new WriteAction() {
                 *
                 * @Override public void run(JDBCHelper db) throws SQLException
                 * {
                 */
                dest.addIndexValue(byId(indexRecord.document), indexRecord.name, indexRecord.value);
                /*
                 * } });
                 */
            }
        }
        /*
         * } }, -1);
         */
        dest.logManager.setLastNumber(logManager.getLastNumber());
    }
    public SFile getFile() {
        return dbFile;
    }
    @Override
    public String getDBID() {
        return dbid;
    }
    @Override
    public void searchByIndex(Map<String, String> keyValues, boolean exactMatch, IndexAction a) throws NotInReadTransactionException {
        try {
            IndexIterator it=indexIterator(keyValues, exactMatch);
            while (it.hasNext()) {
                IndexRecord d=it.next();
                if (a.run(d)) {
                    break;
                }
            }
            it.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void searchByIndex(final Map<String, String> keyValues, boolean exactMatch, final UpdatingIndexAction a)
            throws NotInWriteTransactionException {
        try {
            IndexIterator it=indexIterator(keyValues, exactMatch);
            while (it.hasNext()) {
                IndexRecord d=it.next();
                if (a.run(d)) {
                    break;
                }
            }
            it.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private IndexIterator indexIterator(final Map<String, String> keyValues, boolean exactMatch) throws NotInReadTransactionException,
            SQLException {
        if (keyValues.size()==1) {
            for (String key : keyValues.keySet()) {
                String value=keyValues.get(key);
                return new SingleIndexIterator(SDB.this, key, value, exactMatch);
            }
        }
        final IntersectIndexIterator it=new IntersectIndexIterator();
        for (String key : keyValues.keySet()) {
            String value=keyValues.get(key);
            it.add(new SingleIndexIterator(SDB.this, key, value, exactMatch));
        }
        return it;
    }

    @Override
    public boolean indexAvailable(String key) {
        return "name".equals(key)||IndexRecord.INDEX_REFERS.equals(key);// ||
                                                                        // DocumentRecord.OWNER.equals(key);
    }
    public SFile realtimeBackupDir() {
        return homeDir.rel("rtBack");
    }
    // ---delegates
    public <T extends JDBCRecord> JDBCTable<T> table(Class<T> class1) throws SQLException {
        return helper.table(class1);
    }
    public void readTransaction(ReadAction dbAction) throws SQLException {
        helper.readTransaction(dbAction);
    }
    public void close() throws SQLException {
        helper.close();
    }
    public Map<String, List<Map<String, Object>>> backup() throws SQLException {
        return helper.backup();
    }
    public int execUpdate(String q) throws SQLException {
        return helper.execUpdate(q);
    }
    public Statement createStatement() throws SQLException {
        return helper.createStatement();
    }
    public boolean equals(Object arg0) {
        return helper.equals(arg0);
    }
    public void exec(String q) throws SQLException {
        helper.exec(q);
    }
    public ResultSet execQuery(String q) throws SQLException, NotInReadTransactionException {
        return helper.execQuery(q);
    }
    public ResultSet execQuery(String q, Object... args) throws SQLException, NotInReadTransactionException {
        return helper.execQuery(q, args);
    }
    public int execUpdate(String q, Object... args) throws SQLException, NotInWriteTransactionException {
        return helper.execUpdate(q, args);
    }
    public int hashCode() {
        return helper.hashCode();
    }
    public void writeTransaction(WriteAction action) throws SQLException {
        helper.writeTransaction(action);
    }
    /*
     * public void reserveWriteTransaction(WriteAction action) throws
     * SQLException { helper.reserveWriteTransaction(action); }
     */
    public <T extends JDBCRecord> JDBCTable<T> table(String name) {
        return helper.table(name);
    }
    public <T extends JDBCRecord> JDBCTable<T> table(T r) {
        return helper.table(r);
    }
    public Class<? extends JDBCRecord>[] tables(int version) {
        return helper.tables(version);
    }
    public void update(JDBCRecord r) throws SQLException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, NotInWriteTransactionException {
        helper.update(r);
    }
    public Map<String, JDBCRecord> tablesAsMap(int version) {
        return helper.tablesAsMap(version);
    }
    public int version() {
        return helper.version();
    }
    public void restore(Map<String, List<Map<String, Object>>> data) throws SQLException {
        helper.restore(data);
    }
	@Override
	public Workspace getSystemContext() {
		return workspace;
	}
	public JDBCTable<IdSerialRecord> idSerialTable() throws SQLException {
		return table(IdSerialRecord.class);
	}
}