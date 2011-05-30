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

/** A method called within an http request signals that the response to the
 * request should be 403 Forbidden. The request may be forbidden because the
 * client has sent unacceptable authentication, or because the client has sent
 * an unacceptable parameter, or some other reason.
 * @author BARCELONA\alexanderb
 *
 */
public class ForbiddenException extends HttpException {

	//State -----------------------------------------------

	private String realm;

	//Constructors ---------------------------------------

	public ForbiddenException(){
		super(403);
	}

	public ForbiddenException(String message){
		super(403, message);
	}

	public ForbiddenException(String message, String realm){
		super(403, message);
		this.realm = realm;
	}

	public ForbiddenException(String message, Throwable cause) {
		super(message, cause);
		this.setStatus(403);
	}

	//Accessors ------------------------------------------


	public String getRealm(){return this.realm;}
	public void setRealm(String realm){this.realm=realm;}
}