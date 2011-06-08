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

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/** Functionality that we add to javax.servlet.http.HttpServletRequest.
 * <p>
 * Invariant:
 * <ul>
 * 	<li>
 * 		If request.getAttribute(ORIGINAL_COMPLETE_SERVLET_PATH) is set,
 * 		it is the complete servlet path before any forwards.
 * 	<li>
 * 		If there has been a forward, it is set. 
 * </ul>
 * 
 * The class also provides setters for: 
 * <ul>
 * 	<li>user principal: java.security.Principal
 * 	<li>remote user: String
 * 	<li>auth type: String
 * </ul>
 * If they are not in this class, the getter returns whatever the wrapped object returns.
 * 
 * */
public class IHttpServletRequest extends HttpServletRequestWrapper{

	/** Store the original complete servlet path 
	 * in a request attribute with this key.
	 * By "original" I mean, the URL before a forward. 
	 * See class invariant.
	 * <p>
	 * The servlet path is stored a String. If it were a StringBuffer,
	 * a client could retain a reference to the StringBuffer and
	 * change its value from the outside. Strings are immutable.
	 * */
	public static final String ORIGINAL_COMPLETE_SERVLET_PATH = "inspiracio.servlet.http.IHttpServletRequest.originalCompleteServletPath";
	
	//State -------------------------------------------------
	
	/** Is the body of the request gzipped? */
	private boolean zippedRequest=false;
	
	/** The user principal that has been set or null. */
	private Principal userPrincipal = null;
	
	/** Records whether the user principal has been set, even if it 
	 * has been set to null. */
	private boolean userPrincipalSet = false;
	
	/** The remote user that has been set or null. */
	private String remoteUser = null;
	
	/** The auth type that has been set or null. */
	private String authType;
	
	//Constructors -----------------------------------------
	
	/** Wraps the given request
	 * @param request the HttpServletRequest to be wrapped
	 */
	public IHttpServletRequest(HttpServletRequest request){
		super(request);
	}
	
	/** Wraps the given request
	 * @param request the HttpServletRequest to be wrapped
	 * @param zippedRequest Is the body of the request gzipped?
	 */
	public IHttpServletRequest(HttpServletRequest request, boolean zippedRequest){
		this(request);
		this.zippedRequest=zippedRequest;
	}
	
	//Methods -----------------------------------------------
	
	/** If it has been set, the set value; otherwise delegate to wrapped object.
	 * @return the authType
	 */
	@Override public String getAuthType() {
		return this.authType!=null ? this.authType : super.getAuthType();
	}

	/** Gets a stream to read the request. */
	@Override public ServletInputStream getInputStream()throws IOException{
		ServletInputStream in=super.getInputStream();
		if(this.zippedRequest){
			try{
				InputStream i=new GZIPInputStream(in);
				in=new ServletInputStreamWrapper(i);
			}catch(IOException ioe){
				//Maybe Google AppEngine server already has unzipped (without changing the headers)?
				String message=ioe.getMessage();
				if("Not in GZIP format".equals(message))
					in=super.getInputStream();//I hope the GZipInputStream constructor has not yet consumed bytes.
				else
					throw ioe;
			}
		}
		return in;
	}
	
	/**
	 * @param authType the authType to set
	 */
	public void setAuthType(String authType) {
		this.authType = authType;
	}

	/** If it has been set, the set value;
	 * otherwise if the user principal is set, its name; 
	 * otherwise delegate to wrapped object.
	 * @return the remoteUser
	 */
	@Override public String getRemoteUser() {
		if (this.remoteUser!=null)
			return this.remoteUser;
		if (this.userPrincipal!=null)
			return this.userPrincipal.getName();
		return super.getRemoteUser();
	}

	/**
	 * @param remoteUser the remoteUser to set
	 */
	void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	/** If it has been set, the set value; otherwise delegate to wrapped object.
	 * @return the userPrincipal
	 */
	@Override public Principal getUserPrincipal() {
		return this.userPrincipal!=null ? this.userPrincipal : super.getUserPrincipal();
	}

	/**
	 * @param userPrincipal the userPrincipal to set
	 */
	public void setUserPrincipal(Principal userPrincipal) {
		this.userPrincipal = userPrincipal;
		this.userPrincipalSet = true;//even if the user principal is set to null!
	}

	/** @return the userPrincipalSet */
	public boolean getUserPrincipalSet() {
		return userPrincipalSet;
	}

}