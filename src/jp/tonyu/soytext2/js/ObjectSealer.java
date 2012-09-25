package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ObjectSealer {
    public static void sealAll(Object root) {
        HashMap<String, Integer> types=new HashMap<String, Integer>();
        sealAll(root, new HashSet<Object>(), "", types);
        /*for (String n:types.keySet()) {
            System.out.println(n+" - "+types.get(n));
        }*/
    }
    private static void sealAll(Object s, HashSet<Object> visited , String path, Map<String,Integer> types) {
        if (s==null || !visited.add(s)) return ;
        String cln=s.getClass().getCanonicalName();
        //System.out.println(path+":"+cln);
        if (types.get(cln)==null) types.put(cln, 1);
        else types.put(cln, types.get(cln)+1);
        Object[] ids=null;
        if (s instanceof ScriptableObject) {
            ScriptableObject so=(ScriptableObject) s;
            so.sealObject();
            ids=so.getAllIds();
        } else if (s instanceof Scriptable) {
            Scriptable ss=(Scriptable) s;
            ids=ss.getIds();
            types.put(cln, types.get(cln) | 4096);
        } else {
            types.put(cln, types.get(cln) | 8192);
        }
        if (ids!=null) {
            Scriptable ss=(Scriptable)s;
            for (Object n:ids) {
                Object v=null;
                if (n instanceof String) {
                    String sn=(String) n;
                    v=ScriptableObject.getProperty(ss, sn);
                }
                if (n instanceof Integer) {
                    Integer in=(Integer) n;
                    v=ScriptableObject.getProperty(ss, in);
                }
                sealAll(v, visited, path+"."+n, types);
            }
        }
    }
}
