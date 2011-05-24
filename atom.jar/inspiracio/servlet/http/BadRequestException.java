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
 * request should be 400 Bad request. The request can not be conducted because
 * of a format error or insufficient parameters.
 * @author BARCELONA\alexanderb
 *
 */
public class BadRequestException extends HttpException {

	/** Constructs a BadRequestException.
	 * */
	public BadRequestException(){
		super(400);
	}
	
	/** Constructs a BadRequestException with message.
	 * */
	public BadRequestException(String msg){
		super(400, msg);
	}
	
}