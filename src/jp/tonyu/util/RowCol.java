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

public class RowCol {
	private CharSequence src;
	private int row,col;
	private int byteCount;
	public int setRowCol(int r, int c) {
		row=0;
		col=0;
		for (byteCount=0 ; byteCount<src.length() ; byteCount++) {
			char ch=src.charAt(byteCount);
			if (row>=r && col>=c) { return byteCount;}
			if (ch=='\n') {
				if (row>=r) { return byteCount; }
				row++; col=0;
			} else {
				col++;
			}
		}
		return byteCount;
	}
	public void setByteCount(int cnt) {
		row=0;
		col=0;
		if (cnt>=src.length()) cnt=src.length();
		for (byteCount=0 ; byteCount<cnt ; byteCount++) {
			char ch=src.charAt(byteCount);
			if (ch=='\n') {
				row++; col=0;
			} else {
				col++;
			}
		}
	}

	public CharSequence getSrc() {
		return src;
	}
	public int getRow() {
		return row;
	}
	public int getCol() {
		return col;
	}
	public int getByteCount() {
		return byteCount;
	}
	public RowCol(CharSequence src) {
		super();
		this.src = src;
	}
	@Override
	public String toString() {
		return "RowCol [byteCount=" + byteCount + ", col=" + col + ", row="
				+ row + ", src=" + src + "]";
	}
}