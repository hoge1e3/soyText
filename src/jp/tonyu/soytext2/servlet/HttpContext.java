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

import static jp.tonyu.util.Literal.toLiteral;
import static jp.tonyu.util.SPrintf.sprintf;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Args;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.auth.UserPasswordCredential;
import jp.tonyu.soytext2.browserjs.IndentAdaptor;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.PairSet;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.file.ReadableBinData;
import jp.tonyu.soytext2.file.ZipMaker;
import jp.tonyu.soytext2.js.ContentChecker;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.search.QueryExpressionParser;
import jp.tonyu.soytext2.search.expr.QueryExpression;
import jp.tonyu.util.Literal;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.Ref;
import jp.tonyu.util.Resource;
import jp.tonyu.util.SFile;
import jp.tonyu.util.SPrintf;
import jp.tonyu.util.Util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class HttpContext implements Wrappable {
    public static final String CONTENT_TYPE = "Content-Type";
	private static final String DO_EDIT = "doEdit";
	public static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain; charset=utf-8";
	private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=utf-8";
	private static final String SEL = "sel_";
	public static final jp.tonyu.util.Context<HttpContext> cur=new jp.tonyu.util.Context<HttpContext>();
	public static final String ATTR_BODY = "body";
	public boolean isRoot() {
		return Auth.cur.get().isRootUser();
	}
	public String user() {
		return Auth.cur.get().user();
	}
	public boolean assertRoot() {
		if (isRoot()) return false;
		redirect(romRootPath()+"/auth");
		return true;
	}
	void rebuildIndex() {
		documentLoader.rebuildIndex();
	}
	public final DocumentLoader documentLoader;
	public DocumentSet documentSet() {
		return documentLoader.getDocumentSet();
	}
	public HttpContext( DocumentLoader loader, HttpServletRequest req, HttpServletResponse res) {
		super();
		this.req = req;
		this.res = res;
		this.documentLoader=loader;
	}
	int printc=0; // printc is used in D-rails
	public void print(Object str) throws IOException {
		res.getWriter().print(str);
		printc++;
	}
    public int getPrintCount(){return printc;}

	private HttpServletRequest req;
	public HttpServletRequest getReq() {
		return req;
	}
	public HttpServletResponse getRes() {
		return res;
	}
	private HttpServletResponse res;
	static final String OP_="OP_";
	public static final String AJAXTAG = "AJAXTAG:";
	public static final String ATTR_ARGUMENTORDER="argumentOrder";
	Map<String,String> _params=null;
	static final String ATTR_FORMAT = "_format";
	private static final String DOGET = "doGet";

    public Map<String,String> params() {
		if (_params!=null) return _params;
    	Map<String,Object> m=req.getParameterMap();
		Map<String,String> res=new Hashtable<String, String>();
		for (String k:m.keySet()) {
			Object vo=m.get(k);
			if (vo instanceof String[]) {
				String[] str = (String[]) vo;
				String val=Util.toSingleton(str);
				res.put(k, val);
			}
		}
		_params=res;
		return res;
	}
    String nativePrefix="ROM";
	public String[] args() {
    	String str=req.getPathInfo();
    	str=str.replaceAll("^/"+nativePrefix, "");
        String[] s=str.split("/");
        return s;
    }
	public String[] argsIncludingRom() {
    	String str=req.getPathInfo();
        String[] s=str.split("/");
        return s;
    }
	public String[] execArgs() {
		//0  1    2   3
		// /exec/id/args0/args1
		int start=3;
		String[] src = args();
		String[] res=new String[src.length-start];
		System.arraycopy(src,start,  res,0, res.length);
		return res;

	}
    public String queryString() {
        String query = req.getQueryString();
        return (query==null?"":query);
    }
    public DocumentProcessor documentProcessor(DocumentScriptable d) {return new DocumentProcessor(d, this);}
    public void proc() throws IOException {
    	final Ref<IOException> ee=new Ref<IOException>();
    	cur.enter(this, new Runnable() {
			@Override
			public void run() {
				try {
					Log.d("htpcon","Before proc2 "+req.getPathInfo()+ " user ="+user());
					proc2();
					Log.d("htpcon","After proc2");
				}catch (Exception e) {
					try {
						Log.d("htpcon", "spawned Error - "+e);
						res.setContentType(TEXT_PLAIN_CHARSET_UTF_8);
						Httpd.respondByString(getRes(), "Error - "+e);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		});
    	if (ee.isSet()) throw ee.get();
	}
    private void proc2() throws IOException
    {
		req.setCharacterEncoding("UTF-8");
		res.setContentType(TEXT_HTML_CHARSET_UTF_8);
		if (req.getPathInfo().startsWith("/"+nativePrefix) ) {
			procRom();
		} else {
			topPage();
		}
    }
    public void procRom() throws IOException {
		String[] s=args();
        String cmd=null;
        if (s.length>=2) cmd=s[1];
        Log.d(this,"pathinfo = "+req.getPathInfo());
        Log.d(this,"qstr = "+req.getQueryString());
        DocumentScriptable root=documentLoader.rootDocument();
        if (!isRoot()) {
            Object permitted=(root!=null ?
                    ScriptableObject.getProperty(root, DocumentLoader.PERMITTED_ROM_COMMANDS) : null);
            boolean p=false;
            if (cmd!=null && permitted instanceof Scriptable) {
                Scriptable perm=(Scriptable) permitted;
                p=ScriptableObject.hasProperty(perm, cmd);
            }
            if (!p) {
                auth();
                return;
            }
        }
        if (s.length == 2 && s[1].equalsIgnoreCase("auth")) {
        	auth();
        	return;
        }
        else if (s.length>=2 && (s[1].equalsIgnoreCase("byid") || s[1].equalsIgnoreCase("view")) ) {
            view();
        }
        else if (s.length >= 3 && s[1].equalsIgnoreCase("exec")) {
        	exec();
        }
        else if (s.length == 2 && s[1].equalsIgnoreCase("new")) {
        	newDocument();
        }
        else if (s.length >= 3 && s[1].equalsIgnoreCase("edit")) {
        	edit();
        }
        else if (s.length >= 3 && s[1].equalsIgnoreCase("customedit")) {
        	customEdit();
        }
        else if (s.length >= 3 && s[1].equalsIgnoreCase("editbody")) {
        	editBody();
        }
        else if (s.length==2 && s[1].equalsIgnoreCase("all")) {
        	all();
        }
        else if (s.length==2 && s[1].equalsIgnoreCase("rebuildindex")) {
        	rebuildIndex();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("search")) {
        	search();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("browserjs")) {
        	browserjs();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("errorlog")) {
        	errorLog();
        }
        else if (req.getPathInfo().equals("/")) {
        	if (fullURL().endsWith("/")) {
        		topPage();
        	} else {
        		redirect(fullURL()+"/");
        	}
        }
        else {
            notfound(s[1]);
        }
    }
    private void errorLog() throws IOException {
    	res.setContentType(TEXT_PLAIN_CHARSET_UTF_8);
    	PrintWriter w=res.getWriter();
    	w.println("-------Log Stat---");
    	w.println(Log.reportStat());
        w.println("-------Error logs---");
    	w.println(Log.errorLog.getBuffer());
    	w.close();
	}
	public String browserjsPath(Class klass) {
    	return romRootPath()+"/browserjs/"+klass.getName();
    }
    private void browserjs() throws IOException {
		//0  1          2
		// /browserjs/path.to.Class
    	String[] a = args();
    	if (a.length<3) return ;
    	try {
			Class c = Class.forName(a[2]);
			String src = Resource.text(c, ".js");
			res.setContentType("text/javascript");
			Httpd.respondByString(res,src);
    	} catch (ClassNotFoundException e) {
			notfound("Class "+a[2]+" Not found.");
		}
	}
	static final String LOCAL_LAST_SYNCED= "localLastSynced";
	static final String REMOTE_LAST_SYNCED= "remoteLastSynced";
	private void topPage() throws IOException {
		Log.d("htpcon", "home");
		final Ref<Boolean> execed = Ref.create(false);
		DocumentScriptable root=documentLoader.rootDocument();
		if (root!=null) {
			Object home=root.get("home");
			Log.d("htpcon", "home is "+home);
			if (home instanceof DocumentScriptable) {
				DocumentScriptable homed = (DocumentScriptable) home;
				exec(homed);
				execed.set(true);
			}
			Log.d("htpcon", "execed = "+execed.get());
		} else {
			root=documentLoader.newDocument(documentLoader.rootDocumentId());
			root.save();
		}
		if (!execed.get()){
			all();
		}
	}
	private void view() throws IOException {
        String[] s=args();
		String id = s[2];
		DocumentScriptable d = (DocumentScriptable)documentLoader.byIdOrNull(id);
		if (d != null)
		{
		    documentProcessor(d).proc();
		}
		else
		{
		    notfound(id);
		}
	}
	private void exec()
			throws IOException {
		//Map<String,String> params=params();
        String[] s=args();


		String id=s[2];
		final DocumentScriptable d= documentLoader.byIdOrNull(id);

		if (d!=null) {
			boolean execed = exec(d);
	        if (!execed) {
	        	print(id+" is not executable :"+d);
	        }
		} else {
			 notfound(id);
		}
	}
	private boolean exec(final DocumentScriptable d) {
		return exec(d,d);
	}
	public String[] getParamNames(Function f) {
		return Args.getArgs(f);
	}
	private boolean exec(final DocumentScriptable d, final Scriptable thiz) {
		boolean execed=false;
		Object doGet = ScriptableObject.getProperty(d, DOGET);
		if (doGet instanceof Function ) {
			final Function f=(Function) doGet;
			JSSession.withContext(new ContextRunnable() {

				@Override
				public Object run(Context cx) {
					if (Args.getArgs(f).length>1) {
						f.call(cx, jssession().root, thiz,
							new Object[]{getReq(),getRes(),HttpContext.this});
					} else {
						f.call(cx, jssession().root, thiz,
								new Object[]{HttpContext.this});
					}
					return null;
				}
			});
			execed=true;
		}
		return execed;
	}
	private JSSession jssession() {
		JSSession jsSession = DocumentLoader.curJsSesssion();
		Log.d("htpctx_jsses",jsSession);
		return jsSession;
	}
	private String contentStatus(ContentChecker c) {
		StringBuilder msg=new StringBuilder(c.getMsg()+"<br/>\n");
		return msg+"";
	}
	private String romRootPath() {
		return rootPath()+"/"+nativePrefix;
	}
	private void newDocument() throws IOException {
		String content="$.extend(_,{\n    name:\"New_Document\"\n});";
		String ctr=params().get("constructor");
		if (ctr!=null) {
			content=sprintf(
					"$.extend(_,{\n"+
					"  name:\"New_Document\",\n"+
			        "  constructor: $.byId(%s) \n" +
			        "});"
					, toLiteral(ctr) ); ;
		}
		String msg="";
		if (req.getMethod().equals("POST")) {
			content=params().get(DocumentRecord.ATTR_CONTENT);
			String[] reqs = getRequires();
			ContentChecker c=new ContentChecker(content,addedVars(),reqs);
			String id=params().get("id");
			if (c.check()) {
				DocumentScriptable d;
				if (id!=null && id.indexOf(".")>=0) { // TODO: @.
					d = documentLoader.newDocument(id);
				} else {
					d = documentLoader.newDocument();
				}
				documentProcessor(d).proc();
				return;
			}
			content=c.getChangedContent();
			msg=contentStatus(c);
		}
		Httpd.respondByString(res, Html.p("<html><title>New Document</title>"+
				"<body><form action=%a method=POST>%s"+
				"Requires: <input name=requires><BR>"+
				"Content: <br/>\n"+
				"<textarea id=edit name=%a rows=25 cols=60>%t</textarea><br/>"+
				"ID(optional): <input name=id><br/>"+
				"<input type=submit>"+
				"</form>"+indentAdap()+"</body></html>",
				 "./new", msg,
				 DocumentRecord.ATTR_CONTENT , content)
		);
	}
	private String[] getRequires() {
		String[] reqs=new String[0];
		String reqss=params().get("requires");
		if (reqss!=null && reqss.trim().length()>0) reqs=reqss.split("\\W+");
		return reqs;
	}
	private String indentAdap() {
		return  Html.p("<script src=%a></script><script>attachIndentAdaptor('edit')</script>"
				,browserjsPath(IndentAdaptor.class));
	}
	DocumentScriptable target;
	public Scriptable targetDocument() {
		return target;
	}
	private boolean customEdit() throws IOException {
		String[] s=args();
		//   $soyText/customedit/00000
		//   $soyText/customedit/00000?defaultEditor=id
		String id=s[2];
		target = documentLoader.byIdOrNull(id);
		if (target==null) {
		    notfound(id);
		    return false;
		}
		String defEdit= params().get("defaultEditor");
		boolean execed=false;
		Object doEdit=ScriptableObject.getProperty(target, DO_EDIT);
		if (doEdit instanceof Function ) {
			final Function f=(Function) doEdit;
			JSSession.withContext(new ContextRunnable() {

				@Override
				public Object run(Context cx) {
					if (Args.getArgs(f).length>1) {
						f.call(cx, jssession().root, target,
							new Object[]{getReq(),getRes(),HttpContext.this});
					} else {
						f.call(cx, jssession().root, target,
								new Object[]{HttpContext.this});
					}
					return null;
				}
			});
			execed=true;
		} else if (defEdit!=null){
			redirect(romRootPath()+"/exec/"+defEdit+"?doc="+target);
		} else {
			edit();
		}
		return execed;
	}
	private void edit() throws IOException {
		String[] s=args();
		//   $soyText/edit/00000
		String id=s[2];
		String msg="";
		target = documentLoader.byIdOrNull(id);
		if (target==null) {
		    notfound(id);
		    return;
		}
		String content = target.getDocument().content;
		if (req.getMethod().equals("POST")) {
			content=params().get(DocumentRecord.ATTR_CONTENT);
			String[] reqs = getRequires();
			ContentChecker c=new ContentChecker(content,addedVars(),reqs);
			c.setPreDefined(Scriptables.toStringKeyMap(target.getScope()).keySet());
			if (c.check()) {
				documentProcessor(target).proc();
				return;
			}
			content=c.getChangedContent();
			msg=contentStatus(c);
		}
		Httpd.respondByString(res, menuBar()+Html.p(
				"<form action=%a method=POST>%s"+
				"Requires: <input name=requires><BR>"+
				"Content: <br/>\n"+
				"<textarea id=edit name=%a rows=20 cols=80>%t</textarea><br/>\n"+
				"Owner: <input name=%a value=%a/><br/>\n"+
				"Scope: <textarea id=edit name=%a rows=5 cols=80>%t</textarea><br/>\n"+
				"Constructor: <input name=%a value=%a/><br/>\n"+
				"<input type=submit>"+
				"</form>"+indentAdap()+"</body></html>",
					"./"+id,msg,
					DocumentRecord.ATTR_CONTENT, content,
					DocumentRecord.OWNER, target.getDocument().owner,
					DocumentRecord.ATTR_SCOPE, target.getDocument().scope,
					DocumentRecord.ATTR_CONSTRUCTOR, target.getDocument().constructor
				)
		);

	}

	private Map<String,String> addedVars() {
		final Map<String,String> b=new HashMap<String, String>();
		Maps.entries(params()).each(new MapAction<String, String>() {
			@Override
			public void run(String key, String value) {
				if (key.startsWith(SEL) && value.length()>0) {
					String name=key.substring(SEL.length());
					b.put(name,value);//(SPrintf.sprintf("var %s=%s;\n", name, value));
				}
			}
		});
		return b;
	}
	private void editBody() throws IOException {
		String[] s=args();
		//   $soyText/edit/00000
		String id=s[2];
		DocumentScriptable d = documentLoader.byIdOrNull(id);
		if (d==null) {
		    notfound(id);
		} else if (req.getMethod().equals("POST")) {
			documentProcessor(d).proc();
		} else {

			Httpd.respondByString(res, menuBar()+Html.p(
					"<form action=%a method=\"POST\">"+
					"Body: <br/>\n"+
					"<textarea id=edit name=%a rows=20 cols=80>%t</textarea>"+
					"<input type=submit>"+
					"</form>"+indentAdap()+"</body></html>",
					"./"+id, ATTR_BODY, d.get(ATTR_BODY)+"")
			);
		}
	}
    private void auth() throws IOException {
		String user=params().get("user");
		String pass=params().get("pass");
		//Session s=null;
		String msg="";
		boolean prompt=true;
		if (/*"logout".equals(user) || */
				(user!=null && user.length()>0 && pass!=null && pass.length()>0)) {
			String userr=Auth.cur.get().login(new UserPasswordCredential(user,pass));
			if (userr!=null) {
				prompt=false;
	    		String after=params().get("after");
	    		if (after!=null) {
	    			res.sendRedirect(rootPath()+"/"+after);
	    		} else {
	    			res.sendRedirect(rootPath()+"/");
	    		}
			} else {
				msg="Login incorrect";
			}
    	}
		if (prompt) {
    		if (user==null) user="";
    		String aft="";
    		String after=params().get("after");
		    if (after!=null) aft=Html.p("<input type=hidden name=after value=%a/>",after);
			Httpd.respondByString(res, msg+"<form action=\"./auth\" method=\"POST\">"+
    				"Username： <input name=\"user\" value=\""+user+"\"><br/>"+
    				"Password: <input type=\"password\" name=\"pass\">"+
    				aft+
    				"<br><input type=submit>"+
    				"</form>"
    		);
    	}
	}
    public String fullURL() {
    	return req.getRequestURL()+"";
    }
    public String absoluteRootPath() {
    	String res=fullURL();
    	int length=args().length;
    	//  docBase()/byId/****
   		// http://host/soytext2     args=[""]
   		// http://host/soytext2/     args=["",""]
   		// http://host/soytext2/aaa     args=["","aaa"]
    	// http://host/soytext2/aaa/bbb   args=["","aaa","bbb"]
    	// http://host/soytext2/aaa/bbb/   args=["","aaa","bbb",""]
    	while (length>=2) {
    		res=res.replaceAll("/[^/]*$", "");
   			length--;
   		}
		return res;
    }
    public String encodeURI(String str) throws UnsupportedEncodingException {
    	return URLEncoder.encode(str, "utf-8");
    }
    public String encodeHTML(String str) {
    	return HTMLDecoder.encode(str);
    }
    public void redirect(String url) {
    	try {
    		Log.d("htpcon", "Redirect to "+url);
    		res.sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	private void all() throws IOException {
        //if (assertRoot()) return;
        final StringBuffer buf = new StringBuffer(isAjaxRequest() ? "" : menuBar());
        documentLoader.all(new BuiltinFunc() {
            int c=0;
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                    Object[] args) {
                DocumentScriptable s=(DocumentScriptable)args[0];
                buf.append(linkBar(s,null));
                c++;
                return c>100;
            }
        });
        buf.append("<BR>insts= "+SMain.insts);
        res.setContentType (TEXT_HTML_CHARSET_UTF_8);
        Httpd.respondByString(res, buf.toString());
	}
	public String linkBar(DocumentScriptable ds) {
		return linkBar(ds,null);
	}
	/**
	 *
	 * @param ds
	 * @param sel
	 * @return
	 */
	public String linkBar(DocumentScriptable ds,String sel) {
		DocumentRecord d=ds.getDocument();
		String id=d.id;

		if (isAjaxRequest()) {
			return 	Util.encodeTabJoin(new Object[] {d.lastUpdate , id, d.summary})+"\n";
		} else {
			String selt="";
			if (sel!=null) {
				selt=Html.p("<a href=%a>Sel</a> ",
					  SPrintf.sprintf(
						 "javascript:window.opener.document.getElementById(%s).value=%s;window.close();",
						 Literal.toLiteral(sel),
						 Literal.toLiteral(SPrintf.sprintf("$.byId(%s)",Literal.toLiteral(id)))
					  )
				);
			}
			return Html.p(
				"<!--%t-->"+
				"%s <a href=%a>View</a>  "+
				"<a href=%a>Edit</a> "+
				"<a href=%a>EditBody</a> "+
				"<a href=%a>CustomEdit</a> "+
				"<a href=%a>Exec</a> "+
				"<a href=%a>NewObj</a> "+
				"%t<br/>\n"
				, AJAXTAG+id
				, selt
				, romRootPath()+"/view/"+id
				, romRootPath()+"/edit/"+id
				, romRootPath()+"/editbody/"+id
				, romRootPath()+"/customedit/"+id
				, romRootPath()+"/exec/"+id
				, romRootPath()+"/new?constructor="+id
				, d.summary);
		}
	}
	private boolean isAjaxRequest() {
		return "ajax".equals( params().get("_responseType") );
	}
	private void search() throws IOException
    {
    	Map<String,String> params=params();
    	//args[2]: id of savedsearch
		String cstr=params.get("q");
        Log.d(this,"cstr = "+cstr);
    	if (cstr==null) {
    		Httpd.respondByString(res,"<form action=\"search\" method=POST><input name=q></form>");
    	} else {
    		search(cstr,params.get("sel"));

    	}
    }
	private void search(String cstr, final String sel) throws IOException {
	    //if (assertRoot()) return;
    	final StringBuffer buf = new StringBuffer(isAjaxRequest() ? "" : menuBar());
        documentLoader.searchByQuery(QueryExpressionParser.parse(cstr), new BuiltinFunc() {
        	int c=0;
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				DocumentScriptable s=(DocumentScriptable)args[0];
				buf.append(linkBar(s,sel));
				c++;
				return c>100;
			}
		});
        buf.append("<BR>insts= "+SMain.insts);
    	res.setContentType (TEXT_HTML_CHARSET_UTF_8);
        Httpd.respondByString(res, buf.toString());
	}
	public String rootPath() {
    	int length=argsIncludingRom().length;
		//  docBase()/byId/****
		if (length<=2) {
			// $SOYTEXT/aaa     args=["","aaa"]
			return ".";
		}
		StringBuffer buf=new StringBuffer();
		String cmd="";
		while (length>2) {
			buf.append(cmd+"..");
			cmd="/";
			length--;
		}
		return buf.toString();
	}
    public void notfound( String searchString)
    throws IOException {
    	res.setStatus(404);
    	Httpd.respondByString(res,searchString+" Not found");
    }
    public void frobidden( String searchString)
    throws IOException {
    	res.setStatus(403);
    	Httpd.respondByString(res,searchString);
    }

	String menuBar() {
		String q=params().get("q");
		if (q==null) q="";
		String path=romRootPath();
		StringBuilder buf=new StringBuilder();
        buf.append(Html.p("<html><head><meta http-equiv=%a content=%a></head>"
        		                ,CONTENT_TYPE,TEXT_HTML_CHARSET_UTF_8));
        buf.append("<body>");
        buf.append("User: "+user()+" | ");
        buf.append(Html.p("<a href=%a>Login..</a>  |" , path+"/auth"));
        buf.append(Html.p("<a href=%a>Recents</a>  |" , path+"/all"));
        buf.append(Html.p("<a href=%a>New..</a> | ", path+"/new"));
        buf.append(Html.p("<form action=%s method=POST style=\"display: inline;\">" +
        		"<input name=q value=%s></form>", path+"/search" ,q));
        //buf.append(Html.p("<a href=%a>検索</a> |\n" , path+"/search"));
        buf.append("DB: "+documentSet());
        buf.append("| Loaders: "+DocumentLoader.loaders.size());
        buf.append(Html.p("| Err: <a href=%a>%s</a>",
        		path+"/errorlog", ""+Log.errorLog.getBuffer().length()));
        buf.append("<HR>");
        return buf.toString();
	}

	public static String detectContentType(String fileName) {
		return detectContentType(fileName, TEXT_PLAIN_CHARSET_UTF_8);
	}
	public static String detectContentType(String fileName,String def) {
	    if (fileName != null)
	    {
	    	fileName=fileName.toLowerCase();
	        if (fileName.endsWith(".js"))
	        {
	            def = "text/javascript; charset=utf-8";
	        }
	        if (fileName.endsWith(".css"))
	        {
	            def = "text/css; charset=utf-8";
	        }
	        if (fileName.endsWith(".html"))
	        {
	            def = TEXT_HTML_CHARSET_UTF_8;
	        }
	        if (fileName.endsWith(".gif"))
	        {
	            def = "image/gif";
	        }
	        if (fileName.endsWith(".ico"))
	        {
	            def = "image/vnd.microsoft.icon";
	        }
	        if (fileName.endsWith(".jpg"))
	        {
	            def = "image/jpeg";
	        }
	    }
	    return def;
	}
	public Object getSession(String key) {
		HttpSession s=req.getSession();
		if (s!=null) return s.getAttribute(key);
		return null;
	}
	public void putSession(String key,Object value) {
		HttpSession s=req.getSession();
		if (s!=null) s.setAttribute(key,value);
	}

	public static String detectContentType(DocumentScriptable d)
	{
	    Object c = d.get(CONTENT_TYPE);
	    if (c instanceof String) return c.toString();
	    c = d.get("Content-type");
	    if (c instanceof String) return c.toString();
	    String n = d.get("name")+"";
	    Log.d("HTPCON", "Detecting "+d.getDocument().id+" - "+n);
	    return detectContentType(n);
	}
	public static String ajaxTag(String string) {
		return "<!--"+AJAXTAG+string+"-->";
	}

	public void write(ReadableBinData data) throws IOException {
		write(data.getInputStream());
	}
	public void write(InputStream in) throws IOException {
		SFile.redirect(in, res.getOutputStream());
	}
	public ZipMaker zipMaker() throws IOException {
		ServletOutputStream out = res.getOutputStream();
		ZipMaker z = new ZipMaker(out);
		return z;
	}
}