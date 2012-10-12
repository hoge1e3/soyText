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

package jp.tonyu.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import jp.tonyu.soytext2.servlet.Html;

public class HttpPost {
	public static String send(String urls, Map<String,String> params ) throws IOException{
		final StringBuilder data=new StringBuilder();
		StringBuilder res=new StringBuilder();
		Maps.entries(params).each(new MapAction<String, String>() {
			@Override
			public void run(String key, String value) {
				data.append(Html.p("%u=%u&", key,value));
			}
		});
		URL url = new URL(urls);
		HttpURLConnection htpcon = (HttpURLConnection)url.openConnection();
		htpcon.setRequestMethod("POST");
		htpcon.setDoOutput(true);
		htpcon.connect();
		OutputStreamWriter out = new OutputStreamWriter( htpcon.getOutputStream() );
		out.write(data+"\n");
		out.flush();
		out.close();
		Scanner s=new Scanner(new InputStreamReader(htpcon.getInputStream() ,"utf-8"));
		while (s.hasNextLine()) {
			res.append(s.nextLine()+"\n");
		}
		s.close();
		return res+"";
	}
	public static void main(String[] args) throws IOException {
		
		String res=send("http://localhost:8080/soytext2/exec/1269@1.2010.tonyu.jp",
				Maps.create("data", "てすと").p("de_ta", "とすて"));
		System.out.println(res);
	}
}