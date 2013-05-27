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
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.auth.AuthenticatorList;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.HashBlob;
import jp.tonyu.soytext2.document.IndexAction;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.LooseReadAction;
import jp.tonyu.soytext2.document.LooseTransaction;
import jp.tonyu.soytext2.document.LooseWriteAction;
import jp.tonyu.soytext2.document.PairSet;
import jp.tonyu.soytext2.document.UpdatingDocumentAction;
import jp.tonyu.soytext2.file.ReadableBinData;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.expr.AndExpr;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.BackLinkExpr;
import jp.tonyu.soytext2.search.expr.InstanceofExpr;
import jp.tonyu.soytext2.search.expr.QueryExpression;
import jp.tonyu.soytext2.servlet.Auth;
import jp.tonyu.soytext2.servlet.DocumentProcessor;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.util.Ref;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class DocumentLoader implements Wrappable, IDocumentLoader {
    public static final jp.tonyu.util.Context<DocumentLoader> cur=new jp.tonyu.util.Context<DocumentLoader>();
    // private static final Object LOADING = "LOADING";
    //public static final Pattern idpatWiki=DocumentProcessor.idpatWiki;// Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
    private static final String ERROR_CONTENT="err_content";
    private static final String ERROR_MSG="err_message";

    // final LooseTransaction looseTransaction;
    // Map<String, Scriptable>objs=new HashMap<String, Scriptable>();
    private final DocumentSet documentSet;
    private Map<String, DocumentScriptable> objs=new HashMap<String, DocumentScriptable>();
    private final JSSession jsSession;
    public static WeakHashMap<DocumentLoader, Boolean> loaders=new WeakHashMap<DocumentLoader, Boolean>();
    final LooseTransaction ltr;
    public LooseTransaction getLTR() {return ltr;}
    public DocumentLoader(DocumentSet documentSet) {
        super();
        this.documentSet=Log.notNull(documentSet, "documentSet");
        // looseTransaction=new LooseTransaction(documentSet);
        ltr=new LooseTransaction(documentSet);
        this.jsSession=new JSSession();
        loaders.put(this, true);
    }
    public Workspace getWorkspace() {
    	return documentSet.getSystemContext();
    }
    public void notifySave(DocumentRecord d) {
        for (DocumentLoader dl : loaders.keySet()) {
            if (dl!=this) {
                dl.onSaveNotified(d);
            }
        }
    }
    public void onSaveNotified(DocumentRecord d) {
        DocumentScriptable s=objs.get(d.id);
        if (s!=null) {
            s.reloadFromContent();
        }
    }
    /*
     * $.byId(id) returns some DocumentScriptable even if it is not exist.
         it is for lazy loading of DocumentRecord to avoid much queries.
     */
    public DocumentScriptable byId(final String id) {
        DocumentScriptable o=objs.get(id);
        if (o!=null)
            return o;
        return defaultDocumentScriptable(id);
    }
    public DocumentScriptable byIdOrNull(final String id) {
        DocumentScriptable o=objs.get(id);
        if (o!=null)
            return o;
        DocumentRecord r=recordById(id);
        if (r==null) return null;
        return defaultDocumentScriptable(r);
    }
    public DocumentRecord recordById(final String id) {
        final Ref<DocumentRecord> src=Ref.create(null);
        ltr.read(new LooseReadAction() {
            @Override
            public void run() throws NotInReadTransactionException {
                src.set(Log.notNull(getDocumentSet(), "gds").byId(id));
            }
        });
        return src.get();
    }
    private DocumentScriptable byRecordOrCache(final DocumentRecord src) {
        DocumentScriptable o=objs.get(src.id);
        if (o!=null) {
            if (!o.isRecordLoaded()) {// It is needed for fullTextGrep
                o.loadRecord(src);
            }
            return o;
        }
        return byRecord(src);
    }
    private DocumentScriptable byRecord(final DocumentRecord src) {
        DocumentScriptable o;
        // if (src.preContent==null || src.preContent.trim().length()==0) {
        o=defaultDocumentScriptable(src);

        // objs.put(id, o); moved to defDocscr
        if (src.content!=null) {
            if (DocumentScriptable.lazyLoad==false) {
                ind.append(" ");
                Log.d(this, "DLoader.loadFromContent"+ ind+"["+src.id+"]");// "+src.content);
                loadFromContent(src.content, o);
                ind.delete(0, 1);
            }
        } else {
            Log.d(this, src.id+".content is still null");
            /*
             * Why allowed this? at 1206@1.2010.tonyu.jp com=new Comment();
             * wrt(com); sub.com=com; sub.save(); // At this time, com.content
             * == null. // And notify content of sub to other sessions // In
             * other sessions, com has not loaded(because it is new) // Thus,
             * com will loaded while saving sub with null content com.save(); //
             * at this time, com.content is properly set. No problem.
             */
        }
        return o;
    }

    public void save(final DocumentRecord d, final PairSet<String, String> updatingIndex) {
        if (d.content==null)
            Log.die("Content of "+d.id+" is null!");
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                getDocumentSet().save(d, updatingIndex);// d.save();
            }
        });
        notifySave(d);
    }
    public void setVesion(final DocumentRecord d, final String ver) {
    	ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                getDocumentSet().setVersion(d, ver);
            }
        });
        notifySave(d);
    }
    public void updateIndex(final DocumentRecord d, final PairSet<String, String> updatingIndex) {
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                getDocumentSet().updateIndex(d, updatingIndex);
            }
        });
    }

    // Map<String, DocumentScriptable> debugH=new HashMap<String,
    // DocumentScriptable>();
    private DocumentScriptable defaultDocumentScriptable(final DocumentRecord src) {
        DocumentScriptable res=new DocumentScriptable(this, src);
        if (objs.containsKey(src.id))
            Log.die("Already have "+src);
        objs.put(src.id, res);
        return res;
    }
    private DocumentScriptable defaultDocumentScriptable(final String id) {
        DocumentScriptable res=new DocumentScriptable(this, id);
        if (objs.containsKey(id))
            Log.die("Already have "+id);
        objs.put(id, res);
        return res;
    }
    static StringBuffer ind=new StringBuffer();
    public void loadFromContent(final String newContent, DocumentScriptable dst) {
        if (newContent==null)
            Log.die("New content is null!");
        dst.clear();
        try {
            Scriptable s=dst.getScope();
            BlankScriptableObject sc=new BlankScriptableObject();
            sc.setParentScope(jsSession().root);
            if (s!=null) Scriptables.extend(sc, s);
            ScriptableObject.putProperty(sc, "$", this);
            ScriptableObject.putProperty(sc, "_", dst);
            ScriptableObject.putProperty(sc, "db", new DBHelper(this) );
            dst.trace("ld1 "+newContent);
            jsSession().eval(dst+"", newContent, sc);
            /*	DocumentLoaderScriptable loaderScope=new DocumentLoaderScriptable(jsSession().root, this, dst);
            	jsSession().eval(dst+"", newContent, loaderScope);
            	dst.trace("ld2 "+newContent);
            	dst.put(HttpContext.ATTR_SCOPE, loaderScope.scope());
            */
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(e);
            Log.d(this, dst.id()+" has invalid content "+newContent);
            dst.put(ERROR_MSG, e.getMessage());
            dst.put(ERROR_CONTENT, newContent);
        }
    }
    public HashBlob hashBlob(String hash) {
        //ReadableBinData b=documentSet.getHashBlob(hash);
        return documentSet.getHashBlob(hash);
    }
    public HashBlob writeHashBlob(ReadableBinData i) throws IOException {
        return writeHashBlob(i.getInputStream());
    }
    public HashBlob writeHashBlob(InputStream i) {
        HashBlob r=documentSet.writeHashBlob(i);
        return r;
    }
    public static JSSession curJsSesssion() {
        return cur.get().jsSession();
    }
    public JSSession jsSession() {
        return jsSession; // JSSession.cur.get();
    }

    public DocumentScriptable newDocument(final String id) {
        final Ref<DocumentScriptable> res=Ref.create(null);
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                DocumentRecord d=getDocumentSet().newDocument(id);
                d.owner=Auth.cur.get().user();
                res.set(defaultDocumentScriptable(d));
            }
        });
        return res.get();
    }
    public DocumentScriptable newDocument() {
        final Ref<DocumentScriptable> res=Ref.create(null);
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                DocumentRecord d=getDocumentSet().newDocument();
                d.owner=Auth.cur.get().user();
                res.set(defaultDocumentScriptable(d));
            }
        });
        return res.get();
    }
    public DocumentScriptable newDocument(Scriptable hash) {
        final Object id=hash!=null ? hash.get("id", hash) : null;
        final Ref<DocumentRecord> d=Ref.create(null);
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                if (id instanceof String) {
                    d.set(getDocumentSet().newDocument((String) id));
                } else {
                    d.set(getDocumentSet().newDocument());
                }
            }
        });
        final DocumentScriptable res=defaultDocumentScriptable(d.get());
        extend(res, hash);
        return res;
    }
    /*
     * private Map<String, String> extractIndexExpr(QueryExpression e) throws
     * NotInReadTransactionException { final Map<String, String> idxs=new
     * HashMap<String, String>(); extractIndexExpr(idxs, e); return idxs; }
     */
    private void extractIndexExpr(Map<String, String> idxs, QueryExpression e) throws NotInReadTransactionException {
        if (e instanceof AndExpr) {
            AndExpr a=(AndExpr) e;
            for (QueryExpression ea : a) {
                extractIndexExpr(idxs, ea);
                if (!idxs.isEmpty())
                    break; // get only first: the result of
                           // first cond must be fewer than
                           // following
                // QueryExpression res=extractIndexExpr(idxs,ea);
                // if (res!=null) return res;
            }
        } else if (e instanceof AttrExpr) {
            AttrExpr aidx=(AttrExpr) e;
            String key=aidx.getKey();
            Object value=aidx.getValue();
            if (getDocumentSet().indexAvailable(key)) {
                if (value instanceof DocumentScriptable) {
                    DocumentScriptable ds=(DocumentScriptable) value;
                    value=ds.getDocument().id;
                }
                idxs.put(key, value.toString());
            } else {
                if (value instanceof DocumentScriptable) {
                    DocumentScriptable ds=(DocumentScriptable) value;
                    value=ds.getDocument().id;
                    idxs.put(IndexRecord.INDEX_REFERS, value.toString());
                }
            }
        } else if (e instanceof BackLinkExpr) {
            BackLinkExpr bidx=(BackLinkExpr) e;
            idxs.put(IndexRecord.INDEX_REFERS, bidx.toId);
            // return b;
        } else if (e instanceof InstanceofExpr) {
            InstanceofExpr iidx=(InstanceofExpr) e;
            idxs.put(IndexRecord.INDEX_INSTANCEOF, iidx.klass);
        }
        // return null;
    }
    private Map<String, String> extractIdxMap(final QueryExpression e) {
        Log.d(this, "Search by "+e);
        final Map<String, String> idxs=new HashMap<String, String>();
        ltr.read(new LooseReadAction() {
            public void run() throws NotInReadTransactionException {
                extractIndexExpr(idxs, e);
            }
        });
        return idxs;
    }
    public void searchByQuery(final QueryExpression q, final Function iter) {
        final Map<String, String> idxs=extractIdxMap(q);
        if (idxs.size()==0) {
            ltr.read(new LooseReadAction() {
                @Override
                public void run() throws NotInReadTransactionException {
                    Log.d(this, "Search add");
                    DocumentAction docAct=new DocumentAction() {
                        @Override
                        public boolean run(DocumentRecord d) throws NotInReadTransactionException {
                            DocumentScriptable s=byRecordOrCache(d);
                            return callDocIter(s , q , iter);
                        }
                    };
                    getDocumentSet().all(docAct);
                }
            });
        } else {
            ltr.read(new LooseReadAction() {
                @Override
                public void run() throws NotInReadTransactionException {
                    Log.d(this, "Search with index "+idxs);
                    IndexAction docAct=new IndexAction() {
                        @Override
                        public boolean run(IndexRecord i) {
                            Log.d("Index Matched", i.document);
                            DocumentScriptable s=(DocumentScriptable) byIdOrNull(i.document);
                            return callDocIter(s , q , iter);
                        }
                    };
                    getDocumentSet().searchByIndex(idxs, false, docAct);
                }
            });
        }
    }
    private boolean callDocIter(DocumentScriptable s, final QueryExpression q, final Function iter) {
        QueryResult r=q.matches(s);
        if (r.filterMatched) {
            Object brk=null;
            brk=jsSession().call(iter, iter, new Object[] { s });
            if (brk instanceof Boolean) {
                Boolean b=(Boolean) brk;
                if (b.booleanValue())
                    return true;
            }
        }
        return false;
    }
    public void updatingSearchByQuery(final QueryExpression q, final Function iter) {
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                searchByQuery(q, iter);
            }
        });
    }
    /*
     * (non-Javadoc)
     *
     * @see jp.tonyu.soytext2.js.IDocumentLoader#extend(jp.tonyu.soytext2.js.
     * DocumentScriptable, org.mozilla.javascript.Scriptable)
     */
    public void extend(final DocumentScriptable dst, Scriptable src) {
        if (src==null)
            return;
        dst.trace("extend src="+src);
        Scriptables.each(src, new StringPropAction() {
            @Override
            public void run(String key, Object value) {
                dst.trace("extend key="+key+" val="+value);
                    dst.put(key, value);
            }
        });
    }
    @Override
    public Wrappable javaNative(String className) {
        try {
            return (Wrappable) Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public DocumentSet getDocumentSet() {
        return documentSet;
    }
    @Override
    public Scriptable inherit(Function superclass, Scriptable overrideMethods) {
        BlessedScriptable res=new BlessedScriptable(superclass);
        Scriptables.extend(res, overrideMethods);
        return res;
    }
    @Override
    public Scriptable bless(Function klass, Scriptable fields) {
        return inherit(klass, fields);
    }
    public DocumentScriptable rootDocument() {
        DocumentScriptable res=byIdOrNull(rootDocumentId());

        return res;
    }
    public String rootDocumentId() {
        return "root."+documentSet.getDBID();  // TODO: @.
    }
    private AuthenticatorList authList;
    public AuthenticatorList authenticator() {
        if (authList!=null)
            return authList;
        authList=new AuthenticatorList();
        DocumentScriptable r=rootDocument();
        Object a=(r!=null ? r.get("authenticator") : null);
        if (a instanceof Function) {
            Function f=(Function) a;
            Log.d(this, "Using - "+f+" as authlist");
            jsSession.call(f, new Object[] { authList });
        }
        return authList;
    }
    /*private void copyDocumentExceptDates(DocumentRecord src, DocumentRecord dst) throws SQLException {
        long lu=dst.lastUpdate;
        src.copyTo(dst);
        dst.lastUpdate=lu;
    }*/
    /**
     *
     * @param drs
     *            DocumentRecords to be imported
     * @throws SQLException
     * @throws NotInWriteTransactionException
     */
    public void importDocuments(Collection<DocumentRecord> drs) throws SQLException, NotInWriteTransactionException {
        Set<DocumentScriptable> willReload=new HashSet<DocumentScriptable>();
        Set<String> willUpdateIndex=new HashSet<String>();
        for (DocumentRecord dr : drs) {
            DocumentRecord existentDr;
            if (objs.containsKey(dr.id)) {
                DocumentScriptable ds=objs.get(dr.id);
                willReload.add(ds);
                existentDr=ds.getDocument();
            } else {
                existentDr=documentSet.byId(dr.id);
            }
            if (existentDr!=null) {
                dr.copyTo(existentDr);
                //copyDocumentExceptDates(dr, existentDr);
                Log.d(this, "Imported Existent: "+existentDr.content);
                documentSet.importRecord(existentDr);
            } else {
                DocumentRecord newDr=documentSet.newDocument(dr.id);
                dr.copyTo(newDr);
                //copyDocumentExceptDates(dr, newDr);
                Log.d(this, "Imported New: "+newDr.content);
                documentSet.importRecord(newDr);
            }
            willUpdateIndex.add(dr.id);
        }
        for (DocumentScriptable ds : willReload) {
            Log.d(this, "Reloading ds: "+ds.getDocument().content);
            ds.reloadFromContent();
        }
        for (String id : willUpdateIndex) {
            DocumentScriptable s=byIdOrNull(id);
            s.refreshIndex();
        }
    }
    public void rebuildIndex() {
        JSSession.withContext(new ContextRunnable() {
            @Override
            public Object run(Context cx) {
                ltr.write(new LooseWriteAction() {
                    @Override
                    public void run() throws NotInWriteTransactionException {
                        documentSet.all(new UpdatingDocumentAction() {
                            @Override
                            public boolean run(DocumentRecord d) throws NotInWriteTransactionException {
                                Log.d("rebuildIndex", d.id);// +" lastUpdate="+d.lastUpdate);
                                DocumentScriptable s=byRecordOrCache(d);
                                s.refreshIndex();
                                return false;
                            }
                        });
                    }
                });

                return null;
            }
        });
    }
    public void all(Function builtinFunc) {
        searchByQuery(new AndExpr(), builtinFunc);
    }
}
