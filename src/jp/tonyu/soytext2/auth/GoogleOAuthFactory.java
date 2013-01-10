package jp.tonyu.soytext2.auth;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.arnx.jsonic.JSONException;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.util.SFile;

public class GoogleOAuthFactory implements Wrappable {
	public GoogleOAuth create() throws JSONException, FileNotFoundException, IOException {
		SFile conf=DocumentLoader.cur.get().getWorkspace().getConfig(getClass());
		SFile cli=conf.rel("client_secrets.json");
		GoogleOAuth res = new GoogleOAuth(cli);
		return res;
	}
}
