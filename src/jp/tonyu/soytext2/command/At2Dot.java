package jp.tonyu.soytext2.command;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.file.Comm;
import jp.tonyu.util.MD5;
import jp.tonyu.util.Maps;
import jp.tonyu.util.SFile;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

public class At2Dot {
	static String idPat = "(byId\\([\"'][^\"']+)@([^\"']+[\"']\\))";
	static Pattern around=Pattern.compile("......................................................@................");
	public static void main2(String[] args) throws JSONException, FileNotFoundException, IOException {
		String content="  byId(\"123@4.tonyu.com\");";
		content=content.replaceAll(idPat, "$1.$2");
		System.out.println(content);
	}
	   public static SFile getNewestBackupFile(SFile backupDir) {
	        SFile src=null;
	        Log.d("newestBackup", "Search backup "+backupDir+" *.json");
	        for (SFile txt : backupDir) {
	            if (!txt.name().endsWith(".json"))
	                continue;
	            if (txt.name().startsWith("dot_"))
	                continue;
	            if (src==null||txt.lastModified()>src.lastModified()) {
	                src=txt;
	            }
	        }
	        if (src==null)
	            Log.die("Backup not found in "+backupDir);
	        return src;
	    }
	public static void main(String[] args) throws Exception {
		SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
		//String fm=/*"Sun "+*/"Apr 15 16:11:21 JST 2012";
		//fmt="MMM dd HH:mm:ss z yyyy";
		//System.out.println(df.format( new Date()));

		Common.parseArgs(args);
		SFile sFile=getNewestBackupFile(Common.workspace.getBackupDir(Common.dbid));

		//SFile sFile = new SFile(args[0]);
        System.out.println("sFile="+sFile);
        //String src=sFile.text();
		SFile newFile = sFile.parent().rel("dot_"+sFile.name());
		Map m=(Map)JSON.decode(sFile.inputStream());
		List docs=(List)m.get("DocumentRecord");
		Map<String,String> ids=new HashMap<String,String>();
		Map<Long, Date> lu2Date=new HashMap<Long, Date>();
        List logs=(List)m.get("LogRecord");
        if (logs!=null) {
            for (Object l:logs) {
                Map log=(Map)l;
                long id= Long.parseLong(log.get("id")+"");
                Date d = df.parse( log.get("date")+"" );
                //d.to
                lu2Date.put(id,d);
                //System.out.println(id+"-"+d);
            }
        }
		//System.exit(1);
		int ser=0;
		for (Object s:docs) {
			Map doc = (Map)s;
			String id = doc.get("id")+"";
			String newId=id.replace("@", ".");
			ids.put(id,newId);
			if (Common.dbid.equals(SDB.dbIdPart(newId))) {
				int se=SDB.serialPart(newId);
				if (se>ser) ser=se;
			}
			//System.out.println(newId);
			doc.put("id", newId);

			String scope=doc.get("scope")+"";
			if (scope.compareTo("null")!=0) {
	            doc.put("scope", scope.replaceAll("@","."));
			}
			String con=doc.get("constructor")+"";
            if (con.compareTo("null")!=0) {
                doc.put("constructor", con.replaceAll("@","."));
            }

			convertDate(lu2Date, doc, "lastUpdate");
			convertDate(lu2Date, doc, "lastAccessed");
			String content= doc.get("content")+"";
			//                   byId\("[^"]+@[^"]+"\)
			//                  (byId\("[^"]+)@([^"]+"\))
			//                  (byId\\(\"[^\"]+)@([^\"]+\"\\))
			content=content.replaceAll(idPat, "$1.$2");
			doc.put("content", content);

			doc.put("version", MD5.crypt(content));

			String sum=doc.get("summary")+"";
			int i=0;
			Matcher ma = around.matcher(content);
			while (true) {
				if (ma.find(i)) {
					String group = ma.group();
					if (group.matches(".*@[a-z\\.]+\\.(com|jp).*") ||
							group.contains("アドレス")||
							group.contains("ac.jp/soytext2/exec/")
							) {

					} else {
						System.out.println(id+":"+sum+": "+group);
					}
					i=ma.end();
				} else break;
			}
		}
		for (Object s:docs) {
			Map doc = (Map)s;
			String sum=doc.get("summary")+"";
			if (ids.get(sum)!=null) {
				doc.put("summary",ids.get(sum));
			}
		}
		m.remove("IndexRecord");
		m.remove("LogRecord");
		m.put("IdSerialRecord", new Object[]{Maps.create("serial", ser).p("id", 0)});
		/*int idc=ids.size();
		int c=0;
		for (String oldId:ids.keySet()) {
			String newId=ids.get(oldId);
			oldId=oldId.replaceAll("\\.", "\\\\$0");
			newId=newId.replaceAll("\\.", "\\\\$0");
			System.out.println("["+c+"/"+idc+"]"+"s/"+oldId+"/"+newId+"/g");
			src=src.replaceAll(oldId, newId);
		}*/
		JSON j=new JSON();
		j.setPrettyPrint(true);
		j.format(m, newFile.outputStream());
		//newFile.text(j.format(m));
		/*for (String s:newFile.lines()) {
			if (s.indexOf('@')>=0) {
				System.out.println(s);
			}
		}*/
	}
	private static void convertDate(Map<Long, Date> lu2Date, Map doc,
			String key) {
		long lu= Long.parseLong(doc.get(key)+"");
		Date nlu = lu2Date.get(lu);
		if (nlu==null) doc.put(key, 0 );
		else doc.put(key, nlu.getTime() );
	}
}
