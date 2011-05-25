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
package inspiracio.atom;

import inspiracio.servlet.http.BadRequestException;
import inspiracio.servlet.http.ForbiddenException;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.InternalServerErrorException;
import inspiracio.servlet.http.NotAuthorizedException;
import inspiracio.servlet.http.PreconditionFailedException;

import java.util.List;


import atom.Entry;
import atom.Feed;
import atom.gdata.GDataURL;

/** A SAO that we want to use for Atom must implement this interface.
 * It specifies the ReST operations and translation between beans and entries.
 *
 * @param T The business object managed by this AtomSAO. A subclass of AtomBean.
 *
 * @author BARCELONA\alexanderb
 */
public interface AtomSAO<T extends AtomBean>{

	//The ReST operations ----------------------------------------------------

	/** Create a new bean.
	 * @param url The URL that has received the insertion request.
	 * @param bean The bean to be inserted.
	 * @param slug The client wants something similar to this String appear in the id.
	 * @return The entry as inserted. Usually, the id will have been changed.
	 * @throws BadRequestException Some invalid parameter has been passed
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * @throws PreconditionFailedException ETag is not current.
	 * @exception HttpException Look at status (=subclass) and message for more information.
	 * */
	T insert(GDataURL url, T bean, String slug)throws HttpException, BadRequestException, NotAuthorizedException, ForbiddenException;

	/** Create many new beans, in a transaction. */
	void insert(GDataURL url, List<T> beans)throws HttpException, BadRequestException, NotAuthorizedException, ForbiddenException;

	/** Edit a bean.
	 * @param bean The bean as edited by the client. The id obviously cannot be changed.
	 * @throws BadRequestException Some invalid parameter has been passed
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * @exception PreconditionFailedException Client has not sent up-to-date ETag.
	 * @exception HttpException Look at status (=subclass) and message for more information.
	 * */
	T update(T bean)throws PreconditionFailedException, HttpException;

	/** Get a feed containing some beans.
	 * <p>
	 * This method returns a feed, and not just a collection of beans or entries,
	 * because the SAO may want to set the fields of the feed too, and may want to add some
	 * extension elements to the feed. SAOs that are not interested in setting fields in the
	 * feed can extend AbstractAtomSAO, and overwrite just get, and not getFeed.
	 * </p>
	 * @param url The URL of the request, in a form that parses the Atom parameters easily.
	 * @return The feed that should be sent to the client.
	 * @throws BadRequestException Some invalid parameter has been passed
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * @throws InternalServerErrorException The SAO signals that something has gone
	 * 	wrong in processing the request and that the servlet should reply 500 Internal
	 * 	Server Error to the client.
	 * */
	Feed getFeed(GDataURL url)throws HttpException, InternalServerErrorException;

	/** Remove a bean from the store.
	 * @param id The id of the bean to be removed.
	 * @param etag Must be current, else PreconditionFailedException
	 * @throws BadRequestException Some invalid parameter has been passed
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * @throws PreconditionFailedException Client has not sent current etag.
	 * */
	void delete(String id, String etag) throws PreconditionFailedException, HttpException;

	//Translation between entries and beans -----------------------------------

	/** Translates an entry to the bean.
	 * @param entry
	 * @return The parsed bean */
	T toAtomBean(Entry entry);

}