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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.file.BinData;
import jp.tonyu.soytext2.file.ReadableBinData;
import jp.tonyu.soytext2.file.WrappedInputStream;

public interface HashBlob extends ReadableBinData, Wrappable {
    /*DocumentSet s;
    String hash;

    public HashBlob(DocumentSet s, String hash) {
        super();
        this.s=s;
        this.hash=hash;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return new  WrappedInputStream( s.getHashBlob(hash).getInputStream() );
    }
    public String getHash() {
        return hash;
    }*/
    public String getHash();
    public boolean exists();
    public String text();
    /*
     public String text() {  StringgetInputStream().
     */

}