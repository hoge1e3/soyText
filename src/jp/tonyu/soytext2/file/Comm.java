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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Comm implements Wrappable {
    public static final String RT_TEXT="text", RT_BIN="binary";
    public Scriptable exec(String rootPath, String relPath, Scriptable sparams) throws ClientProtocolException, IOException {
        return exec(rootPath, relPath, sparams, RT_TEXT);
    }
    public Scriptable exec(String rootPath, String relPath, Scriptable sparams, Object responseType ) throws ClientProtocolException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(rootPath+relPath);
        final List <NameValuePair> params = new Vector <NameValuePair>();
        Scriptables.each(sparams, new StringPropAction() {

            @Override
            public void run(String key, Object value) {
                if (value instanceof String) {
                    String s=(String) value;
                    params.add(new BasicNameValuePair(key, s));
                }
            }
        });
        httppost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

        HttpResponse response=httpclient.execute(httppost);
        return response2Scriptable(response,responseType);
    }
    public Scriptable execMultiPart(String rootPath, String relPath, final Scriptable sparams ) throws ClientProtocolException, IOException {
        return execMultiPart(rootPath, relPath, sparams, RT_TEXT);
    }
    public Scriptable execMultiPart(String rootPath, String relPath, final Scriptable sparams,Object responseType ) throws ClientProtocolException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(rootPath+relPath);
        final MultipartEntity reqEntity = new MultipartEntity();
        Scriptables.each(sparams, new StringPropAction() {

            @Override
            public void run(String key, Object value) {
                if (value instanceof String) {
                    String s=(String) value;
                    try {
                        reqEntity.addPart(key , new StringBody(s, Charset.forName("UTF-8")));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if (value instanceof Scriptable) {
                    Scriptable s=(Scriptable) value;
                    String filename= (String)ScriptableObject.getProperty(s, FileUpload.FILENAME);
                    Object body = ScriptableObject.getProperty(s, FileUpload.BODY);
                    InputStream st=null;
                    if (body instanceof InputStream) {
                        st=(InputStream) body;
                    } else if (body instanceof ReadableBinData) {
                        ReadableBinData rbd=(ReadableBinData) body;
                        try {
                            st=rbd.getInputStream();
                        } catch (IOException e) {
                            Log.die(e);
                        }
                    }
                    if (st==null) throw new RuntimeException("Not a bindata - "+body);
                    InputStreamBody bin = new InputStreamBody(st, filename);
                    reqEntity.addPart(key, bin);
                }
            }
        });
        httppost.setEntity(reqEntity);

        Log.d(this,"executing request " + httppost.getRequestLine());
        HttpResponse response = httpclient.execute(httppost);
        Scriptable res=response2Scriptable(response, responseType);
        return res;
    }
    private Scriptable response2Scriptable(HttpResponse response, Object responseType)
            throws IOException {
        HttpEntity resEntity = response.getEntity();
        Scriptable res=new BlankScriptableObject();
        if (RT_TEXT.equals(responseType)) {
            String resStr=EntityUtils.toString(resEntity);
            Scriptables.put(res, "content", resStr);
            Log.d(this,resStr);
        } else {
            ByteArrayBinData data=new ByteArrayBinData(EntityUtils.toByteArray(resEntity));
            Scriptables.put(res, "content", data);
        }
        Scriptables.put(res, "statusLine", response.getStatusLine());
        Log.d(this,response.getStatusLine());
        if (resEntity != null) {
            Log.d(this,"Response content length: " + resEntity.getContentLength());
        }
        EntityUtils.consume(resEntity);
        return res;
    }
}