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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/** The page context factory maintains a variable for each thread,
 * and puts the page context there. So we can access the page context from
 * anywhere on the same thread. 
 * <p>
 * To establish the page context for a thread, call setPageContext, at the start
 * of the request. 
 * */
public class PageContextFactory{
	//private static final Logger logger=Logger.getLogger(PageContextFactory.class);

	// No constructors -----------------------------------------
	
	private PageContextFactory(){}
	
	//State ----------------------------------------------------
	
	private static PageContextMaker maker=null;
	private static ThreadLocal<PageContext> threadLocal=new ThreadLocal<PageContext>();
	
	/** There's only one servlet context object in the whole server.
	 * Cache it here so that we can give it to DWRPageContexts that cannot reach it otherwise. 
	 * Every request refreshes it. */
	private static ServletContext servletContext=null;
	
	//Methods ---------------------------------------------------
	
	/** At the start of a request, call this to set an appropriate page context
	 * for the request. If you forget, the request will use a page context of a previous
	 * request (wrong). 
	 * <p>
	 * DWR requests cannot and needn't set the page context: see getPageContext(). 
	 * */
	public static void setPageContext(PageContext pc){
		threadLocal.set(pc);
		servletContext=pc.getServletContext();
	}
	
	/** At the start of a request to a servlet, call this to set an appropriate page context
	 * for the request. If you forget, the request will use a page context of a previous
	 * request (wrong). 
	 * @param servlet the calling servlet
	 * @param request the current request
	 * @param response the current response
	 * */
	public static void setServletPageContext(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response){
		ServletPageContext pc=new ServletPageContext();
		pc.setPage(servlet);
		pc.setRequest(request);
		pc.setResponse(response);
		PageContextFactory.setPageContext(pc);
	}
	
	/** Get the page context for this request.
	 * <p> 
	 * If this request is a DWR request, this method will return a page context
	 * good for DWR, otherwise whatever page context was last set.
	 * */
	public static PageContext getPageContext(){
		//try to get it from the thread
		PageContext pc=threadLocal.get();
		
		if(pc==null && maker!=null){//try to get it from a maker
			pc=maker.getPageContext();
			threadLocal.set(pc);
		}
		
		/*
		if(DWRPageContext.isDWR()){
			//If the first ever request is DWR, than we have no servlet context yet.
			//But the first request cannot realistically be DWR.
			if(servletContext==null){
				String m="First request is a DWR request. It will not have access to servletContext and servletConfig.";
				logger.warn(m);
			}
				
			pc=new DWRPageContext(servletContext);
		}
		*/
		
		return pc;
	}
	
	/** Sets the make of page contexts. If we use PageContextFactory in environments
	 * that are not web applications, put a maker here.
	 * <p>
	 * For example, when using page context factory in a java application, for using
	 * the session as a map of global variables. 
	 * */
	public static void setPageContextMaker(PageContextMaker m){maker=m;}

	/** Gets the servlet context for this web application. */
	static ServletContext getServletContext(){return servletContext;}
}