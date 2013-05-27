package jp.tonyu.soytext2.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.util.SFile;

public interface Workspace {

	public abstract DocumentSet getDB(String dbid) throws SQLException, IOException,
			ClassNotFoundException;

	public abstract DocumentSet getPrimaryDB() throws SQLException, IOException,
			ClassNotFoundException;

	public abstract String getPrimaryDBID() throws IOException;


	public abstract void closeDB(String dbid) throws SQLException, IOException,
			ClassNotFoundException;

	public abstract void setPrimaryDBID(String dbid)
			throws FileNotFoundException;

	public SFile getConfig(Class<?> klass);

    public abstract SFile globalHashBlobdir();
}