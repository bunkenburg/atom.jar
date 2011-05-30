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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/** Superclass for PageContext implementations.
 * This class gives the convenience methods and a basis for page scope.
 * The rest of the methods throws new NotImplementedException("not implemented").
 * @author alex
 */
public class DummyPageContext extends PageContext {

	//State -------------------------------------
	
	/** If this is a request for an exception page, here the exception. */
	private Exception exception;
	
	/** Basis for a page scope implementation. */
	private Map<String, Object> pageScope=new HashMap<String, Object>();
	
	private Servlet servlet;//page
	private JspWriter out;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;
	private ServletConfig servletConfig;
	private ServletContext servletContext;

	//Constructors -----------------------------------------
	
	protected DummyPageContext(){}
	
	//Methods ----------------------------------------------
	
	/** Searches for the named attribute in page, request, session (if valid), 
	 * and application scope(s) in order and returns the value associated or null. 
	 * @see javax.servlet.jsp.PageContext#findAttribute(java.lang.String)
	 */
	@Override public Object findAttribute(String name) {
		Object att = null;
		//page
		att = this.getAttribute(name);
		if (att!=null) return att;
		//request
		ServletRequest request = this.getRequest();
		att = request.getAttribute(name);
		if (att!=null) return att;
		//session
		HttpSession session = this.getSession();
		att = session.getAttribute(name);
		if (att!=null) return att;
		//application
		ServletContext context = this.getServletContext();
		att = context.getAttribute(name);
		if (att!=null) return att;
		return null;
	}

	/** Throws RuntimeException("not implemented").
	 * @see javax.servlet.jsp.PageContext#forward(java.lang.String)
	 * Should work for absolute and relative paths. Untested.
	 * @param relativeURL
	 * @throws ServletException
	 * @throws IOException
	 *  */
	@Override public void forward(String relativeURL) throws ServletException, IOException {
		HttpServletRequest request = this.getRequest();
		RequestDispatcher dispatcher = request.getRequestDispatcher(relativeURL);
		dispatcher.forward(this.getRequest(), this.getResponse());
	}

	/** Returns the object associated with the name in the page scope 
	 * or null if not found. 
	 * Simple implementation: subclass may want to override.
	 * @see javax.servlet.jsp.PageContext#getAttribute(java.lang.String)
	 */
	@Override public Object getAttribute(String name) {
		return this.pageScope.get(name);
	}

	/** Return the object associated with the name in the specified scope or 
	 * null if not found. 
	 * @see javax.servlet.jsp.PageContext#getAttribute(java.lang.String, int)
	 */
	@Override public Object getAttribute(String name, int scope) {
		switch (scope){
		case PAGE_SCOPE:  
			return this.getAttribute(name);
		case REQUEST_SCOPE:
			ServletRequest request = this.getRequest();
			return request.getAttribute(name);
		case SESSION_SCOPE:
			HttpSession session = this.getSession();
			return session.getAttribute(name);
		case APPLICATION_SCOPE:
			ServletContext context = this.getServletContext();
			return context.getAttribute(name);
		default:
			throw new IllegalArgumentException();
		}
	}

	/** Enumerate all the attributes in a given scope. 
	 * Simple implementation for page scope: subclass may want to override.
	 * The other scopes are implemented fine.
	 * @see javax.servlet.jsp.PageContext#getAttributeNamesInScope(int)
	 */
	@SuppressWarnings("unchecked")
	@Override public Enumeration<String> getAttributeNamesInScope(int scope) {
		switch (scope){
		case PAGE_SCOPE:
			final Iterator<String> keys = this.pageScope.keySet().iterator();
			Enumeration<String> e = new Enumeration<String>(){
				public boolean hasMoreElements(){
					return keys.hasNext();
				}
				public String nextElement(){
					return keys.next();
				}
			};
			return e;
		case REQUEST_SCOPE:
			ServletRequest request = this.getRequest();
			return request.getAttributeNames();
		case SESSION_SCOPE:
			HttpSession session = this.getSession();
			return session.getAttributeNames();
		case APPLICATION_SCOPE:
			ServletContext context = this.getServletContext();
			return context.getAttributeNames();
		default:
			throw new IllegalArgumentException();
		}
	}

	/** Get the scope where a given attribute is defined. 
	 * @see javax.servlet.jsp.PageContext#getAttributesScope(java.lang.String)
	 */
	@Override public int getAttributesScope(String name) {
		Object att = null;
		//page
		att = this.getAttribute(name);
		if (att!=null) return PAGE_SCOPE;
		//request
		ServletRequest request = this.getRequest();
		att = request.getAttribute(name);
		if (att!=null) return REQUEST_SCOPE;
		//session
		HttpSession session = this.getSession();
		att = session.getAttribute(name);
		if (att!=null) return SESSION_SCOPE;
		//application
		ServletContext context = this.getServletContext();
		att = context.getAttribute(name);
		if (att!=null) return APPLICATION_SCOPE;
		return 0;
	}

	/** The current value of the exception object (an Exception). 
	 * @see javax.servlet.jsp.PageContext#getException()
	 */
	@Override public Exception getException(){return this.exception;}

	/** The current value of the out object (a JspWriter). 
	 * @see javax.servlet.jsp.PageContext#getOut()
	 * */
	@Override public JspWriter getOut(){return this.out;}

	protected void setException(Exception exception){this.exception=exception;}
	protected void setOut(JspWriter out){this.out=out;}
	protected void setRequest(HttpServletRequest request){this.request=request;}
	protected void setResponse(HttpServletResponse response){this.response=response;}
	public void setSession(HttpSession session){this.session=session;}

	/** The current value of the page object (In a Servlet environment, this 
	 * is an instance of javax.servlet.Servlet). 
	 * @see javax.servlet.jsp.PageContext#getPage()
	 */
	@Override public Servlet getPage(){return this.servlet;}

	/** The current value of the request object (a ServletRequest).
	 * @see javax.servlet.jsp.PageContext#getRequest()
	 */
	@Override public HttpServletRequest getRequest(){return this.request;}

	/** The current value of the response object (a ServletResponse). 
	 * @see javax.servlet.jsp.PageContext#getResponse()
	 */
	@Override public HttpServletResponse getResponse(){return response;}

	protected Servlet getServlet(){return this.servlet;}
	protected void setServlet(Servlet servlet){this.servlet=servlet;}

	/** The ServletConfig instance. 
	 * @see javax.servlet.jsp.PageContext#getServletConfig()
	 */
	@Override public ServletConfig getServletConfig(){return this.servletConfig;}

	protected void setServletConfig(ServletConfig config){this.servletConfig=config;}
	
	/** The ServletContext instance.
	 * @see javax.servlet.jsp.PageContext#getServletContext()
	 */
	@Override public ServletContext getServletContext(){return servletContext;}

	protected void setServletContext(ServletContext context){this.servletContext=context;}
	
	/** The current value of the session object (an HttpSession). 
	 * @see javax.servlet.jsp.PageContext#getSession()
	 */
	@Override public HttpSession getSession(){return session;}

	/** Delegates to handlePageException(Throwable throwable).
	 * @see javax.servlet.jsp.PageContext#handlePageException(java.lang.Exception)
	 */
	@Override public void handlePageException(Exception exception) throws ServletException, IOException {
		Throwable throwable = exception;
		this.handlePageException(throwable);
	}

	/** Not implemented.
	 * @see javax.servlet.jsp.PageContext#handlePageException(java.lang.Throwable)
	 */
	@Override public void handlePageException(Throwable throwable) throws ServletException, IOException {
		throw new NotImplementedException();
	}

	/** Not implemented.
	 * @see javax.servlet.jsp.PageContext#include(java.lang.String)
	 */
	@Override public void include(String arg0) throws ServletException, IOException {
		throw new NotImplementedException();
	}

	/** Not implemented.
	 * @see javax.servlet.jsp.PageContext#initialize(javax.servlet.Servlet, javax.servlet.ServletRequest, javax.servlet.ServletResponse, java.lang.String, boolean, int, boolean)
	 */
	@Override public void initialize(
		Servlet servlet, ServletRequest request, ServletResponse response, 
		String errorPageURL, boolean needsSession, int bufferSize, boolean autoFlush
	)throws IOException, IllegalStateException, IllegalArgumentException{
		throw new NotImplementedException();
	}

	/** Does nothing.
	 * @see javax.servlet.jsp.PageContext#release()
	 */
	@Override public void release(){}

	/** Remove the object reference associated with the given name from all 
	 * scopes. Does nothing if there is no such object. 
	 * @see javax.servlet.jsp.PageContext#removeAttribute(java.lang.String)
	 */
	@Override public void removeAttribute(String name) {
		this.removeAttribute(name, PAGE_SCOPE);
		this.removeAttribute(name, REQUEST_SCOPE);
		this.removeAttribute(name, SESSION_SCOPE);
		this.removeAttribute(name, APPLICATION_SCOPE);
	}

	/** Remove the object reference associated with the specified name in the 
	 * given scope. Does nothing if there is no such object.
	 * Simple implementation for page scope: subclass may want to override.
	 * For the other scopes, the implementation here is fine.
	 * @see javax.servlet.jsp.PageContext#removeAttribute(java.lang.String, int)
	 */
	@Override public void removeAttribute(String name, int scope) {
		switch (scope){
		case PAGE_SCOPE:
			this.pageScope.remove(name);
		case REQUEST_SCOPE:
			ServletRequest request = this.getRequest();
			request.removeAttribute(name);
		case SESSION_SCOPE:
			HttpSession session = this.getSession();
			session.removeAttribute(name);
		case APPLICATION_SCOPE:
			ServletContext context = this.getServletContext();
			context.removeAttribute(name);
		default:
			throw new IllegalArgumentException();
		}
	}

	/** Simple implementation: subclass may want to override.
	 * Register the name and value specified with page scope semantics. 
	 * If the value passed in is null, this has the same effect as calling 
	 * removeAttribute( name, PageContext.PAGE_SCOPE ). 
	 * @see javax.servlet.jsp.PageContext#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override public void setAttribute(String name, Object value) {
		this.pageScope.put(name, value);
	}

	/** Register the name and value specified with appropriate scope semantics. 
	 * If the value passed in is null, this has the same effect as calling 
	 * removeAttribute( name, scope ). 
	 * @see javax.servlet.jsp.PageContext#setAttribute(java.lang.String, java.lang.Object, int)
	 */
	@Override public void setAttribute(String name, Object value, int scope) {
		switch (scope){
		case PAGE_SCOPE:
			this.setAttribute(name, value);
			break;
		case REQUEST_SCOPE:
			ServletRequest request = this.getRequest();
			request.setAttribute(name, value);
			break;
		case SESSION_SCOPE:
			HttpSession session = this.getSession();
			session.setAttribute(name, value);
			break;
		case APPLICATION_SCOPE:
			ServletContext context = this.getServletContext();
			context.setAttribute(name, value);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override public void include(String arg0, boolean arg1) throws ServletException,IOException {
		throw new NotImplementedException();
	}

	@Override public ELContext getELContext() {
		throw new NotImplementedException();
	}

	@SuppressWarnings("deprecation")
	@Override public javax.servlet.jsp.el.ExpressionEvaluator getExpressionEvaluator() {
		throw new NotImplementedException();
	}

	@SuppressWarnings("deprecation")
	@Override public javax.servlet.jsp.el.VariableResolver getVariableResolver() {
		throw new RuntimeException();
	}

}