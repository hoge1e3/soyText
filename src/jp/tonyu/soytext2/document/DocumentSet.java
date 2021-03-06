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

import java.io.InputStream;
import java.util.Map;

import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.TransactionMode;
import jp.tonyu.soytext2.servlet.Workspace;

public interface DocumentSet {
	public DocumentRecord newDocument() throws NotInWriteTransactionException;
	public DocumentRecord newDocument(String id) throws NotInWriteTransactionException;
	public void importRecord(DocumentRecord d) throws NotInWriteTransactionException;
	public void save(DocumentRecord d, PairSet<String,String> updatingIndex) throws NotInWriteTransactionException;
	public void setVersion(DocumentRecord d, String version) throws NotInWriteTransactionException;
	public void updateIndex(DocumentRecord d,PairSet<String,String> h) throws NotInWriteTransactionException;
	public DocumentRecord byId(String id) throws NotInReadTransactionException;
	public void transaction(TransactionMode mode);
    //public Object transactionMode();//, Runnable action);
	//public File getBlob(String id);
	public HashBlob getHashBlob(String hash);
	public HashBlob writeHashBlob(InputStream i);
    public void commit();
    public void rollback();
	//public int log( String date, String action, String target, String option)  throws NotInWriteTransactionException;
	public String getDBID();

	//public void searchByIndex(String key, String value, IndexAction a)  throws NotInReadTransactionException;
	public void searchByIndex(Map<String, String> keyValues, boolean exactMatch, IndexAction a)  throws NotInReadTransactionException;
	public void all(DocumentAction a)  throws NotInReadTransactionException;
    public void searchByIndex(Map<String, String> keyValues, boolean exactMatch, UpdatingIndexAction a)  throws NotInWriteTransactionException;
    public void all(UpdatingDocumentAction a)  throws NotInWriteTransactionException;

    public boolean indexAvailable(String key)  throws NotInReadTransactionException;

    public Workspace getSystemContext();
}