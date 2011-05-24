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

/** A method called from within an http request signals that the
 * response should be an error response, with status code like
 * 4** or 5**:
 * @author BARCELONA\alexanderb
 */
public class HttpException extends Exception {

	//State -------------------------------------------

	/** default status is 500 */
	private int status=500;

	//Constructors ------------------------------------

	/** Construct a new HttpException. */
	protected HttpException(){}

	/** Construct a new HttpException. */
	protected HttpException(int status){
		this.status=status;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

	/** @param message */
	public HttpException(String message) {
		super(message);
	}

	/** Construct a new HttpException. */
	protected HttpException(int status, String msg){
		super(msg);
		this.status = status;
	}

	/** Construct a new HttpException, wrapping
	 * the cause.
	 * @param cause The underlying exception
	 *  */
	public HttpException(Throwable cause){
		super(cause);
	}

	/** Make an HttpException with the correct status. */
	public static HttpException getInstance(int status, String msg){
		//If you make more subclasses, add them here.
		switch(status){
		case 400: return new BadRequestException(msg);
		case 401: return new NotAuthorizedException(msg);
		case 403: return new ForbiddenException(msg);
		case 404: return new NotFoundException(msg);
		case 405: return new MethodNotAllowedException(msg);
		case 412: return new PreconditionFailedException(msg);
		case 500: return new InternalServerErrorException(msg);
		default: return new HttpException(status, msg);
		}
	}

	//Accessors --------------------------------------

	public int getStatus(){return this.status;}
	public void setStatus(int status){this.status=status;}
}