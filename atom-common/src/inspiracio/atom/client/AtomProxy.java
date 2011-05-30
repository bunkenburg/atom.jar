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
package inspiracio.atom.client;

import inspiracio.atom.AtomBean;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.PreconditionFailedException;

import java.util.List;

import atom.gdata.GDataParameters;

/** The client calls methods of an instance of this. */
public interface AtomProxy<T extends AtomBean>{
	
	/** Inserts many beans in the server.
	 * A good implementation of the proxy can do it in one request.
	 * A bad implementation can do it in many requests. 
	 * @param beans List of beans to insert.
	 * @return The same beans, as inserted by the server.
	 * @exception HttpException The usual ones: 401, 500, ... 
	 * */
	List<T> insert(List<T> beans)throws HttpException;
	
	/** Create a new bean on the server. 
	 * @exception HttpException Some error on the server. */
	T insert(T bean)throws HttpException;
	
	/** Delete a bean on the server.
	 * @param id Declared as Object so that String and Integer and Long can be passed
	 * 	easily. Will be converted to String by .toString().
	 * @param etag The ETag that the client sent.
	 * @exception PreconditionFailedException The ETag is not up to date. 
	 * 	That means the client has an old version of the 
	 * 	object. The client must see the fresh object before deleting.
	 * @exception HttpException Some server error.
	 * */
	void delete(Object id, String etag)throws PreconditionFailedException, HttpException;
	
	/** Update a bean on the server. 
	 * You cannot change the id.
	 * @param bean The bean to update.
	 * @exception PreconditionFailedException The ETag in the request HTTP header 
	 * 	If-Match is not up to date. That means the client has an old version of the 
	 * 	object. The client must see the fresh object before updating.
	 * @exception HttpException Some server error.
	 * */
	T update(T bean)throws PreconditionFailedException, HttpException;
	
	/** Gets a feed of beans from the server. 
	 * @param params
	 * @exception HttpException Some server error.
	 * */
	List<T> get(GDataParameters params)throws HttpException;

}