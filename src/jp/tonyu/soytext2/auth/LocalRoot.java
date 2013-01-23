package jp.tonyu.soytext2.auth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.util.SFile;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

public class LocalRoot {
	//public static final String LOCAL_ROOT_NAME="localroot";
	public static String rootName() {
		try {
			Map r = conf();
			return r.get("username").toString()	;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static boolean isRootUser(String user) {
		//Log.d("LocalRoot", user+" - "+rootName());
		return user.equals(rootName());
	}
	public static String check(Object credential) {
		try {
			Map r = conf();
			String rootPass=""+r.get("password");
			//Log.d("LocalRoot","rtp : "+rootPass);
			UserPasswordCredential cr = UserPasswordCredential.extract(credential);
			if (cr!=null) {
				String rn=rootName();
				//Log.d("LocalRoot","inp: "+cr.password);
				if (cr.username.equals(rn) &&  cr.password.equals(rootPass)) return rn;
			}
			return null;
		} catch (JSONException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		return null;
	}
	private static Map conf() throws IOException, FileNotFoundException {
		SFile f=DocumentLoader.cur.get().getWorkspace().getConfig(LocalRoot.class);
		f=f.rel("root.json");
		Map r;
		r = (Map)JSON.decode(f.inputStream());
		return r;
	}
}

