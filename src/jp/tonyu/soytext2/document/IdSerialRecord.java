package jp.tonyu.soytext2.document;

import jp.tonyu.db.JDBCRecord;

public class IdSerialRecord  extends JDBCRecord {
	public int id=0;
	public int serial;// it is last number. Increment BEFORE create new id
	@Override
	public String tableName() {
		return "IdSerialRecord";
	}

}
