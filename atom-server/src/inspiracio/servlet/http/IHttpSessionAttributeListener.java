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

import inspiracio.lang.Equals;
import inspiracio.servlet.jsp.PageContextFactory;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.jsp.PageContext;


/** Convenient superclass for session attribute listeners:
 * remembers its key. */
public class IHttpSessionAttributeListener implements HttpSessionAttributeListener {

	//State ---------------------------------------------------
	
	private String key;
	
	//Constructors --------------------------------------------
	
	public IHttpSessionAttributeListener(String key){this.key=key;}
	
	//Methods -------------------------------------------------
	
	public String getKey(){return key;}
	
	/** Sets the key. You can call this while the listener is registered:
	 * it will remove and re-add itself. */
	public void setKey(String key){
		if(!Equals.equals(key,this.key)){
			PageContext pc=PageContextFactory.getPageContext();
			CachingHttpSession session=(CachingHttpSession)pc.getSession();
			session.removeSessionAttributeListener(this);
			this.key=key;
			session.addSessionAttributeListener(this, key);
		}
	}
	
	/** Does nothing: client can override. */
	@Override public void attributeAdded(HttpSessionBindingEvent e){}

	/** Does nothing: client can override. */
	@Override public void attributeRemoved(HttpSessionBindingEvent e){}

	/** Calls attributeAdded, that is a usual implementation.
	 * Client can override. */
	@Override public void attributeReplaced(HttpSessionBindingEvent e){this.attributeAdded(e);}

	@Override public String toString(){return "IHttpSessionAttributeListener[" + key + "]";}
}