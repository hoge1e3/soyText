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

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.util.SFile;



public class DocumentProcessor {
	DocumentScriptable d;
	HttpContext ctx;
	public DocumentProcessor(DocumentScriptable d, HttpContext ctx) {
		super();
		this.d = d;
		this.ctx = ctx;
	}
	public HttpServletRequest req() {return ctx.getReq();}
	public HttpServletResponse res() {return ctx.getRes();}
	public DocumentSet documentSet() {return ctx.documentSet();}
	public static final Pattern docPat=Pattern.compile("\\[\\[([^\\[\\]]+)*\\]\\]");

	public Map<String, String> params() {
		return ctx.params();
	}
	void feedBody() throws IOException {
	    HttpServletResponse res=res();
	    res.setContentType( HttpContext.detectContentType(d) );
	    Object body = d.get(HttpContext.ATTR_BODY);
	    Httpd.respondByString(res, body+"");
	}
	void feedJSON() throws IOException
	{
		HttpServletResponse res=res();
	    String c = "text/plain; charset=utf-8";
	    res.setContentType (c);
	    String meta="id: "+id()+"\n"+"lastupdate: "+d.getDocument().lastUpdate+"\n";
	    Httpd.respondByString(res, meta+d.getDocument().content);
	}
	private String id() {
		return d.getDocument().id;
	}
	void proc() throws IOException
	{
		HttpServletRequest req=req();
		HttpServletResponse res=res();

		String query=queryString();

		if (req.getMethod().equals("GET"))
		{
			if ("json".equals(query)) {
				feedJSON();
			} else {
				feedBody();
			}
		}
		else if (req.getMethod().equals("POST"))
		{
			Map<String,String> keys=params();//  HttpUtility.ParseQueryString(str);
			if (keys.containsKey(DocumentRecord.OWNER)) {
				String o=keys.get(DocumentRecord.OWNER);
				if (o!=null && o.length()>0) {
					d.getDocument().owner=o;
				}
			}
			if (keys.containsKey(DocumentRecord.ATTR_SCOPE)) {
				String o=keys.get(DocumentRecord.ATTR_SCOPE);
				if (o!=null && o.length()>0) {
					d.getDocument().scope=o;
				}
			}
			if (keys.containsKey(DocumentRecord.ATTR_CONSTRUCTOR)) {
				String o=keys.get(DocumentRecord.ATTR_CONSTRUCTOR);
				if (o!=null && o.length()>0) {
					d.getDocument().scope=o;
				}
			}
			if (keys.containsKey(DocumentRecord.ATTR_CONTENT)) {

			    String cont = keys.get(DocumentRecord.ATTR_CONTENT);
			    d.getDocument().content=cont;
			    d.save();
			} else if (keys.containsKey(HttpContext.ATTR_BODY)) {
				d.put(HttpContext.ATTR_BODY, keys.get(HttpContext.ATTR_BODY));
				d.save();
			}
			res.setContentType("text/html; charset=utf8");
			String docBase=rootPath();
			Httpd.respondByString(res, Html.p(HttpContext.ajaxTag("id:"+id())+"\n Edit "+id()+" End <br/>\n"+
					ctx.linkBar(d)+"<a href=%a>Top</a>  ",docBase+"/all"
			        ));
		}
	}
	public String rootPath() {
		return ctx.rootPath();
	}
	public String[] args() {
		return ctx.args();
	}
	public String queryString() {
		return ctx.queryString();
	}

}