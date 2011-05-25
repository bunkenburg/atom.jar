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
package inspiracio.security;

import inspiracio.servlet.jsp.PageContextFactory;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;


import org.apache.log4j.Logger;

/** A factory for knowing who is logged in.
 * Use:
 * Principal p=PrincipalFactory.getCallerPrincipal();
 * */
public class PrincipalFactory{
	private static final Logger logger=Logger.getLogger(PrincipalFactory.class);

	/** no instantiation: everything static */
	private PrincipalFactory(){}

	//Static state ---------------------------------------------------

	private static List<PrincipalMaker> makers=new ArrayList<PrincipalMaker>();

	// Methods ------------------------------------------------

	public static synchronized void addPrincipalMaker(PrincipalMaker maker){
		makers.add(maker);
	}

	/** Gets the current user as Principal with details.
	 * Returns null if none of the registered makers are appropriate
	 * or if none can authenticate the current user.
	 * */
	public static Principal getCallerPrincipal(){
		//Assume we are in a web application and cache the result in the session.
		//Revise this code when we have request that are not web.
		PageContext pc=PageContextFactory.getPageContext();
		Principal principal=null;
		HttpSession session=pc.getSession();
		if(session!=null){
			//logger.debug("Session is not set, I cannot get principal");//I think this line is wrong here.
			principal=(Principal)session.getAttribute("callerPrincipal");
		}

		if(principal!=null)
			logger.trace("Principal " + principal + " was cached on session");
		else{
			//Careful: not quite threadsafe. This is like an event dispatcher.
			for(PrincipalMaker maker : makers){
				logger.debug("Looking for principal in maker: " + maker);
				principal=maker.getCallerPrincipal();
				if(principal!=null)
					break;
			}//for
			if(principal!=null){
				logger.debug("Principal " + principal + " found! Caching in session as \"callerPrincipal\"");
				if(session!=null)	//How can session ever be null?
					session.setAttribute("callerPrincipal", principal);
			}else
				logger.debug("I couldn't get the principal");
		}
		return principal;
	}

	/** Sets the caller principal programmatically.
	 * The client is responsible for correctness and safety.
	 * @param principal The principal to set. */
	public static void setCallerPrincipal(Principal principal){
		PageContext pc=PageContextFactory.getPageContext();
		HttpSession session=pc.getSession();
		session.setAttribute("callerPrincipal", principal);
	}
}