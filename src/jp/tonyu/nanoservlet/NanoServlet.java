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

package jp.tonyu.nanoservlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class NanoServlet extends NanoHTTPD {
	HttpServlet servlet;
	/**
	 * Run HttpServlet on NanoHTTPD
	 *
	 * @param port   Port number
	 * @param servlet  servlet which runs on NanoHTTPD
	 * @throws IOException
	 */
	private AutoRestart autoRestart;
	public NanoServlet(int port, HttpServlet servlet, AutoRestart auto) throws IOException {
		this(port,servlet);
		autoRestart=auto;
	}
	public NanoServlet(int port, HttpServlet servlet) throws IOException {
		super(port);
		this.servlet=servlet;
	}
	boolean stopped=false;
	@Override
	public Response serve(HttpServletRequest req) {
	    //HttpServletRequest req=new RequestWrapper(uri, method,	 header, parms , files);
		ResponseWrapper res = new ResponseWrapper(this);
		try {
			if (stopped) {
			} else if (autoRestart!=null && autoRestart.hasToBeStopped(req.getPathInfo())) {
				res.setContentType("text/plain");
				stopped=true;
			} else {
				servlet.service(req, res);
			}
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res.close();

	}
	public boolean hasToBeStopped() {
		return stopped;
	}
	public void stop() {
		super.stop();
		if (autoRestart!=null) autoRestart.stop();

	}
}