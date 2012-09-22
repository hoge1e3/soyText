package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.ReadAction;
import jp.tonyu.db.WriteAction;
import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.util.Context;
import jp.tonyu.util.SFile;

public class JarDownloader {
	public static final Context<String> jarFile=new Context<String>();
	public static void startDownload(HttpContext ctx, String dbid,final SDB src, final String[] ids) throws IOException, SQLException, ClassNotFoundException {
		if (jarFile.get().length()==0) Log.die("jar file not set");
		SFile inputJarFile=new SFile(jarFile.get());
		File outDbF=File.createTempFile("main.tmp",".db");
		final SDB outdb=new SDB(outDbF);
		outdb.writeTransaction(new WriteAction() {
            @Override
            public void run(JDBCHelper jdbcHelper) throws NotInWriteTransactionException, SQLException {
                src.cloneWithFilter(outdb, ids);
            }
        });
		outdb.close();
		//SFile dbFile = new SFile( src.getFile() );
		//String dbid= Math.random()+".tonyu.jp";
		Log.d("DBDL", "dbid ="+dbid+" outDBF="+outDbF);
		//SFile outputJarFile=new SFile( File.createTempFile("main.tmp", ".db") );
		ctx.getRes().setContentType("Application/Octet-Stream");
		OutputStream out=ctx.getRes().getOutputStream();
		JarGenerator g = new JarGenerator(inputJarFile, out, new SFile(outDbF), dbid);
		g.generate();
		out.close();
	}
}
