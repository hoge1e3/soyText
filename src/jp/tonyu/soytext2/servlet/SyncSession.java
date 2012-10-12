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

package jp.tonyu.soytext2.servlet;

import java.util.HashSet;
import java.util.Set;

import jp.tonyu.soytext2.js.DocumentScriptable;

public class SyncSession {
	Set<String> uploadIds=new HashSet<String>();
	Set<String> downloadIds=new HashSet<String>();
	DocumentScriptable profile;
	HttpContext ctx;
	long newRemoteLastSynced;

	public SyncSession(HttpContext ctx, DocumentScriptable profile) {
		super();
		this.ctx=ctx;
		this.profile = profile;
	}
	public void confirm() {
		requestUpdatedIds();
		calcUpdatedLocalIds();
	}
	private void calcUpdatedLocalIds() {
		// TODO 自動生成されたメソッド・スタブ

	}
	public void exec() {

	}

	public void requestUpdatedIds() {
		// updatedIds?since=remoteLastSynced
	}
	public void responseUpdatedIds() {

	}

	public void requestUploadDocuments() {
		// get from session

	}
	public void responseUploadDocuments() {

	}
	public void requestDownloadDocuments() {

	}
	public void responsetDownloadDocuments() {

	}
}