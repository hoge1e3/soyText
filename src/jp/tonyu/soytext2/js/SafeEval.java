package jp.tonyu.soytext2.js;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.Scriptables;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


public class SafeEval extends BuiltinFunc {
    Scriptable root;
    public SafeEval(Scriptable root){this.root=root;}

   /* public Object call2(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        Context c=cx; //Context.getCurrentContext();
        //System.out.println(scope.getPrototype());
        //System.out.println(scope.getParentScope());
        final Scriptable scope2=(Scriptable)args[1];
        ScriptableObject p=new ScriptableObject() {
            @Override
            public String getClassName() {
                // TODO 自動生成されたメソッド・スタブ
                return null;
            }
            public void put(String name, Scriptable start, Object value) {
                System.out.println("WrtP "+name+" = "+value+" start="+start);
                //scope2.put(name, start, value);  // Stackoverflow??
                ScriptableObject.putProperty(scope2, name, value);
            }
        };

        ScriptableObject s=new ScriptableObject() {
            @Override
            public String getClassName() {
                // TODO 自動生成されたメソッド・スタブ
                return null;
            }
            public void put(String name, Scriptable start, Object value) {
                System.out.println("Wrt "+name+" = "+value+" start="+start);
                //scope2.put(name, start, value);  // Stackoverflow??
                ScriptableObject.putProperty(scope2, name, value);
            }

        };
        s.setParentScope(p);
        s.setPrototype(root);
        return c.evaluateString( s, args[0].toString() ,"a", 1,null);
    }*/

    @SuppressWarnings("serial")
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        String source=null, sourceName="safeEval";
        final Scriptable sscope;
        if (args.length>=1&&args[0]!=null) {
            source=args[0].toString();
        }
        if (args.length>=2&&args[1] instanceof Scriptable) {
            sscope=(Scriptable) args[1];
        } else {
            sscope=null;
        }
        if (args.length>=3&&args[2]!=null) {
            sourceName=args[2].toString();
        }
        ScriptableObject p=new BlankScriptableObject() {
            @Override
            public void put(String name, Scriptable start, Object value) {
                super.put(name, start, value);
                ScriptableObject.putProperty(sscope, name, value);
            }
        };
        ScriptableObject s=new BlankScriptableObject() {
            @Override
            public void put(String name, Scriptable start, Object value) {
                Log.d("SafeEval::put", name+"="+value);
                ScriptableObject.putProperty(sscope, name, value);
                super.put(name, start , value);
            }
        };
        if (sscope!=null&&source!=null) {
            Scriptables.extend(s, sscope);
            //s.setParentScope(p);
            s.setPrototype(root);
            Log.d(this , ScriptableObject.getProperty(s, "Object")  );
            Object res=cx.evaluateString(s, source, sourceName, 1, null);
            return res;
        }
        return null;
    }
}