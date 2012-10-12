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

package jp.tonyu.nanoservlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import jp.tonyu.debug.Log;
import jp.tonyu.util.SFile;

public class AutoRestart {
	File lockFile;
	String key;
	int port;
	public AutoRestart(int port, File lockFile) {
		this.lockFile=lockFile;
		this.port=port;
		start();
	}
	public void start() {
		if (lockFile.exists()) {
			try {
				key=new SFile(lockFile).text();
				URL u=new URL("http://localhost:"+port+"/"+key);
				InputStream i=(InputStream)u.getContent(new Class[]{InputStream.class});
				i.close();
				while (lockFile.exists()) {
					Log.d(this, "Waiting for shutdown...");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		key=Math.random()+"";
		try {
			new SFile(lockFile).text(key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	public void stop() {
		onStop();
		lockFile.delete();
	}
	protected void onStop() {
		
	}
	public boolean hasToBeStopped(String path) {
		return path.startsWith("/"+key);
	}
	public String stopURL() {
		return "http://localhost:"+port+"/"+key;
	}

}