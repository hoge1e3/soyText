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

import jp.tonyu.db.JDBCRecord;

/**
 * Represents remote system known by this system.
 * @author hoge1e3
 *
 */
public class RemoteRecord extends JDBCRecord {
	/**
	 * Primary key
	 */
	public int id;
	/**
	 * The database id in which the remote system having.
	 * Database id is like "1.2010.tonyu.jp"
	 */
	public String dbid;
	/**
	 * an URL of the remote system.
	 * If null, the system may not IP-reachable.
	 */
	public String url;
	/**
	 * password hash, public key or something, it may be used
	 * to check whether the remote's request is valid. NOT
	 * to obtain access premission to remote.
	 *   (to obatin, access to {@code url} and give some instruction)
	 */
	public String credential;
	/**
	 * The status of the remote system. For example:
	 * <ul>
	 *  <li>"master" - It can generate document of {@code dbid}(document having id ends with ".{@code dbid}" )
	 *  <li>"mirror" - It cannot generate, but keep syncing with master and have full docuemnts of {@code dbid}
	 *  <li>"mirror before Date" - It was kept syncing before the Date
	 *  <li>"partial" - It having only partial documents of {@code dbid}
	 * </ul>
	 */
	public String status;
	/**
	 * the "source" URL from which the information of this record is obtained.
	 * If the URL is suspicious, ths system should confirm the user before take actions.
	 * If null, this system "believes" that the information is true.
	 */
	public String statedBy;
	/**
	 * An id of LogRecord(of remote system) indicating when synced last.
	 */
	public long remoteLastSynced;
	/**
	 * An id of LogRecord(of this system) indicating when synced last.
	 */
	public long localLastSynced;
	@Override
	public String tableName() {
		return "RemoteRecord";
	}
}