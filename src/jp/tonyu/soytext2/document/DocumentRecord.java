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


public class DocumentRecord extends JDBCRecord /*implements Wrappable*/ {

	/*	db.createTable("CREATE TABLE "+DOCUMENT_1+"(\n"+
			    "   id TEXT NOT NULL PRIMARY KEY,\n"+
			    "   lastupdate INTEGER NOT NULL,\n"+
			    "   createdate INTEGER NOT NULL, \n"+
			    "   lastaccessed INTEGER NOT NULL,\n"+
			    "   language TEXT,\n"+
			    "   summary TEXT,\n"+
			    "   precontent TEXT,\n"+
			    "   content TEXT,\n"+
			    "   owner TEXT,\n"+
			    "   group TEXT,\n"+
			    "   permission TEXT \n"+
			    ")\n"+
			    "");*/
	@Override
	public String[] columnOrder() {
		return new String[]{"id",LASTUPDATE,"createDate","lastAccessed","language","version",
				"summary",ATTR_CONTENT,OWNER,"group","permission",
				ATTR_CONSTRUCTOR, ATTR_SCOPE
		};
	}
	@Override
	public String tableName() {
		return "DocumentRecord";
	}
	//private DocumentSet documentSet;
	public String id;
	public long lastUpdate,createDate,lastAccessed;
	public String summary,content;
	public String language="javascript";
	public String version="";
	public String owner="",group="",permission="";
	public String constructor="", scope="";
	public static final String OWNER="owner",LASTUPDATE="lastUpdate",LASTUPDATE_DESC="-lastUpdate";
	public static final String ATTR_CONTENT="content"; //"[[110414_052728@"+Origin.uid+"]]";
	public static final String ATTR_SCOPE = "scope";
	public static final String ATTR_CONSTRUCTOR = "constructor";

	/*public DocumentRecord(String id) {
		//this.documentSet=documentSet;
		this.id=id;
	}*/
	@Override
	public String toString() {
		return "(Document "+id+")";
	}
	@Override
	public String[] indexSpecs() {
		return q(LASTUPDATE_DESC,"lastAccessed",OWNER+","+LASTUPDATE_DESC);
	}



	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public long getCreateDate() {
		return createDate;
	}
	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}
	public long getLastAccessed() {
		return lastAccessed;
	}
	public void setLastAccessed(long lastAccessed) {
		this.lastAccessed = lastAccessed;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}


}