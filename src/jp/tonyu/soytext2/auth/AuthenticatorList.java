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

package jp.tonyu.soytext2.auth;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;

public class AuthenticatorList implements Authenticator, Wrappable {
	//public static Authentificator alist=prepare();
	Vector<Authenticator> auths=new Vector<Authenticator>();
	Set<String> roots=new HashSet<String>();
	@Override
	public boolean check(String username, String password) {
		for (Authenticator a: auths) {
			if (a.check(username, password)) return true;
		}
		return false;
	}
	public void install(Authenticator a) {
		Log.d(this, "Installed - "+a);
		auths.add(a);
	}
	public void addRootUser(String userName) {
		roots.add(userName);
	}
	public boolean isRootUser(String userName) {
		return roots.contains(userName);
	}
	public static AuthenticatorList repare() {
		AuthenticatorList res=new AuthenticatorList();
		res.install(new AnyAuth(""));
		/*res.install(new Authentificator() {
			
			@Override
			public boolean check(String username, String password) {
				return "all".equals(username) && "oll".equals(password);
			}
		});*/
		return res;
	}
}