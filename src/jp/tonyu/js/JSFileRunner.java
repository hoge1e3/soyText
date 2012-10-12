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

package jp.tonyu.js;

import java.io.IOException;

import jp.tonyu.debug.Log;
import jp.tonyu.util.SFile;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JSFileRunner {
    Context ctx;
    Scriptable root;
    public JSFileRunner()  {
        ctx=Context.enter();
        root=ctx.initStandardObjects();
        ScriptableObject.putProperty(root, "sys", this);
        ScriptableObject.putProperty(root, "console", this);
    }
    public void log(Object s) {
        Log.d("log", s);
    }
    public void close() {
        Context.exit();
    }
    public Object load(SFile file) throws IOException {
        return ctx.evaluateString(root, file.text(), file.fullPath(), 1, null);
    }
    public Object load(String fileName) throws IOException {
        return load(new SFile(fileName));
    }
    public String fileText(String fileName) throws IOException {
        return new SFile(fileName).text();
    }
    public Object call(Function f,Scriptable self,Object... args) {
        return f.call(ctx, root, self, args);
    }
    public Object call(Function f, Object...args ) {
        return call(f, root, args);
    }
}