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

package jp.tonyu.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import jp.tonyu.util.TDate;



public class Log {
	static LogWindow lw;
	public static void showLogWindow(Runnable onClose) {
		if (lw==null) {
			lw=new LogWindow(onClose);
		}
		lw.setVisible(true);
	}
	static boolean delay=true;
	static class Stat {
	    int count=0,length=0;
	}
	static HashMap<String,Stat>stat=new HashMap<String, Stat>();
	static StringBuilder buf=new StringBuilder();
	static HashSet<String> whiteList=new HashSet<String>();
	static HashSet<String> blackList=new HashSet<String>();
	static boolean useWhiteList=false;
	static {
		String[] whiteLista=new String[] {};//"query","query-prep","JDBC","jp.tonyu.soytext2.document.SDB"};
		String[] blackLista=new String[] {};
		for (String s:whiteLista) {
			whiteList.add(s);
		}
		for (String s:blackLista) {
			blackList.add(s);
		}
		if (delay) runThread();
	}
	public static void runThread() {
	    Thread t= new Thread() {
	        public void run() {
	            while(true) {
	                String r;
	                synchronized (buf) {
                        while (buf.length()==0) {
                            try {
                                buf.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        r=buf.toString();
                        buf.delete(0, buf.length());
                    }
	                System.out.print(r);
	                if (lw!=null) lw.print(r);
	            }
	        }
	    };
	    t.setDaemon(true);
	    t.start();
	}
	public static void addBuf(String s) {
	    if (delay) {
	        synchronized (buf) {
	            buf.append(s+"\n");
	            buf.notifyAll();
	        }
	    } else {
	        System.out.println(s);
	    }
	}
	public static String reportStat() {
	    StringBuilder b=new StringBuilder();
	    for (String s: stat.keySet()) {
	        Stat st=stat.get(s);
	        b.append(s+"\t"+st.count+"\t"+st.length+"\n");
	    }
	    return b.toString();
	}
	public static void d(Object tag,Object content) {
		//if ("ToValues".equals(tag) || "ClassIdx".equals(tag) ||"getSPClass".equals(tag)) {
		tag=convTag(tag);
	    if ( tagMatch(tag) && wordMatch(content)) {
			String cont = "["+tag+"]"+content;
			String s=new TDate().toString("yy/MM/dd HH:mm:ss.SSS")+":"+cont;
            addStat(tag+"", s.length());
            addBuf(s);
			//System.out.flush();
		}
	}

	private static void addStat(String tag, int length) {
        Stat s=stat.get(tag);
        if (s==null) {
            s=new Stat();
            stat.put(tag, s);
        }
        s.count++;
        s.length+=length;
    }
    private static Object convTag(Object tag) {
	    if (tag==null) return "null";
	    if (tag instanceof String) {
            String s=(String) tag;
            return s;
        }
	    if (tag instanceof Class) {
            Class<?> cl=(Class<?>) tag;
            return cl.getName();
        }
        return tag.getClass().getName();
    }

    private static boolean wordMatch(Object content) {
		return true;//(content+"").indexOf("root@")>=0;
	}

	private static boolean tagMatch(Object tag) {
		return (useWhiteList && whiteList.contains(tag+"")) ||
				(!useWhiteList && !blackList.contains(tag+""));
	}

	public static Object die(String string) {
		throw new RuntimeException(string);

	}

	public static void w(Object tag, Object content) {
		d(tag,content);
	}

	public static <T> T notNull(T value,String msg) {
		if (value==null) die(msg);
		return value;
	}

	public static void die(Exception e) {
		die("Log:die Wrapped Exception :"+e);
	}
	public static StringWriter errorLog=new StringWriter();
	public static void e(Exception e) {
		PrintWriter p = new PrintWriter(errorLog);
		e.printStackTrace(p);
		p.close();
	}
}