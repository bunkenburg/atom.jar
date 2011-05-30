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
 * should be 500 Internal Server Error.
 * @author BARCELONA\alexanderb
 *
 */
public class InternalServerErrorException extends HttpException {

	/** Construct a new InternalServerErrorException. */
	public InternalServerErrorException(){
		super(500);
	}

	/** Construct a new InternalServerErrorException. */
	public InternalServerErrorException(String msg){
		super(500,msg);
	}

	/** Construct a new InternalServerErrorException, wrapping
	 * the cause.
	 * @param cause The underlying exception
	 *  */
	public InternalServerErrorException(Throwable cause){
		super(cause);
		this.setStatus(500);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InternalServerErrorException(String message, Throwable cause) {
		super(message, cause);
		this.setStatus(500);
	}
}