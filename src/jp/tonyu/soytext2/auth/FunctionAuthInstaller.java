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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.JSSession;

public class FunctionAuthInstaller implements Wrappable {
	public void install(AuthenticatorList ls, final Function f) {
		ls.install(new Authenticator() {

			@Override
			public String check(final Object credential) {
				Object r=JSSession.withContext(new ContextRunnable() {

					@Override
					public Object run(Context cx) {
						return f.call(cx, f, f, new Object[]{credential});
					}
				});
				if (r==null) return null;
				return r.toString();
			}
		});
	}
}