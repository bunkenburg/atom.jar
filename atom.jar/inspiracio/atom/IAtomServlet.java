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

import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.InternalServerErrorException;
import inspiracio.servlet.http.PreconditionFailedException;

import java.util.ArrayList;
import java.util.List;

import javax.resource.spi.security.PasswordCredential;


import org.apache.log4j.Logger;

import atom.AtomServlet;
import atom.Entry;
import atom.Feed;
import atom.gdata.GDataURL;
import atom.gdata.Style;

/** An Atom-servlet that follows our policy of having exactly one
 * category in every request identifying the class of business object,
 * and then delegating the request to the relevant AtomSAO.
 * */
public class IAtomServlet extends AtomServlet {
	private static final Logger logger = Logger.getLogger(IAtomServlet.class);

	/** Create an entry.
	 * @param slug The client wants the URL of the new entry to contain something
	 * 	similar to this String. The server may use the slug, or ignore it.
	 * @param entry The new entry that the client wants to publish. The server
	 * 	will store the new entry. The server may alter some of the fields of the
	 * 	entry. Usually the server will alter the ID of the entry.
	 * @param user If the client is sending http basic authentication, username and
	 * 	password, else null. The server may throw an HttpException to signal that it
	 * 	is not satisfied with the authentication
	 * @return The entry as stored. The server may have altered some of the fields.
	 * @throws HttpException For example, NotAuthorizedException.
	 * */
	@Override protected Entry insert(GDataURL url, String slug, Entry entry, PasswordCredential user) throws HttpException {
		AtomSAO<AtomBean> sao=AtomSAOFactory.get(url);
		AtomBean bean=sao.toAtomBean(entry);
		bean=sao.insert(url, bean, slug, user);
		try{
			Style style=url.getParameters().getStyle();
			entry=bean.toEntry(true, style);//Exception
			return entry;
		}catch(HttpException he){
			throw he;
		}catch(Exception e){
			throw new InternalServerErrorException(e);
		}
	}

	/** Create multiple entries.
	 * @param entries The entries that the client wants to publish. The server
	 * 	will store the new entries. The server may alter some of the fields of the
	 * 	entries. Usually the server will alter the ID of the entries.
	 * @param user If the client is sending http basic authentication, username and
	 * 	password, else null. The server may throw an HttpException to signal that it
	 * 	is not satisfied with the authentication
	 * @throws HttpException For example, NotAuthorizedException.
	 * */
	@Override protected void insert(GDataURL url, List<Entry> entries, PasswordCredential user) throws HttpException {
		logger.info("Preprocessing a multiple insert of " + entries.size() + " entries");

		AtomSAO<AtomBean> sao=AtomSAOFactory.get(url);
		List<AtomBean> beans=new ArrayList<AtomBean>(entries.size());
		int iterationCounter=0;
		for(Entry entry : entries){
			AtomBean bean=sao.toAtomBean(entry);
			beans.add(bean);

			if(iterationCounter % 1000 == 0)
				logger.info(iterationCounter + " entries preprocessed so far");
			iterationCounter++;
		}
		entries=null;//help gc
		logger.info(iterationCounter - 1 + " entries preprocessed");
		sao.insert(url, beans, user);
	}

	/** Deletes an entry.
	 * @param url
	 * @param etag SAO must throw PreconditionFailedException if etag is not current.
	 * @param user If the client has authenticated by http basic authentication, the
	 * 	username and password, else null. The server may signal it is not satisfied
	 * 	with the authentication by throwing NotAuthorizedException or ForbiddenException,
	 * both identifying realm.
	 * @throws PreconditionFailedException ETag is not current.
	 * @throws HttpException
	 * */
	@Override protected void delete(GDataURL url, String etag, PasswordCredential user) throws HttpException {
		AtomSAO<AtomBean> sao=AtomSAOFactory.get(url);
		String id=url.getParameters().getId();
		sao.delete(id, etag, user);
	}

	/** Generates the feed that should be served as response to
	 * a request to the given URL.
	 * @param url The URL of the request.
	 * @param user If the client has authenticated by http basic authentication, the
	 * 	user name and password, else null. The server may signal it is not satisfied
	 * 	with the authentication by throwing NotAuthorizedException or ForbiddenException.
	 * @return The feed that should served.
	 * @throws HttpException
	 * */
	@Override protected Feed get(GDataURL url, PasswordCredential user) throws HttpException {
		AtomSAO<AtomBean> sao=AtomSAOFactory.get(url);
		Feed feed=sao.get(url, user);
		if(url.getParameters().getPrettyprint())
			feed.setPrettyprint(true);
		return feed;
	}

	/** Updates an entry.
	 * @param entry The entry that the client wants to update.
	 * @param user If the client has authenticated by http basic authentication, the
	 * 	username and password, else null. The server may signal it is not satisfied
	 * 	with the authentication by throwing NotAuthorizedException or ForbiddenException.
	 * @return The entry as stored. The server may have altered some of the fields.
	 * @throws PreconditionFailedException ETag is not current.
	 * @throws HttpException
	 * */
	@Override protected Entry update(GDataURL url, Entry entry, PasswordCredential user) throws HttpException {
		AtomSAO<AtomBean> sao=AtomSAOFactory.get(url);
		Style style=url.getParameters().getStyle();
		AtomBean bean=sao.toAtomBean(entry);
		bean=sao.update(bean, user);
		try{
			entry=bean.toEntry(true,style);//Exception
			return entry;
		}catch(Exception e){
			throw new InternalServerErrorException(e);
		}
	}

}