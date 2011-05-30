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
import inspiracio.servlet.jsp.PageContextFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

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
public class IAtomServlet extends AtomServlet{
	private static final Logger logger=Logger.getLogger(IAtomServlet.class);
	
	/** Wraps super.service(), just for getting init-parameter beanPackage and
	 * passing it to AtomSAOFactory. 
	 * (Maybe can do this is a better way?)*/
	@Override protected void service(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
		String beanPackage=this.getInitParameter("beanPackage");
		AtomSAOFactory.setBeanPackage(beanPackage);
		
		super.service(request, response);
	}

	/** Create an entry.
	 * @param slug The client wants the URL of the new entry to contain something
	 * 	similar to this String. The server may use the slug, or ignore it.
	 * @param entry The new entry that the client wants to publish. The server
	 * 	will store the new entry. The server may alter some of the fields of the
	 * 	entry. Usually the server will alter the ID of the entry.
	 * @return The entry as stored. The server may have altered some of the fields.
	 * @throws HttpException For example, NotAuthorizedException.
	 * */
	@Override protected Entry insert(GDataURL url, String slug, Entry entry) throws HttpException{
		
		//Just testing whether we can get the user
		PageContext pc=PageContextFactory.getPageContext();
		HttpServletRequest request=(HttpServletRequest)pc.getRequest();
		@SuppressWarnings("unused")
		Principal user=request.getUserPrincipal();
		
		AtomSAO<AtomBean> sao=AtomSAOFactory.get(url);
		AtomBean bean=sao.toAtomBean(entry);
		bean=sao.insert(url, bean, slug);
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
	 * @throws HttpException For example, NotAuthorizedException.
	 * */
	@Override protected void insert(GDataURL url, List<Entry> entries) throws HttpException {
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
		
		sao.insert(url, beans);
	}

	/** Deletes an entry.
	 * @param url
	 * @param etag SAO must throw PreconditionFailedException if etag is not current.
	 * @throws PreconditionFailedException ETag is not current.
	 * @throws HttpException
	 * */
	@Override protected void delete(GDataURL url, String etag) throws HttpException {
		AtomSAO<AtomBean> sao=AtomSAOFactory.get(url);
		String id=url.getParameters().getId();
		sao.delete(id, etag);
	}

	/** Generates the feed that should be served as response to
	 * a request to the given URL.
	 * @param url The URL of the request.
	 * @return The feed that should served.
	 * @throws HttpException
	 * */
	@Override protected Feed get(GDataURL url) throws HttpException {
		AtomSAO<AtomBean> sao=AtomSAOFactory.get(url);
		Feed feed=sao.getFeed(url);
		if(url.getParameters().getPrettyprint())
			feed.setPrettyprint(true);
		return feed;
	}

	/** Updates an entry.
	 * @param entry The entry that the client wants to update.
	 * @return The entry as stored. The server may have altered some of the fields.
	 * @throws PreconditionFailedException ETag is not current.
	 * @throws HttpException
	 * */
	@Override protected Entry update(GDataURL url, Entry entry) throws HttpException {
		AtomSAO<AtomBean> sao=AtomSAOFactory.get(url);
		Style style=url.getParameters().getStyle();
		AtomBean bean=sao.toAtomBean(entry);
		bean=sao.update(bean);
		try{
			entry=bean.toEntry(true,style);//Exception
			return entry;
		}catch(Exception e){
			throw new InternalServerErrorException(e);
		}
	}

	//Helpers ----------------------------------------------------------
	
}