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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import jp.tonyu.db.DBAction;
import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.JDBCTable;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.PrimaryKeySequence;
import jp.tonyu.db.ReadAction;
import jp.tonyu.db.TransactionMode;
import jp.tonyu.db.WriteAction;
import jp.tonyu.debug.Log;
import java.sql.SQLException;

public class LogManager {
	IdSerialRecord lastNumber;
	SDB sdb;
	public LogManager(final SDB sdb) throws SQLException, NotInReadTransactionException {
		super();
		this.sdb=sdb;
		JDBCTable<IdSerialRecord> ids = sdb.idSerialTable();
		JDBCRecordCursor<IdSerialRecord> it = ids.all();
		while (it.next()) {
			lastNumber=it.fetch();
		}
		it.close();
		//lastNumber=new PrimaryKeySequence(sdb.logTable()).current();
	}

	public synchronized void liftUpLastNumber(int n) throws SQLException, NotInWriteTransactionException {
		if (lastNumber==null || n>lastNumber.serial) setLastNumber(n);
	}
	public synchronized void setLastNumber(int n) throws SQLException, NotInWriteTransactionException {
		/*lastNumber=n-1;
		write("setLastNumber","");*/
	    if (lastNumber==null) {
	        lastNumber=new IdSerialRecord();
	        lastNumber.serial=n;
            sdb.idSerialTable().insert(lastNumber);
	    } else {
	        lastNumber.serial=n;
	        sdb.idSerialTable().update(lastNumber);
	    }
	}

	public synchronized int newSerial() throws SQLException, NotInWriteTransactionException {
		setLastNumber(getLastNumber()+1);
		return getLastNumber();
	}

	public int getLastNumber() {
		return lastNumber.serial;
	}
	long lastTime=0;
	public synchronized long timeStamp() {
		long res=new Date().getTime();
		while (lastTime==res) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			res=new Date().getTime();
		}
		lastTime=res;
		return res;
	}
}