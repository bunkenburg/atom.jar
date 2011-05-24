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

/** A method called within an http request signals that the response
 * should be 401 Not Authorized.
 * @author BARCELONA\alexanderb
 *
 */
public class NotAuthorizedException extends HttpException {
	
	/** The realm that the client should authenticate for. */
	private String realm;
	
	/** Constructs a NotAuthorizedException not identifying the realm.
	 * */
	public NotAuthorizedException(){
		super(401);
		this.realm = "";
	}
	
	/** Constructs a NotAuthorizedException with a message.
	 * @param message 
	 * */
	public NotAuthorizedException(String message){
		super(401, message);
	}

	//Accessors ------------------------------------------
	
	/** Gets the realm that the client should authenticate for.
	 * @return realm */
	public String getRealm(){return this.realm;}
	
	public void setRealm(String realm){this.realm=realm;}
}