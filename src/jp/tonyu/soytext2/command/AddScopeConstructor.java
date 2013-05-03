package jp.tonyu.soytext2.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import jp.tonyu.db.JDBCCursor;
import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentRecordVer3;
import jp.tonyu.soytext2.document.LooseWriteAction;
import jp.tonyu.soytext2.document.NeedsUpgradeException;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.UpdatingDocumentAction;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.servlet.FileWorkspace;
import jp.tonyu.util.SFile;

public class AddScopeConstructor {
    public static void main(String[] args) throws Exception {
        FileWorkspace workspace=new FileWorkspace(new SFile("."));
        String dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);
        try {
            SDB sold= new SDB( workspace , dbid, SDB.CUR_DB_VERSION);
            sold.close();
        } catch(NeedsUpgradeException e) {
            System.out.println(e.getMessage());
            backupOLD(workspace, dbid, e);
            removeOldBin(workspace, dbid);
            restoreAsNew(workspace, dbid);
            convert(workspace, dbid);
        }
    }

    private static void convert(FileWorkspace workspace, String dbid)
            throws SQLException, IOException, ClassNotFoundException {
        final SDB s2=workspace.getDB(dbid);
        //final Vector<String> ids=new Vector<String>();
        final DocumentLoader dld = new DocumentLoader(s2);
        DocumentLoader.cur.enter(dld, new Runnable() {
            @Override
            public void run() {
                dld.getLTR().write(new LooseWriteAction() {
                    @Override
                    public void run() throws NotInWriteTransactionException {
                        s2.all(new UpdatingDocumentAction() {
                            @Override
                            public boolean run(DocumentRecord d) throws NotInWriteTransactionException {
                                DocumentScriptable ds = dld.byId(d.id);
                                ds.reloadFromContent();
                                DocumentRecord dr = ds.getDocument();
                                try {
                                    s2.docTable().update(dr);
                                } catch (SQLException e) {
                                    // TODO 自動生成された catch ブロック
                                    e.printStackTrace();
                                }
                                //ids.insertElementAt(d.id,0);
                                return false;
                            }
                        });
                    }
                });
            }
        });
        s2.backupToJSON();
        s2.close();
    }

    private static void restoreAsNew(FileWorkspace workspace, String dbid)
            throws SQLException, IOException, ClassNotFoundException {
        SDB s=workspace.getDB(dbid);
        s.restoreFromNewestJSON();
        workspace.closeDB(dbid);
    }

    private static void removeOldBin(FileWorkspace workspace, String dbid) {
        SFile dbf=workspace.getDBFile(dbid);
        boolean succ=dbf.moveTo(dbf.backupFile("backup"));
        System.out.println("Move res "+succ);
    }

    private static void backupOLD(FileWorkspace workspace, String dbid,
            NeedsUpgradeException e) throws SQLException,
            ClassNotFoundException, IOException {
        SDB sold= new SDB( workspace , dbid, e.oldVersion);
        sold.backupToJSON();
        sold.close();
    }
}
