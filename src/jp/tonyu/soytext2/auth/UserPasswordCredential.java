package jp.tonyu.soytext2.auth;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.Wrappable;

public class UserPasswordCredential extends BlankScriptableObject {
	private static final String PASSWORD = "password";
	private static final String USERNAME = "username";
	public String username,password;
	public static UserPasswordCredential extract(Object o) {
		if (o instanceof UserPasswordCredential) {
			UserPasswordCredential p = (UserPasswordCredential) o;
			return p;
		}
		if (o instanceof Scriptable) {
			Scriptable s = (Scriptable) o;
			Object uo=ScriptableObject.getProperty(s, USERNAME);
			Object po=ScriptableObject.getProperty(s, PASSWORD);
			if (uo instanceof String && po instanceof String) {
				return new UserPasswordCredential(uo+"", po+"");
			}
		}
		return null;
	}
	public UserPasswordCredential(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	@Override
	public Object get(String name, Scriptable start) {
		if (name.equals(USERNAME)) return username;
		if (name.equals(PASSWORD)) return password;
		return super.get(name, start);
	}
}
