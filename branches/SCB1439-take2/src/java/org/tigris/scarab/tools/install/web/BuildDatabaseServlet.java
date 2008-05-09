package org.tigris.scarab.tools.install.web;

/* ================================================================
 * Copyright (c) 2000-2005 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BuildDatabaseServlet extends HttpServlet {

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		

		try {
			ResourceBundle rb = ResourceBundle.getBundle("InstallBundle");

			File currentDirectory = new File(System.getProperty("user.dir"));
			String buildDirectoryName = currentDirectory.getParentFile().getParentFile().getAbsolutePath() + File.separatorChar + "build";
			String commandLine2 = null;
			
			// Write a couple of batch (command) file
			// I could not find another way to launch ant in another directory
			if ("Windows".equals(System.getProperty("os.name").substring(0, 7))) {
		        BufferedWriter batch2 = new BufferedWriter(new FileWriter("build-scarab-db.cmd"));
		        batch2.write("cd " + buildDirectoryName + "\r\n");
		        batch2.write("ant create-db\r\n");
		        batch2.close();
		        
		        commandLine2 = "cmd /c build-scarab-db.cmd";
			} else {
		        BufferedWriter batch2 = new BufferedWriter(new FileWriter("build-scarab-db"));
		        batch2.write("#! /bin/sh\n");
		        batch2.write("cd " + buildDirectoryName + "\n");
		        batch2.write("ant\n");
		        batch2.close();
		        
		        commandLine2 = "sh ./build-scarab-db";
			}

			// Launch second batch to build the Scarab database
			out.println("<div class=\"infomessage\">");
			out.println("<strong>Phase 2</strong> : " + rb.getString("building.scarab.database"));
			out.println("</div>");
			
			Process child = Runtime.getRuntime().exec(commandLine2);
			
			out.println("<pre>");
			InputStream in = child.getInputStream();
			int c;
			while ((c = in.read()) != -1) {
				out.print((char) c);
			}
			out.println("</pre>");
			in.close();
			
			if (child.exitValue() == 0) {
				out.println("<p class=\"infomark\">" + rb.getString("success") + "</p>");
			} else {
				out.println("<p class=\"errormark\">" + rb.getString("failure") + "</p>");
			}
		} catch (MissingResourceException mre) {
			System.out.println("Something is wrong with the installation, I could not find the InstallBundle*.properties resource bundles");
		} catch (Exception e) {
			out.println("<div class=\"errormessage\">");
			out.println("An error occured, stack trace follows: ");
			out.println("</div>");
			out.println("<pre>");
			e.printStackTrace(out);
			out.println("</pre>");
		}
		
	}

}
