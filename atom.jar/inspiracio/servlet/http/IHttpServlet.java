/*  Copyright 2011 Alexander Bunkenburg alex@inspiracio.com

    This file is part of atom.jar.

    atom.jar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    atom.jar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with atom.jar.  If not, see <http://www.gnu.org/licenses/>.
 */
package inspiracio.servlet.http;

import inspiracio.servlet.jsp.PageContextFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.log4j.Logger;

/**
 *
 * @author iparraga
 */
public abstract class IHttpServlet extends HttpServlet {

	private static final String ACCEPT_ENCODING="Accept-Encoding";
	private static final String CONTENT_ENCODING="Content-Encoding";
	private static final String GZIP="gzip";

	private static final Logger logger = Logger.getLogger(IHttpServlet.class);

	//Constructors -------------------------------------------------------------

	public IHttpServlet(){}

	//Implement Servlet interface, delegating to Atom-methods ------------------

	/** Some general things.
	 * <ol>
	 * 	<li>Wrap response into IHttpServletResponse
	 * 	<li>Initialise PageContext
	 * 	<li>Simulate POST by PUT with x-HTTP-Method-Override
	 * 	<li>delegate to doPut, doPost, etc
	 * </ol>
	 * */
	@Override protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Manage gzip
		boolean zipResponse=false;
		String accept=request.getHeader(ACCEPT_ENCODING);
		if(accept!=null){
			zipResponse=accept.contains(GZIP);//Client wants a gzipped request.*/
		} else {
			logger.debug("Current client didn't send accept encoding header, so no " +
					"compression is applied");
		}

		boolean zippedRequest=false;
		String encoding=request.getHeader(CONTENT_ENCODING);
		if(encoding!=null){
			zippedRequest=encoding.contains(GZIP);//Client is sending a gzipped request body.
		} else {
			logger.debug("Current client didn't send content encoding header, so no " +
				"compression is supposed");
		}

		request=new IHttpServletRequest(request, zippedRequest);

		//Problem: Tomcat does not permit arbitrary text as http response message (=the text after the status code).
		//Workaround: wrap the response.
		response=new IHttpServletResponse(response, zipResponse);

		//Initialise page context factory.
		PageContextFactory.setServletPageContext(this, request, response);

		//Problem: JBoss's implementation of http basic authentication with POST gives me an
		//empty request input stream! But with PUT it works fine. So here a workaround:
		//Simulate a POST by sending a PUT with a http header indicating the real desired
		//http method POST.
		//See http://code.google.com/apis/gdata/docs/2.0/basics.html.
		String method=request.getHeader("X-HTTP-Method-Override");
		if(method==null)
			method=request.getMethod();

		if("DELETE".equals(method))
			this.doDelete(request, response);
		else if("GET".equals(method))
			this.doGet(request, response);
		else if("HEAD".equals(method))
			this.doHead(request, response);
		else if("OPTIONS".equals(method))
			this.doOptions(request, response);
		else if("POST".equals(method))
			this.doPost(request, response);
		else if("PUT".equals(method))
			this.doPut(request, response);
		else if("TRACE".equals(method))
			this.doTrace(request, response);
	}
}