package jp.tonyu.soytext2.command;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.servlet.FileWorkspace;
import jp.tonyu.util.SFile;

public class AddScopeConstructor {
	public static void main(String[] args) throws Exception {
		FileWorkspace workspace=new FileWorkspace(new SFile("."));
		String dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);

		SDB s=workspace.getDB(dbid);// new SDB(f);
		DocumentLoader d = new DocumentLoader(s);



		s.close();
	}
}
