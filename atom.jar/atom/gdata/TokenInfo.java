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
package atom.gdata;

/** Token info as in Google's AuthSub authentication.
 * Just a bean.
 * 
 * @author BARCELONA\alexanderb
 *
 */
public class TokenInfo {

	/** Username of the authenticated person */
	private String username;
	
	/** URL identifying the client application that wants to use a service */
	private String target;
	
	/** hd as in AuthSub spec. For us, "candidate" | "employer" */
	private String hd;
	
	/** URL identifying the service that the client app wants to use */
	private String scope;
	
	/** Whether the client app is registered for the service */
	private boolean secure;

	/**
	 * @return the scope
	 */
	String getScope() {
		return scope;
	}

	/**
	 * @param scope the scope to set
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return the secure
	 */
	boolean getSecure() {
		return secure;
	}

	/**
	 * @param secure the secure to set
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	/**
	 * @param secure the secure to set. 0=false, 1=true
	 */
	public void setSecure(int secure) {
		this.secure = secure==1;
	}

	/**
	 * @return the target
	 */
	String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}
	
	/** for observation during development 
	 * @return string
	 * */
	public String toString(){
		String s = super.toString() + "{target=" + this.getTarget() + ", scope=" + this.getScope() + ", secure=" + this.getSecure() + "}";
		return s;
	}

	/**
	 * @return the hd
	 */
	public String getHd() {
		return hd;
	}

	/**
	 * @param hd the hd to set
	 */
	public void setHd(String hd) {
		this.hd = hd;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}