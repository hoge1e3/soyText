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

import java.sql.SQLException;
import java.util.NoSuchElementException;

import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.JDBCTable;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.debug.Log;

public class SingleIndexIterator implements IndexIterator {
	JDBCRecordCursor<IndexRecord>  cur;
	SDB sdb;
	String key,value;

	boolean hasNexted=false, lastHasNext;
	public SingleIndexIterator(SDB sdb, String key, String value, boolean exactMatch) throws SQLException, NotInReadTransactionException {
		this.sdb=sdb;
		JDBCTable<IndexRecord> t = sdb.table(IndexRecord.class);
        long time=System.currentTimeMillis();
		if (exactMatch) {
            cur = t.scope(IndexRecord.NAME_VALUE_LAST_UPDATE,
                    new Object[]{key,value ,Long.MIN_VALUE},
                    new Object[]{key,value,Long.MAX_VALUE});
		} else {
		    String value2=value+(char)32767;
		    cur = t.scope(IndexRecord.NAME_VALUE_LAST_UPDATE,
		            new Object[]{key,value ,Long.MIN_VALUE},
		            new Object[]{key,value2,Long.MAX_VALUE});
		}
        Log.d(this, "Query time="+(System.currentTimeMillis()-time));
		//cur = t.scope("name,value", new Object[]{key,value},new Object[]{key,value});
        this.key=key;
		this.value=value;
	}
	@Override
	public boolean hasNext() throws SQLException{
	    if (hasNexted) return lastHasNext;
	    hasNexted=true;
		return lastHasNext=cur.next();
	}

	@Override
	public String toString() {
		return "(IndexIterator "+key+"="+value+")";
	}
	@Override
	public IndexRecord next() throws SQLException{
	    if (!hasNexted) {
	        if (!hasNext()) throw new NoSuchElementException();
	    }
        hasNexted=false;
		IndexRecord r=cur.fetch();
		//DocumentRecord d = sdb.byId(r.document);
		return r;
	}
	@Override
	public void close() throws SQLException {
		cur.close();
	}

}