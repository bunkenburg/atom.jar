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
package inspiracio.servlet.jsp;

import inspiracio.lang.NotImplementedException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;


/** A simple partial implementation of PageContext.
 * Can be initialised from within a HttpServlet.
 * Instances are obtained from a factory method in PageContextFactory.
 * An instance must be initialised with:
 * <ul>
 * 	<li>servlet
 * 	<li>request
 * 	<li>response.
 * </ul> 
 * */
public class ServletPageContext extends DummyPageContext {
	
	//State -------------------------------------------------------------------
	
	/** the servlet that uses this PageContext */
	private HttpServlet servlet;
	
	/** the request we are executing */
	private HttpServletRequest request;
	
	/** the response that we are constructing */
	private HttpServletResponse response;
	
	/** the servlet config */
	private ServletConfig config;
	
	/** the servlet context */
	private ServletContext context;

	//Constructors -----------------------------------------------------------
	
	ServletPageContext(){}
	
	//Accessors --------------------------------------------------------------

	/** Could implement by making an implementation of JspWriter that is based on the 
	 * Writer given by Response.getWriter.
	 * @return nothing
	 * @throws RuntimeException always
	 */
	@Override public JspWriter getOut(){throw new NotImplementedException();}

	@Override public HttpSession getSession(){return request.getSession();}
	@Override public HttpServlet getPage(){return servlet;}
	
	/** Sets servlet, config, context.
	 * @param servlet
	 */
	void setPage(HttpServlet servlet){
		this.servlet=servlet;
		config=servlet.getServletConfig();
		if(config!=null)
			context=config.getServletContext();
	}

	@Override public HttpServletRequest getRequest(){return request;}
	protected void setRequest(HttpServletRequest request){this.request=request;}
	@Override public HttpServletResponse getResponse(){return response;}
	protected void setResponse(HttpServletResponse response){this.response=response;}
	@Override public ServletConfig getServletConfig(){return config;}
	@Override public ServletContext getServletContext(){return context;}

	/** @return original complete servlet path (takes possible forwards into account) */
	public String getOriginalCompleteServletPath(){
		//Maybe it is cached in a request attribute.
		ServletRequest request=this.getRequest();
		String sp=(String)request.getAttribute("ORIGINAL_COMPLETE_SERVLET_PATH");
		//If there is nothing, get and cache the current complete servlet path.
		if(sp==null){
			sp=this.getCompleteServletPath();
			request.setAttribute("ORIGINAL_COMPLETE_SERVLET_PATH", sp);
		}
		return sp;
	}

	//Methods --------------------------------------------------------------

	/** Sets the instance variables to null. */
	@Override public void release() {
		this.servlet=null;
		this.request=null;
		this.response=null;
		this.config=null;
		this.context=null;
	}

	// helpers ---------------------------------------------------
	
	/** Returns the complete servlet path, like
	 * "https://localhost/studio/atom/"
	 * @return complete servlet path
	 */
	private String getCompleteServletPath(){
		HttpServletRequest request=(HttpServletRequest)this.getRequest();
		StringBuffer completeServletPath=request.getRequestURL();//not complete URL, only up to the servlet. Missing extra path info.
		String servletPathWithoutProtocolAndHost=request.getServletPath(); // Starts with "/", ends after the servlet name & before extra path info or query string
		if(servletPathWithoutProtocolAndHost.length()==0){
			//This happens if the servlet is registered for the "/*" pattern.
			return completeServletPath.toString();
		}
		int servletPathIndex=completeServletPath.indexOf(servletPathWithoutProtocolAndHost); // The index of the first "/" after the server name & port
		int end = servletPathIndex + servletPathWithoutProtocolAndHost.length(); // The first char of extra path info or query string
		completeServletPath.replace(end, completeServletPath.length(), ""); // Remove the extra path information and the query string. StringIndexOutOfBoundsException if assertion was false.
		//Assert: completeServletPath has protocol, host, port, and servlet path. No extra path info, no query string.		
		return completeServletPath.toString();
	}

}