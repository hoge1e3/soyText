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

package jp.tonyu.soytext2.servlet;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.auth.AuthenticatorList;
import jp.tonyu.soytext2.auth.LocalRoot;
import jp.tonyu.util.Context;

public class Auth implements Wrappable {
	private String user;
	private final AuthenticatorList a;
	public static final Context<Auth> cur=new Context<Auth>();

	public Auth(AuthenticatorList a) {
		super();
		this.a = a;
	}
	public String login(Object credential) {
		//AuthenticatorList a=authenticator();
		String user=LocalRoot.check(credential);
		if (user==null && a!=null){
			user=a.check(credential);
		}
		if (user!=null) {
			this.user=user;
		}
		return user;
	}
	public String user() {
		String user=(this.user==null?"nobody":this.user); //  currentSession().userName();
		return user;
	}
	public boolean isRootUser() {
	    return LocalRoot.isRootUser(user()) || a.isRootUser(user());
	}
}