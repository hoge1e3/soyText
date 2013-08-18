package jp.tonyu.soytext2.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.soytext2.command.RebuildIndex;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.util.SFile;

public class Setupper {
    FileWorkspace ws;

    public Setupper(FileWorkspace ws) {
        super();
        this.ws=ws;
    }

    public void doIt(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException, ClassNotFoundException {
        PrintWriter w=res.getWriter();
        String dbid=req.getParameter("dbid");
        res.setContentType("text/html");
        if (dbid==null) {
            w.println( Html.p("<form action=%a>" +
                    "DBID <input name=\"dbid\" />" +
                    "<input type=\"submit\" />" +
                    "</form>" ,"."));
        } else {
            setup(dbid);
            w.println(Html.p("%s ... Setup complete. <a href=%a>Start soyText</a>",dbid,"."));
        }

    }

    private void setup(String dbid) throws IOException, SQLException, ClassNotFoundException {
        // 1) decide DBID
        // already!
        /*
         * 2) create ./soytext/db/primaryDbid.txt whose content is DBID
/*
3) create folder soytext/db/DBID

run http://path.to.soytext/exec/109036@1.2010.tonyu.jp  (http://path.to.soytext/exportTest)

4) Unzip to ./soytext/db/DBID
*/
        ws.setPrimaryDBID(dbid);
        SFile skelDir=ws.singleDBHome("skel");
        SFile newDB=ws.singleDBHome(dbid);
        if (!skelDir.moveTo(newDB)) {
            throw new IOException("Cannot move "+skelDir+" to "+newDB);
        }


/*
5) Run jp.tonyu.soytext2.command.RestoreFromJSON in project soyText
   with working directory ./soytext

6) Run jp.tonyu.soytext2.command.RebuildIndex in project soyText
   with working directory ./soytext
*/
        SDB s=ws.getDB(dbid);
        s.restoreFromNewestJSON();
        DocumentLoader d = new DocumentLoader(s);
		d.rebuildIndex(-1,-1);
		ws.closeDB(dbid); // relase cache !

/*
7) Run jp.tonyu.soytext2.servlet.SMain in project soyText
   with working directory ./
   ALREADY run!!
         */

    }
}
