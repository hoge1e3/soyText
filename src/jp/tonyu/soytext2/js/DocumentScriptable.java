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

package jp.tonyu.soytext2.js;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.debug.Log;
import jp.tonyu.js.AllPropAction;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.PairSet;
import jp.tonyu.soytext2.file.AttachedBinData;
import jp.tonyu.soytext2.file.BinData;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.SFile;
import jp.tonyu.util.SPrintf;

import net.arnx.jsonic.JSON;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class DocumentScriptable implements Function {
	private static final String UPDATE_INDEX = "updateIndex";
    public static boolean lazyLoad=true;
	boolean contentLoaded=!lazyLoad; // true iff loaded or loading
	private synchronized void loadContent() {
		if (contentLoaded) return;
		contentLoaded=true;
		reloadFromContent();
	}
	public static final String IS_INSTANCE_ON_MEMORY = "isInstanceOnMemory";
	public static final String CALLSUPER="callSuper";

	//private static final Object GETTERKEY = "[[110414_051952@"+Origin.uid+"]]";
	//Scriptable __proto__;
	Map<Object, Object>_binds=new HashMap<Object, Object>();
	DocumentRecord _d;
	final String _id;
	public final DocumentLoader loader;
	public static final String ONAPPLY="onApply",APPLY="apply",CALL="call";
	private static final Object SETCONTENTANDSAVE = "setContentAndSave";
	private static final Object GETCONTENT = "getContent";
    private static final String DOLLAR="$";
	public static final String ONUPDATEINDEX = "onUpdateIndex";
	static int cnt=0;
	Scriptable scope=null;
	boolean scopeLoaded=false;
	public synchronized Scriptable getScope() {
		if (scopeLoaded) return scope;
        scopeLoaded=true;
        String ss=getDocument().scope;
		if (ss==null || ss.length()==0) {
			return null;
		} else {
			try {
				Map<Object, Object> m=(Map)JSON.decode(ss);
				Scriptable res=new BlankScriptableObject();
				for (Map.Entry e:m.entrySet()) {
					ScriptableObject.putProperty(res, e.getKey()+"", loader.byId(e.getValue()+""));
				}
				scope=res;
			} catch (Exception e) {
				e.printStackTrace();
				scope=null;
			}
		}
		return scope;
	}
	public void setScope(Scriptable s) {
		scope=s;
    	binds().remove(DocumentRecord.ATTR_SCOPE);
		scopeLoaded=true;
	}
	public synchronized void setScopeRaw(String r) {
	    getDocument().scope=r;
	    scopeLoaded=false;
	    getScope();
	}

	public DocumentRecord getDocument() {
	    if (_d!=null) return _d;
	    cnt++;
	    //if (cnt>200) Log.die("Get much! "+_id);
        //Log.d(this , "Get! "+_id);
	    _d=loader.recordById(_id);
	    if (_d==null) Log.die("Document "+_id+" is not exist");
	    return _d;
	}
	private Map<Object, Object> binds() {
		loadContent();
		return _binds;
	}
	//static Map<String, DocumentScriptable> debugH=new HashMap<String, DocumentScriptable>();
	public DocumentScriptable(final DocumentLoader loader,String id) {
		this.loader=loader;
		_id=id;
		//this.d=d;

		/*put("id",this , d.id );
		put("lastUpdate",this, d.lastUpdate);
		put("save",this, );*/
	}
	public DocumentScriptable(final DocumentLoader loader, DocumentRecord rec) {
	    this.loader=loader;
	    _d=rec;
	    if (_d.content==null) contentLoaded=true; // When new document
        _id=rec.id;
	}
	BuiltinFunc saveFunc =new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			save();
			return DocumentScriptable.this;
		}
	};
    BuiltinFunc saveRawFunc =new BuiltinFunc() {

        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                Object[] args) {
            saveRaw((Scriptable)args[0]);
            return DocumentScriptable.this;
        }
    };
    BuiltinFunc reloadFromContentFunc =new BuiltinFunc() {

        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                Object[] args) {
            reloadFromContent();
            return DocumentScriptable.this;
        }
    };

	BuiltinFunc updateIndexFunc =new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
		    if (args.length==0) {
	            updateIndex();
		    } else if (args[1] instanceof Scriptable){
		        updateIndex((Scriptable)args[1]);
		    }
			return DocumentScriptable.this;
		}
	};
	BuiltinFunc setContentAndSaveFunc = new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			setContentAndSave(args[0]+"");
			return DocumentScriptable.this;
		}
	};
	BuiltinFunc getContentFunc = new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			return getDocument().content;
		}
	};
	BuiltinFunc hasOwnPropFunc= new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length==0) return false;
			return binds().containsKey(args[0]);
		}
	};
	int callsuperlim=0;
	BuiltinFunc callSuperFunc =new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			loadContent();
			if (args.length>0) {
				int c=0;
				String name=args[0]+"";
				for (Scriptable p=DocumentScriptable.this;p!=null ; p=p.getPrototype()) {
					Object fo=p.get(name, p);
					if (fo instanceof Function) {
						c++;
						if (c==2) {
							Function f = (Function) fo;

							Object[] argShift=new Object[args.length-1];
							for (int i=0 ; i<argShift.length ; i++) {
								argShift[i]=args[i+1];
							}
							Log.d(this, "Calling superclass function "+cx.decompileFunction(f,0));
							return f.call(cx, scope, thisObj, argShift);
						}
					}
				}
			}
			return null;
		}
	};

	public Object get(Object key) {
	    //Log.d(this, "get - "+_id+"."+key);
        if ("id".equals(key)) return _id;
        // Document Funcs (will be move into Document - 66646.2.2011.tonyu.jp)
        if ("save".equals(key)) return saveFunc;
        if (UPDATE_INDEX.equals(key) || ("_"+UPDATE_INDEX).equals(key)) return updateIndexFunc;
        if (SETCONTENTANDSAVE.equals(key)) return setContentAndSaveFunc;
        if (GETCONTENT.equals(key)) return getContentFunc;
        // --- the followings need not be moved
        if (CALLSUPER.equals(key)) return callSuperFunc;
        if ("identityHashCode".equals(key)) return System.identityHashCode(this);
        if ("hasOwnProperty".equals(key)) return hasOwnPropFunc;
        if ("_reloadFromContent".equals(key)) return reloadFromContentFunc;
        if ("_saveRaw".equals(key)) return saveRawFunc;
	    DocumentRecord d=getDocument();
		// deprecated
		if (DocumentRecord.LASTUPDATE.equals(key)) return d.lastUpdate;
		if (DocumentRecord.OWNER.equals(key)) return d.owner;
		if ("summary".equals(key)) return d.summary;
        // end of deprecated. use followings instead.
        if ("_id".equals(key)) return d.id;
        if (("_"+DocumentRecord.LASTUPDATE).equals(key)) return d.lastUpdate;
        if (("_"+DocumentRecord.OWNER).equals(key)) return d.owner;
        if ("_summary".equals(key)) return d.summary;
        if ("_version".equals(key)) return d.version;
        if ("_content".equals(key)) return d.content;
        if (("_"+DocumentRecord.ATTR_CONSTRUCTOR).equals(key) || DocumentRecord.ATTR_CONSTRUCTOR.equals(key)) {
        	return getConstructor();
        }
        if (("_"+DocumentRecord.ATTR_SCOPE).equals(key) || DocumentRecord.ATTR_SCOPE.equals(key)) {
            return getScope();
        }
        if ("_scopeRaw".equals(key)) {
            return getDocument().scope;
        }
        // end of use followings instead.


		Object res = binds().get(key);
		if (res!=null) return res;

		return UniqueTag.NOT_FOUND;
	}

	public Object put(Object key,Object value) {
		if (("_"+DocumentRecord.ATTR_SCOPE).equals(key) || DocumentRecord.ATTR_SCOPE.equals(key)) {
			setScope((Scriptable)value);
			return value;
		}
		if (("_"+DocumentRecord.ATTR_CONSTRUCTOR).equals(key) || DocumentRecord.ATTR_CONSTRUCTOR.equals(key)) {
			setConstructor((Scriptable)value);
			return value;
		}
        if ("_scopeRaw".equals(key)) {
            setScopeRaw(value+"");
            return value;
        }
        if ("_summary".equals(key)) {
            getDocument().summary=value+"";
            return value;
        }
        if ("_content".equals(key)) {
            getDocument().content=value+"";
            //reloadFromContent();  (would be better comment out for import records..)
            return value;
        }


		/*if (key instanceof DocumentScriptable) {
			DocumentScriptable s = (DocumentScriptable) key;
			binds.put(JSSession.idref(s, d.documentSet),value);
		} else*/
		if (key instanceof String || key instanceof Number) {
			binds().put(key, value);
		} else if (value==null){
			binds().remove(key);
		} else {
			Log.die("Cannot put "+key);
		}
		return value;
	}
	public Set<Object> keySet() {
		return binds().keySet();
	}

	@Override
	public void delete(String name) {
		binds().remove(name);
	}

	@Override
	public void delete(int index) {
		binds().remove(index);
	}

	@Override
	public Object get(String name, Scriptable start) {
		return get(name);
	}

	@Override
	public Object get(int index, Scriptable start) {
		return get(index);
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getDefaultValue(Class<?> hint) {
		return toString();
	}

	@Override
	public Object[] getIds() {
		Set<Object> keys=binds().keySet();
		Object[] res=new Object[keys.size()];
		int i=0;
		for (Object key:keys) {

			if (key instanceof String || key instanceof Number) {
				res[i]=key;
			} else {
				Log.die("Wrong key! "+key);
			}
			i++;
		}

		return res;
	}
	void trace(Object msg) {
		if (id().equals("13892.1.2010.tonyu.jp")) {
			Log.d("loadconst", msg);
		}
	}
	@Override
	public Scriptable getParentScope() {
		// TODO Auto-generated method stub
		return null;
	}
	public Scriptable getConstructor() {
	    String c=getDocument().constructor;
	    if (c!=null && c.length()>0) {
	        if ("Function".equals(c)) {
	            return loader.jsSession().funcFactory;
	        } else {
	            return loader.byId(c);
	        }
	    }
	    return null;
	}
	public void setConstructor(Scriptable s) {
		trace("Set const s="+s);
		if (s instanceof DocumentScriptable) {
			DocumentScriptable ds = (DocumentScriptable) s;
			getDocument().constructor=ds._id;
		} else if (s instanceof Function) {
			getDocument().constructor="Function";
		} else {
			Log.die(s+" cannot be a constructor");
		}
    	binds().remove(DocumentRecord.ATTR_CONSTRUCTOR);
	}
	@Override
	public Scriptable getPrototype() {
		Scriptable s=getConstructor();
		trace("Get const s="+s);
		if (s==null) return null;
		Object res=s.get(Scriptables.PROTOTYPE,s);
		if (res instanceof Scriptable) {
			Scriptable ss = (Scriptable) res;
			return ss;
		}
		return null;
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return binds().containsKey(name);
	}

	@Override
	public boolean has(int index, Scriptable start) {
		return binds().containsKey(index);
	}
/* <p>
 * The JavaScript code "lhs instanceof rhs" causes rhs.hasInstance(lhs) to
  * be called.
*/
	@Override
	public boolean hasInstance(Scriptable instance) {
		for (int i=0 ;i<100 ;i++) {
			Object c=ScriptableObject.getProperty(instance, Scriptables.CONSTRUCTOR);
			if (equals(c)) return true;
			if (c instanceof Scriptable) {
				Scriptable cs = (Scriptable) c;
				Object p=ScriptableObject.getProperty(cs, Scriptables.PROTOTYPE);
				if (p instanceof Scriptable) {
					instance = (Scriptable) p;
					continue;
				}
			}
			return false;
		}
		return false;
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		//if (name.equals("contentEquals")) Log.die("Who set it?");
		trace("put "+name +" = "+value);
		put(name,value);
	}

	@Override
	public void put(int index, Scriptable start, Object value) {
		put(index,value);
	}

	@Override
	public void setParentScope(Scriptable parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPrototype(Scriptable prototype) {
		//Log.d(this, "__proto__"+prototype);
		//this.__proto__= prototype;
	}
	public void save() {
		refreshRecordAttrsToRecord();
		refreshContentToRecord();
		Log.d(this, "save() content changed to "+getDocument().content);
		PairSet<String,String> updatingIndex = indexUpdateMap();
		loader.save(getDocument(), updatingIndex);
	}
	public void saveRaw(Scriptable updatingIndex) {
        loader.save(getDocument(), convUpdIdx(updatingIndex));
	}
	private void updateIndex(Scriptable updatingIndex) {
	    PairSet<String, String> updatingIndexp = convUpdIdx(updatingIndex);
	    updateIndex(updatingIndexp);
	}
    private PairSet<String, String> convUpdIdx(Scriptable updatingIndex) {
        // upd=[[k,v],[k,v]...]
	    PairSet<String,String> updatingIndexp = new PairSet<String, String>();
	    Object []idxs=Scriptables.toArray(updatingIndex);
	    for (Object idx:idxs) {
	        Object[] idxa=Scriptables.toArray(idx);
	        if (idxa.length==2) {
	            updatingIndexp.put(idxa[0]+"", idxa[1]+"");
	        }
	    }
        return updatingIndexp;
    }
	private void updateIndex(PairSet<String,String> updatingIndex) {
        loader.updateIndex(getDocument(), updatingIndex);
	}
	private void refreshRecordAttrsToRecord() {
		refreshSummaryToRecord();
		refreshScopeToRecord();
		//refreshConstructor();
	}
	/*private void refreshConstructor() {
		Scriptable con = getConstructor();
		if (con instanceof Scriptable) {
			setConstructor(con);
		}
	}*/
	private void refreshScopeToRecord() {
		final Map r=new HashMap();
		Scriptable s=getScope();
		if (s==null) {
			getDocument().scope=null;
			return;
		}
		Scriptables.each(scope, new StringPropAction() {

			@Override
			public void run(String key, Object value) {
			    if (value instanceof DocumentScriptable) {
                    DocumentScriptable ds=(DocumentScriptable) value;
                    r.put(key, ds.id());
                } else {
                    Log.d(this,"Cannot :"+DocumentScriptable.this+"["+key+"]="+value+";");
                }
			}
		});
		getDocument().scope=JSON.encode(r);
		Log.d(this, "Scope changed to - "+getDocument().scope);
		binds().remove(DocumentRecord.ATTR_SCOPE);
	}
	public void updateIndex() {
		PairSet<String,String> updatingIndex = indexUpdateMap();
		loader.updateIndex(getDocument(), updatingIndex);
	}
	private PairSet<String,String> indexUpdateMap() {
		PairSet<String,String> updatingIndex=new PairSet<String,String>();
		mkIndex(updatingIndex);
		Log.d(UPDATE_INDEX, "save() - index set to "+updatingIndex);
		return updatingIndex;
	}
	private void mkIndex(PairSet<String,String> idx) {
		String name = Scriptables.getAsString(this, "name", null);
		if (name!=null) idx.put("name", name);
		mkClassIndex(idx);
		mkBackLinkIndex(this , idx);
		Object ouio = ScriptableObject.getProperty(this, ONUPDATEINDEX);
		if (ouio instanceof Function) {
			Function oui=(Function)ouio;
			loader.jsSession().call(oui, this, new Object[]{ new IndexUpdateContext(this,idx) } );
		}
	}
	private void mkClassIndex(	PairSet<String, String> idx) {
		int depth=0;
		for (Function klass=Scriptables.getClass(this);
		     klass!=null;
		     klass=Scriptables.getSuperclass(klass)
		) {
			if (klass instanceof DocumentScriptable) {
				DocumentScriptable d = (DocumentScriptable) klass;
				idx.put(IndexRecord.INDEX_INSTANCEOF, d.id());
			} else {
				break;
			}
			if (depth++>16) Log.die("Depth too many");
		}
	}
	public String id() {
		return _id;
	}
	private static void mkBackLinkIndex(final Scriptable s, final PairSet<String,String> idx) {
		if (s instanceof NativeJavaObject) return;
		if (s instanceof DocumentScriptable) {
            DocumentScriptable ds = (DocumentScriptable) s;
            Scriptable scope=ds.getScope();
            if (scope!=null) {
                mkBackLinkIndex(scope, idx);
            }
        }
		Scriptables.each(s, new AllPropAction() {
			@Override
			public void run(Object key, Object value) {
				//Log.d("updateIndex", key+"="+value);
				if (value instanceof DocumentScriptable) {
					Log.d(UPDATE_INDEX, s+"put "+key+"="+value);
					DocumentScriptable d = (DocumentScriptable) value;
					idx.put(IndexRecord.INDEX_REFERS, d.getDocument().id);
				} else 	if (value instanceof Scriptable) {
					Scriptable scr = (Scriptable) value;
					mkBackLinkIndex(scr,idx);
				}
			}
		});
	}
	private void refreshContentToRecord() {
		final StringBuilder b=new StringBuilder();
		b.append(HashLiteralConv.toHashLiteral(this));
		getDocument().content=b+"";
	}
	public void setContentAndSave(String content) {
		DocumentRecord d=getDocument();
	    d.content=content;
		if (d.content==null) Log.die("Content of "+d.id+" is null!");
		String c=d.content;
		if (c.length()>10000) c=c.substring(0,10000);
		Log.d(System.identityHashCode(this), "setContentAndSave() content changed to "+c);
		loader.loadFromContent(content, this);
		refreshRecordAttrsToRecord();
		PairSet<String,String> idx = indexUpdateMap();
		loader.save(d, idx);
		//loader.getDocumentSet().save(d, idx);//d.save();
	}
	public void reloadFromContent() {
	    DocumentRecord d=getDocument();
	    assert d.content!=null;
		if (d.content==null) return; //Log.die("Content of "+d.id+" is null!");
		trace("Reading content - "+d.content);
		loader.loadFromContent(d.content, this);
		refreshRecordAttrsToRecord();
	}
	@Override
	public String toString() {
		return "(Docscr "+id()+")";
	}
	public void clear() {
		binds().clear();
	}
	public void refreshSummaryToRecord() {
        DocumentRecord d=getDocument();
		d.summary=genSummary();
		Log.d(this, "Sumamry changed to "+d.summary);
	}
	public String genSummary() {
		Object res;
		res=get("name");
		String ress = res+"";
		if (res!=null && res!=UniqueTag.NOT_FOUND && ress.length()>0) return ress;
		res=get("title");ress = res+"";
		if (res!=null && res!=UniqueTag.NOT_FOUND && ress.length()>0) return ress;
		res=get(HttpContext.ATTR_BODY);ress = res+"";
		if (res!=null && res!=UniqueTag.NOT_FOUND && ress.length()>0) return ress.substring(0,Math.min(ress.length(), 20));
		return id();
	}
	@Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
            Object[] args) {
	    Object r;
	    r=ScriptableObject.getProperty(this,DOLLAR);
	    if (r instanceof Function) {
            Function f = (Function) r;
	        return f.call(cx, scope, thisObj , args);
	    }

	    r=ScriptableObject.getProperty(this,ONAPPLY);
		if (r instanceof Function) {
			Function f = (Function) r;
			Object[] args2=new Object[] { thisObj ,args };
			return f.call(cx, scope, this, args2);
		}
		r=ScriptableObject.getProperty(this,APPLY);
		if (r instanceof Function) {
			Function f = (Function) r;
			Object[] args2=new Object[] { thisObj ,args };
			return f.call(cx, scope, this, args2);
		}

		r=ScriptableObject.getProperty(this,CALL);
		if (r instanceof Function) {
			Function f = (Function) r;
			Object[] args2=new Object[args.length+1];
			args2[0]=thisObj;
			for (int i=1 ; i<args2.length ;i++){
				args2[i]=args[i-1];
			}
			return f.call(cx, scope, this , args2);
		}
		Log.die(this+" is not function-callable.");
		return null;
	}
	public boolean isInstanceOnMemory() {
		Object r=get(IS_INSTANCE_ON_MEMORY);
		if (r instanceof Boolean) {
			return (Boolean)r;
		}
		return false;
	}
	@Override
	 public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		Scriptable d; //  generate id
		if (isInstanceOnMemory()) {
			d=new BlessedScriptable(this);
			/* new BlankScriptableObject();
			Object prot = get(Scriptables.PROTOTYPE);
			if (prot instanceof Scriptables) {
				d.setPrototype( (Scriptable)prot  );
			}*/
		} else {
			d=loader.newDocument(); //  generate id
		}
		//Scriptable cons = getConstructor();
		ScriptableObject.putProperty(d,Scriptables.CONSTRUCTOR, this); //cons);
		String name=Scriptables.getAsString(this, "name", null);
		if (name!=null) {
			Scriptable scope2=new BlankScriptableObject();
			ScriptableObject.putProperty(scope2, name, this);
			ScriptableObject.putProperty(d, DocumentRecord.ATTR_SCOPE, scope2);
		}
		/*Scriptable p=getPrototype();
		if (p!=null) {*/
			Object init=ScriptableObject.getProperty(d,"initialize");
			Log.d(this, " initialize = "+init);
			if (init instanceof Function) {
				Log.d(this, " initialize called!");
				Function f = (Function) init;
				f.call(cx, scope, d, args);
			} else {
				Log.d(this, " initialize did not called");
				if (init!=null) {
					Log.d(this, "init="+init.getClass().getSuperclass());
				}
			}
		//}
		return d;
	}
	public void refreshIndex() throws NotInWriteTransactionException {
		PairSet<String,String> h = indexUpdateMap();
		loader.getDocumentSet().updateIndex(getDocument(), h);
	}
    public boolean isRecordLoaded() {
        return _d!=null;
    }
    public void loadRecord(DocumentRecord d) {
        Log.d(this , "Loaded ! "+d);
        _d=d;
    }
    public void clearScope() {
        scopeLoaded=false;
    }

}