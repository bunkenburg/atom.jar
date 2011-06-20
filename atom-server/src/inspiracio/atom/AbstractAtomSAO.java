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

import inspiracio.lang.NotImplementedException;
import inspiracio.servlet.http.BadRequestException;
import inspiracio.servlet.http.ForbiddenException;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.InternalServerErrorException;
import inspiracio.servlet.http.MethodNotAllowedException;
import inspiracio.servlet.http.NotAuthorizedException;

import java.security.Principal;
import java.util.List;

import atom.Entry;
import atom.Feed;
import atom.gdata.GDataURL;
import atom.gdata.Style;

/** For convenience, a super-class for AtomSAO implementations that provides
 * dummy implementations. Partial AtomSAO implementations can extend this class
 * and only implement some methods.
 *
 * @author BARCELONA\alexanderb
 *
 */
public abstract class AbstractAtomSAO<T extends AtomBean> implements AtomSAO<T>{
	
	//State ------------------------------------------------------------------
	
	/** The authenticated principal for the operations */
	private Principal principal;

	//Constructors -----------------------------------------------------------
	
	protected AbstractAtomSAO(){}
	
	//The ReST operations ----------------------------------------------------

	/** Create a new bean.
	 * @param url The URL that has received the insertion request. The implementation
	 * 	may ignore it, or may use some parameters from the URL.
	 * @param bean The bean to be inserted.
	 * @param slug The client wants something similar to this String appear in the id.
	 * @return The entry as inserted. Usually, the id will have been changed.
	 * @exception RuntimeException always. Must override if you want creation.
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * */
	public T insert(GDataURL url, T bean, String slug) throws NotAuthorizedException, ForbiddenException, HttpException{
		throw new MethodNotAllowedException();
	}

	public void insert(GDataURL url, List<T> beans)throws HttpException, BadRequestException, NotAuthorizedException, ForbiddenException {
		throw new MethodNotAllowedException();
	}

	/** Edit a bean.
	 * @param bean The bean as edited by the client. The id obviously cannot be changed.
	 * @exception RuntimeException always. Must override if you want editing.
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * */
	public T update(T bean) throws NotAuthorizedException, ForbiddenException,HttpException{
		throw new MethodNotAllowedException();
	}

	/** Gets some beans.
	 * <p>
	 * If you don't want to set fields in the feed that is sent to the client,
	 * override this method and don't override getFeed.
	 * <p>
	 * If you do want to set fields in the feed, override only getFeed and make it do
	 * everything, or override both, in getFeed first calling super.getFeed.
	 * 
	 * @param url
	 * @return Some beans from the store that match the parameters
	 * 
	 * @exception RuntimeException always. Must override if you want retrieval.
	 * 
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * @throws InternalServerErrorException The SAO signals that something has gone
	 * 	wrong in processing the request and that the servlet should reply 500 Internal
	 * 	Server Error to the client.
	 * */
	public List<T> get(GDataURL url)throws NotAuthorizedException, ForbiddenException, InternalServerErrorException,HttpException{
		throw new MethodNotAllowedException();
	}

	/** Gets a feed.
	 * <p>
	 * See get() for which method to override.
	 * <p>
	 * This implementation calls get() to get the beans, then constructs a boring feed
	 * and returns it. So if you needn't set fields in the feed, you only override get().
	 * 
	 * @param url
	 * @return Some beans from the store that match the parameters
	 * 
	 * @exception MethodNotAllowedException. Must override get() or getFeed() if you want retrieval.
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * @throws InternalServerErrorException The SAO signals that something has gone
	 * 	wrong in processing the request and that the servlet should reply 500 Internal
	 * 	Server Error to the client.
	 * */
	public Feed getFeed(GDataURL url) throws NotAuthorizedException, ForbiddenException, InternalServerErrorException,HttpException{
		Style style=Style.parseStyle(url.getParameter("style"));
		Feed feed=new Feed();
		//Set standard fields in the feed.
		
		List<T>beans=this.get(url);//MethodNotAllowedException
		for(T bean : beans){
			try{
				Entry entry=bean.toEntry(true, style);//We want the bean with XML namespaces.
				feed.addEntry(entry);
			}catch(Exception e){
				throw new InternalServerErrorException(e);
			}
		}
		
		return feed;
	}

	/** Remove a bean from the store.
	 * @param id The id of the bean to be removed.
	 * @param etag Must be current, else throw PreconditionFailedException
	 * @exception RuntimeException always. Must override if you want deletion.
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided user name and password.
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the user name and password provided by the client could not be validated by the
	 * 	SAO.
	 * */
	public void delete(String id, String etag) throws NotAuthorizedException, ForbiddenException,HttpException{
		throw new MethodNotAllowedException();
	}

	//Translation between entries and beans -----------------------------------

	/** Translates an entry to the bean.
	 * You must implement this method if you implement the methods POST or PUT.
	 * @param entry
	 * @return The parsed bean
	 * @exception RuntimeException always. Must override if you want entry parsing.
	 * 	having an implementation of toAtomBean that throw exception here is useful
	 * 	for subclasses that only implement Atom syndication, not Atom publishing.
	 * */
	public T toAtomBean(Entry entry){
		throw new NotImplementedException("Not implemented by " + this.getClass());
	}

	// Authentication -------------------------------------------------------

	/** The authenticated user, as Principal, or null if the user is not authenticated. */
	@Override public Principal getCallerPrincipal(){
		//Principal principal=PrincipalFactory.getCallerPrincipal();//Gets it from the request?
		return this.principal;
	}
	
	@Override public void setCallerPrincipal(Principal principal){
		this.principal=principal;
	}
}