package jp.tonyu.soytext2.document;

import java.sql.SQLException;

public class NeedsUpgradeException extends SQLException {
	public int oldVersion,  newVersion;
	public NeedsUpgradeException(int oldVersion, int newVersion) {
		super("Needs upgrade from "+oldVersion+" to "+newVersion);
		this.oldVersion=oldVersion;
		this.newVersion=newVersion;
	}


}
