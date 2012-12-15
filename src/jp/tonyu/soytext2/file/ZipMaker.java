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

package jp.tonyu.soytext2.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jp.tonyu.js.Wrappable;
import jp.tonyu.util.SFile;

public class ZipMaker implements Wrappable {
	OutputStream out;
	ZipOutputStream zout;
	public ZipMaker(OutputStream out) {
		super();
		this.out = out;
		zout=new ZipOutputStream(out);
	}
    public void add(String path, InputStream data) throws IOException {
        ZipEntry zipEntry = new ZipEntry(path);
        zout.putNextEntry(zipEntry);
        SFile.redirect(data, zout);
    }
	public void add(String path, ReadableBinData data) throws IOException {
		ZipEntry zipEntry = new ZipEntry(path);
		zout.putNextEntry(zipEntry);
		SFile.redirect(data.getInputStream(), zout);
	}
	public void add(String path, String data) throws IOException {
		ZipEntry zipEntry = new ZipEntry(path);
		zout.putNextEntry(zipEntry);
		byte[] b=data.getBytes("utf-8");
		zout.write(b);
	}

	public void close() throws IOException {
		zout.close();
	}
}