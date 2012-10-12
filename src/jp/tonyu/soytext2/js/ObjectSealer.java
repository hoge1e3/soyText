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