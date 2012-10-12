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

package jp.tonyu.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public abstract class ResourceTraverser {
	ClassLoader cl=this.getClass().getClassLoader();
	public void traverse(String name) throws IOException {
		if (!isDir(name)) traverseAsFile(name);
		else traverseAsDir(name);
	}
	public void traverseAsFile(String name) throws IOException {
		//InputStream i = getInputStream(name);
		visitFile(name);
	}
	protected InputStream getInputStream(String name) {
		InputStream i=cl.getResourceAsStream(name);
		return i;
	}
	protected boolean isDir(String name) {
		return name.indexOf(".")<0;
	}
	protected abstract void visitFile(String name) throws IOException;
	protected boolean visitDir(String name, List<String> files)  throws IOException{
		return false;
	}
	public void traverseAsDir( String name) throws IOException {
		InputStream resStr = cl.getResourceAsStream(name);
		if (resStr==null) {
			System.out.println(name+" is not found.");
			return;
		}
		Scanner s=new Scanner(resStr);
		Vector<String> files=new Vector<String>();
		while (s.hasNextLine()) {
			String r=s.nextLine();
			files.add(r);
		}
		s.close();
		if (!visitDir(name, files)) {
			for (String n:files) {
				traverse(name+"/"+n);
			}
		}
	}
}