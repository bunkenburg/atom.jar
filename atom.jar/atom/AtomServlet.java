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
package atom;

import inspiracio.servlet.http.BadRequestException;
import inspiracio.servlet.http.ETag;
import inspiracio.servlet.http.ForbiddenException;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.IHttpServlet;
import inspiracio.servlet.http.NotAuthorizedException;
import inspiracio.servlet.http.NotFoundException;
import inspiracio.servlet.http.PreconditionFailedException;
import inspiracio.transaction.TransactionFactory;
import inspiracio.util.Base64Coder;
import inspiracio.util.Time;
import inspiracio.xml.DOM;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.naming.NamingException;
import javax.resource.spi.security.PasswordCredential;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import atom.gdata.GDataURL;

/** Base servlet for Atom syndication and publishing.
 * See specs http://tools.ietf.org/html/rfc4287 and http://tools.ietf.org/html/rfc5023.
 * <p>
 * This servlet only speaks about Atom, not about any particular
 * business logic. You must make a subclass and overwrite the abstract methods
 * with you particular business logic.
 * <p>
 * The subclass-servlet must be configured for all URLs with http://.../atom*.
 * Configuration:
 * in web.xml in the servlet-section:
 * <pre>
	&lt;servlet>
        &lt;servlet-name>AtomServlet&lt;/servlet-name>
        &lt;servlet-class>inspiracio.atom.IAtomServlet&lt;/servlet-class>
	&lt;/servlet>
 * </pre>
 * in the servlet-mapping-section:
 * <pre>
	&lt;servlet-mapping>
  		&lt;servlet-name>AtomServlet&lt;/servlet-name>
    	&lt;url-pattern>/atom/*&lt;/url-pattern>
	&lt;/servlet-mapping>
 * </pre>
 * */
public abstract class AtomServlet extends IHttpServlet{
	private static final Logger logger=Logger.getLogger(AtomServlet.class);

	/** Invalidates the session, and then services the request.
	 * Make sure that the client is not trying to keep a session on an atom
	 * request. For example, Java Web Start has a cookie manager which
	 * keeps track of session, but atom is REST and user shouldn't be kept
	 * by server.
	 */
	@Override protected void service(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		long initial=System.currentTimeMillis();
		logger.debug("Received a request " + request.getMethod() + " with content legnth of " + request.getContentLength() + " at " + initial);
		request.getSession().invalidate();
		logger.debug("Session invalidated " + Time.getLapseTimeMessage(initial) + " after receiving the request");
		super.service(request, response);
		logger.debug("Atom needed " + Time.getLapseTimeMessage(initial) + " to process the request " + request.getMethod() + " at " + initial);
	}

	/** To delete a Member Resource, a client sends a DELETE request to its
	 * Member URI, as specified in [RFC2616].  The deletion of a Media Link
	 * Entry SHOULD result in the deletion of the corresponding Media
	 * Resource.
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * */
	@Override protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			//Parse input
			GDataURL url=new GDataURL(request);
			//PasswordCredential user=this.getUser(request);
			String etag=request.getHeader("If-Match");
			if(etag==null){
				//Client has not sent If-Match: Bad request.
				throw new BadRequestException("Missing If-Match");
			}
			etag=ETag.parseStrong(etag);

			//delegate deleting
			this.delete(url, etag);

			response.setStatus(200);//Successful deletion replies "Ok".
		}
		catch(NotAuthorizedException nae){
			//Client has not sent credentials: challenge the client.
			String realm = nae.getRealm();
			response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
			response.sendError(401, "Not authorized");
			String msg = "Challenging Http-BASIC-authentication for " + realm + ".";
			abort(msg);
		}
		catch(ForbiddenException fe){
			String realm=fe.getRealm();
			String msg=null;
			if (realm != null) {
				//The client has sent wrong username & password: server forbids access.
				response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
				msg = "Http-BASIC-authentication for " + realm + " has failed.";
			} else {
				// Forbidden by some other reason
				msg = fe.getMessage();
			}
			response.sendError(403, msg);
		}
		catch (HttpException nae){
			//Some other exception.
			int status=nae.getStatus();
			String msg=nae.getMessage();
			response.sendError(status, msg);
			this.log(""+status, nae);
		}
	}

	/** Handle GET: parse the request, get the feed, write feed to response.
	 * <p>
	 * Do all of this within a transaction. Thereby, the delegate classes can
	 * open a DB connection, wrap it in an iterator that delivers entries, and
	 * put the iterator in the feed, in order to implement streaming.
	 *
	 * @param request
	 * @param response
	 * @exception IOException */
	@Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try{
			UserTransaction tx=null;
			boolean committed=false;
			try{
				//Serve an Atom feed.
				GDataURL url=new GDataURL(request);
				//PasswordCredential user=this.getUser(request);

				//Start transaction
				tx=TransactionFactory.getUserTransaction();//NamingException
				tx.begin();//NotSupportedException, SystemException

				Feed feed=this.get(url);//HttpException
				if(feed==null){
					response.sendError(404, "Not found");
					String msg = "Requested object was not found.";
					abort(msg);
					return;
				}

				response.setStatus(200);
				response.setContentType("application/atom+xml; charset=UTF-8");

				//Firefox 3.0.1/ubuntu does not want to display the Atom feed in the same window.
				//Here, I try to suggest a file name in the hope that firefox may deduce the type.
				//response.setHeader("Content-disposition", "inline; filename=atom.xml");

				OutputStream os=response.getOutputStream();
				feed.write(os);//TransformerException
				os.flush();
				os.close();

				//Commit
				tx.commit();//javax.transaction.RollbackException, javax.transaction.HeuristicMixedException, javax.transaction.HeuristicRollbackException, java.lang.SecurityException, java.lang.IllegalStateException, javax.transaction.SystemException;
				committed=true;
			}

			//Bugs or deployment errors: should never happen.
			catch (NamingException ne){
				//Couldn't get transaction.
				throw new ServletException(ne);
			}
			catch (NotSupportedException nse){
				//Couldn't get transaction.
				throw new ServletException(nse);
			}
			catch (SystemException se){
				//Couldn't get transaction.
				throw new ServletException(se);
			}
			catch (TransformerException te){
				throw new ServletException(te);
			}

			//Bug or problem in the tx manager: should never happen.
			catch (HeuristicRollbackException hre){
				//Tx already rolled back.
				throw new ServletException(hre);
			}
			catch (HeuristicMixedException hme){
				//Tx already mixed up. Partially committed, partially rolled back.
				throw new ServletException(hme);
			}
			catch (RollbackException re){
				//Tx already rolled back.
				throw new ServletException(re);
			}

			//Application exceptions: request is bad or SAO won't deliver.
			catch(BadRequestException bre){
				//The client has sent an invalid request.
				response.sendError(400, "Bad request");
			}
			catch(NotAuthorizedException nae){
				//Client has not sent credentials: challenge the client.
				String realm = nae.getRealm();
				response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
				response.sendError(401, "Not authorized");
			}
			catch (ForbiddenException fe){
				//The client has sent wrong username & password: server forbids access.
				response.sendError(403, fe.getMessage());
			}
			catch (NotFoundException nfe){
				//The client has sent wrong username & password: server forbids access.
				response.sendError(404);
			}
			catch (HttpException nae){
				//Some other exception.
				int status=nae.getStatus();
				response.setStatus(status);
				this.log(""+status, nae);
			}

			finally{
				if(!committed && tx!=null)
					tx.rollback();
			}
		}

		//Bugs or deployment errors
		catch(SystemException se){
			//Tx manager is broken. Bug.
			throw new ServletException(se);
		}
	}

	/** Process an insert or a batch.
	 * <h4>insert</h4>
	 * To add members to a Collection, clients send POST requests to the URI
	 * of the Collection.
	 * <p>
	 * Successful member creation is indicated with a 201 ("Created")
	 * response code.  When the Collection responds with a status code of
	 * 201, it SHOULD also return a response body, which MUST be an Atom
	 * Entry Document representing the newly created Resource.  Since the
	 * server is free to alter the POSTed Entry, for example, by changing
	 * the content of the atom:id element, returning the Entry can be useful
	 * to the client, enabling it to correlate the client and server views
	 * of the new Entry.
	 * </p>
	 * <p>
	 * When a Member Resource is created, its Member Entry URI MUST be
	 * returned in a Location header in the Collection's response.
	 * </p>
	 * <p>
	 * If the creation request contained an Atom Entry Document, and the
	 * subsequent response from the server contains a Content-Location
	 * header that matches the Location header character-for-character, then
	 * the client is authorised to interpret the response entity as being a
	 * complete representation of the newly created Entry.  Without a
	 * matching Content-Location header, the client MUST NOT assume the
	 * returned entity is a complete representation of the created Resource.
	 * </p>
	 * <p>
	 * The request body sent with the POST need not be an Atom Entry.  For
	 * example, it might be a picture or a movie.  Collections MAY return a
	 * response with a status code of 415 ("Unsupported Media Type") to
	 * indicate that the media type of the POSTed entity is not allowed or
	 * supported by the Collection.  For a discussion of the issues in
	 * creating such content, see Section 9.6.
	 * </p>
	 * <p>
	 * This method parses the request and delegates to method create(slug, entry).
	 *
	 * <h4>batch</h4>
	 * Batch procession is described in <a href="http://code.google.com/apis/gdata/docs/batch.html">http://code.google.com/apis/gdata/docs/batch.html</a>.
	 * A batch is a feed with entries. Either the batch or each entry says which operation it
	 * wants (default "insert"). The client sends the feed to the server in a POST.
	 * The server processes each operation and combines the resulting entries in a feed
	 * that is sent back to the client.
	 * <p>
	 * Partial implementation:
	 * <ul>
	 * 	<li>no explicit batch operation, always "insert"
	 * 	<li>no batch id
	 * 	<li>results without batch status
	 * </ul>
	 *
	 * @param request
	 * 	For insert, may have http header "Slug".
	 * 	For insert, body is XML of the entry.
	 * 	For batch, body is a feed.
	 * @param response
	 * 	For insert, sends 201 Created, Location header to the new entry,
	 * 	and XML of the entry in the body.
	 * 	For batch, sends 200 with a feed with resulting entries.
	 * @throws ServletException
	 * @throws IOException
	 * */
	@Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			InputStream in=request.getInputStream();

			//These two lines are for testing from html-pages that simulate an arbitrary body of
			//a POST request by sending one parameter with key "body".
			//If you don't need such testing, you can comment these two lines.
			//For large bodies, they are not very space-efficient.
			//byte[] prefix = "body=".getBytes("UTF-8");
			//in = new PrefixIgnorerInputStream(prefix, in);

			//Is is an INSERT or a BATCH?
			//Parse XML and see what the root element is.
			Element root=DOM.getDocumentRoot(in);//TransformerException
			in.close();
			boolean isInsert="entry".equals(root.getTagName());
			if(isInsert)
				this.doInsert(request, response, root);
			else
				this.doBatch(request, response, root);
		}
		catch(TransformerException te){
			throw new ServletException(te);
		}
	}

	/** To edit a Member Resource, a client sends a PUT request to its Member
	 * URI, as specified in [RFC2616].
	 * <p>
	 * To avoid unintentional loss of data when editing Member Entries or
	 * Media Link Entries, an Atom Protocol client SHOULD preserve all
	 * metadata that has not been intentionally modified, including unknown
	 * foreign markup as defined in Section 6 of [RFC4287].
	 *
	 * @param request Body is XML of the entry.
	 * @param response Sends 200 Ok and XML of the entry in the body.
	 * @throws ServletException
	 * @throws IOException
	 * */
	@Override protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			//PasswordCredential user=this.getUser(request);
			GDataURL url=new GDataURL(request);

			//Get If-Match
			String etag=request.getHeader("If-Match");
			if(etag!=null){
				//It is legal to send the request without ETag. Then the ETag must be in the bean itself.
				etag=ETag.parseStrong(etag);
			}

			//parse Entry
			long initial=System.currentTimeMillis();

			InputStream in=request.getInputStream();
			byte[] prefix="body=".getBytes("UTF-8");
			in=new PrefixIgnorerInputStream(prefix, in);//enables testing. Ok for PROD, but you may also eliminate for PROD.
			Entry entry=Entry.parse(in);

			if(etag!=null)
				entry.setETag(etag);//Http header If-Match has priority over gd:etag.
			
			in.close();

			//delegate to subclass
			entry=this.update(url, entry);

			//write response header and body
			response.setStatus(200);//Ok
			response.setContentType("application/atom+xml; charset=UTF-8");
			OutputStream out=response.getOutputStream();
			if(url.getParameters().getPrettyprint())
				entry.setPrettyprint(true);

			initial=System.currentTimeMillis();
			logger.debug("Beginning to write PUT response at " + initial);

			entry.write(out);//TransformerException
			out.flush();
			out.close();

			logger.debug("I needed " + Time.getLapseTimeMessage(initial) + " to write the response inititated at " + initial);
		}
		catch (NotAuthorizedException nae){
			//Client has not sent credentials: challenge the client.
			String realm = nae.getRealm();
			response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
			response.sendError(401, "Not authorized");
			String msg = "Challenging Http-BASIC-authentication for " + realm + ".";
			abort(msg);
		}
		catch(ForbiddenException fe){
			String realm = fe.getRealm();
			String msg = null;

			if(realm!=null) {
				//The client has sent wrong username & password: server forbids access.
				response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
				msg="Http-BASIC-authentication for " + realm + " has failed.";
			}else{
				// Forbidden by some other reason
				msg=fe.getMessage();
			}
			response.sendError(403, msg);
			abort(msg);
		}
		catch(HttpException nae){
			//Some other exception.
			int status=nae.getStatus();
			String msg=nae.getMessage();
			response.sendError(status, msg);
			this.log(status+"", nae);
		}
		catch (TransformerException te){
			throw new ServletException(te);
		}
	}

	/** POST of a single INSERT
	 * @param request
	 * @param response
	 * @param root Root element of the XML received in the request
	 * */
	private void doInsert(HttpServletRequest request, HttpServletResponse response, Element root)throws ServletException,IOException{
		try{
			//PasswordCredential user=this.getUser(request);
			GDataURL url=new GDataURL(request);
			Entry entry=Entry.parse(root);

			//parse Slug header. Value is URL-encoded with UTF-8.
			String slug=request.getHeader("Slug");//null if there is none

			//delegate to subclass
			entry=this.insert(url, slug, entry);

			//write response header and body
			response.setStatus(201);//Created
			response.setContentType("application/atom+xml; charset=UTF-8");
			String entryURI=entry.getURI();
			response.setHeader("Location", entryURI);
			OutputStream out=response.getOutputStream();//IOException
			if(url.getParameters().getPrettyprint())
				entry.setPrettyprint(true);
			entry.write(out);//TransformerException
			out.flush();//IOException
			out.close();//IOException
		}
		catch (NotAuthorizedException nae){
			//Client has not sent credentials: challenge the client.
			String realm=nae.getRealm();
			response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
			response.sendError(401, "Not authorized");//IOException
			String msg="Challenging Http-BASIC-authentication for " + realm + ".";
			abort(msg);
		}
		catch (ForbiddenException fe){
			String msg=fe.getMessage();
			response.sendError(403, msg);//IOException
			abort(msg);
		}
		catch (HttpException nae){//Some other exception.
			int status=nae.getStatus();
			String msg=nae.getLocalizedMessage();
			response.sendError(status, msg);//IOException
			this.log(status+"", nae);//If an error-page declaration has been made for the web application corresponding to the status code passed in, it will be served back in preference to the suggested msg parameter.
		}
		catch (TransformerException te){//Writing result XML has failed.
			throw new ServletException(te);
		}
	}


	/** Process a BATCH request, in a transaction.
	 *
	 * @param request
	 * @param response
	 * @param root The root element of the XM received in the request. It's a feed.
	 * */
	private void doBatch(HttpServletRequest request, HttpServletResponse response,Element root)throws ServletException,IOException{
		try{
			//PasswordCredential user=this.getUser(request);
			GDataURL url=new GDataURL(request);

			Feed feed=Feed.parse(root);
			root=null;//help gc
			List<Entry> entries=feed.getEntries();
			feed=null;//help gc

			//delegate to subclass
			this.insert(url, entries);
			entries=null;//help gc

			//write response header and body
			response.setStatus(200);//Created
			response.setContentType("application/atom+xml; charset=UTF-8");

			// return empty feed (Is this correct for Atom batches?)
			OutputStream out=response.getOutputStream();//IOException
			Feed f=new Feed();
			f.write(out);
			out.flush();//IOException
			out.close();//IOException
		}
		catch (NotAuthorizedException nae){
			//Client has not sent credentials: challenge the client.
			String realm = nae.getRealm();
			response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
			response.sendError(401, "Not authorized");//IOException
			String msg = "Challenging Http-BASIC-authentication for " + realm + ".";
			abort(msg);
		}
		catch (ForbiddenException fe){
			String msg = fe.getMessage();
			response.sendError(403, msg);//IOException
			abort(msg);
		}
		catch (HttpException nae){//Some other exception.
			int status=nae.getStatus();
			String msg = nae.getLocalizedMessage();
			response.sendError(status, msg);//IOException
			this.log(status+"", nae);//If an error-page declaration has been made for the web application corresponding to the status code passed in, it will be served back in preference to the suggested msg parameter.
		}
		catch (Exception e) {
			int status=500;
			String message = e.getLocalizedMessage();
			response.sendError(500, message);
			this.log(status+"", e);//If an error-page declaration has been made for the web application corresponding to the status code passed in, it will be served back in preference to the suggested message parameter.
		}
	}

	/** Process a BATCH request, in a transaction.
	 * <p>
	 * The implementation here gets the transaction from JNDI. An alternative implementation could
	 * delegate to a stateless session bean. In either case, there's a lot of work to get the exceptions
	 * and http response status code and messages right.
	 *
	 * @param request
	 * @param response
	 * @param root The root element of the XM received in the request. It's a feed.
	 *
	private void doOldBatch(HttpServletRequest request, HttpServletResponse response,Element root)throws ServletException,IOException{
		try{//tx stuff
			UserTransaction tx=null;
			boolean committed=false;
			try{
				PasswordCredential user=this.getUser(request);
				GDataURL url=new GDataURL(request);

				Feed feed=Feed.parse(root);
				Entry[] entries=feed.getEntries().toArray(new Entry[]{});

				//Process the entries in the array.

				//Start transaction
				tx=TransactionFactory.getUserTransaction();//NamingException
				tx.begin();//NotSupportedException, SystemException

				for(int i=0; i<entries.length; i++){
					Entry e=entries[i];
					Entry result=null;
					//For now, assume batch operation is always "insert".
					//batchOperation = entry.getBatchOperation();
					//batchId=entry.getBatchId();
					//switch(batchOperation){
					//case INSERT:
					String slug=null;//No slug in a batch.
					result=this.insert(url, slug, e, user);
					//result will have superfluous XML namespaces. Superfluous because it's not root element.
					//result.setBatchStatus(201, "Created");
					//result.setBatchId(batchId);
					//break;
					//case UPDATE: ... break;
					//case QUERY: ... break;
					//case DELETE: ... break;
					//}
					entries[i]=result;//in-place processing of the array
				}

				//Make the result feed
				Feed resultFeed=new Feed();
				//... result feed attributes and minor elements ...
				resultFeed.addEntry(entries);//also updates the namespaces

				//Send the result feed ...
				response.setStatus(200);//Ok
				response.setContentType("application/atom+xml; charset=UTF-8");
				OutputStream out = response.getOutputStream();
				if(url.getParameters().getPrettyprint())
					feed.setPrettyprint(true);
				resultFeed.write(out);//TransformerException
				out.flush();
				out.close();

				//Commit
				tx.commit();//javax.transaction.RollbackException, javax.transaction.HeuristicMixedException, javax.transaction.HeuristicRollbackException, java.lang.SecurityException, java.lang.IllegalStateException, javax.transaction.SystemException;
				committed=true;
			}

			//Bugs or deployment errors
			catch (TransformerException te){
				throw new ServletException(te);//Writing result XML has failed.
			}
			catch (NamingException ne){
				throw new ServletException(ne);//Getting tx has failed.
			}
			catch(NotSupportedException nse){
				throw new ServletException(nse);//Creating new transaction when there's already a tx. Bug.
			}

			//Transaction manager exception: database failure or bug.
			catch (HeuristicMixedException hme){
				//A heuristic decision was made and some relevant updates have been committed while others have been rolled back.
				this.log("heuristic mixed exception", hme);
				throw new ServletException(hme);
			}
			catch (HeuristicRollbackException hre){
				//Thrown to indicate that a heuristic decision was made and that all relevant updates have been rolled back.
				this.log("heuristic rollback exception", hre);
				throw new ServletException(hre);
			}
			catch (RollbackException re){
				//Thrown to indicate that the transaction has been rolled back rather than committed.
				this.log("rollback exception", re);
				throw new ServletException(re);
			}

			//Application exceptions: the SAOs refuse or fail to do something.
			catch (NotAuthorizedException nae){
				//Client has not sent credentials: challenge the client.
				String realm = nae.getRealm();
				response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
				response.sendError(401, "Not authorized");
				String msg = "Challenging Http-BASIC-authentication for " + realm + ".";
				abort(msg);
			}
			catch (ForbiddenException fe){
				String msg = fe.getMessage();
				response.sendError(403, msg);
				abort(msg);
			}
			catch (HttpException nae){//Some other http exception.
				int status=nae.getStatus();
				String message = nae.getLocalizedMessage();
				response.sendError(status, message);
				this.log(status+"", nae);//If an error-page declaration has been made for the web application corresponding to the status code passed in, it will be served back in preference to the suggested message parameter.
			}

			//Any other exception: some bug or unexpected problem.
			catch (Exception e){
				int status=500;
				String message = e.getLocalizedMessage();
				response.sendError(500, message);
				this.log(status+"", e);//If an error-page declaration has been made for the web application corresponding to the status code passed in, it will be served back in preference to the suggested message parameter.
			}

			finally{
				if(!committed && tx!=null)
					tx.rollback();
			}
		}

		//Bugs or deployment errors
		catch(SystemException se){
			//Tx manager is broken. Bug.
			throw new ServletException(se);
		}
	}
	*/

	//The abstract methods that the subclass must implement. --------------------

	/** Create an entry.
	 * @param slug The client wants the URL of the new entry to contain something
	 * 	similar to this String. The server may use the slug, or ignore it.
	 * @param entry The new entry that the client wants to publish. The server
	 * 	will store the new entry. The server may alter some of the fields of the
	 * 	entry. Usually the server will alter the ID of the entry.
	 * @return The entry as stored. The server may have altered some of the fields.
	 * @throws HttpException For example, NotAuthorizedException.
	 * */
	protected abstract Entry insert(GDataURL url, String slug, Entry entry) throws HttpException;

	/** Create multiple entries
	 * @param url
	 * @param entries
	 * @throws HttpException
	 * */
	protected abstract void insert(GDataURL url, List<Entry> entries) throws HttpException;

	/** Updates an entry.
	 * @param entry The entry that the client wants to update. The implementation must check that etag is current.
	 * @return The entry as stored. The server may have altered some of the fields.
	 * @throws PreconditionFailedException if entry.getETag() is not current.
	 * @throws HttpException
	 * */
	protected abstract Entry update(GDataURL url, Entry entry) throws HttpException;

	/** Deletes an entry.
	 * @param url
	 * @param etag ETag of the object that the client wants to delete.
	 * @throws PreconditionFailedException if etag is not current.
	 * @throws HttpException
	 * */
	protected abstract void delete(GDataURL url, String etag) throws HttpException;

	/** Generates the feed that should be served as response to
	 * a request to the given URL.
	 * @param url The URL of the request.
	 * @return The feed that should served.
	 * @throws HttpException
	 * */
	protected abstract Feed get(GDataURL url) throws HttpException;

	/** Call this to signal that there should be no further request processing.
	 * This implementation throws RuntimeException. A subclass may override with
	 * some exception that prevents further request processing but doesn't show
	 * up in the some error management.
	 * @param message An explanation of why there should be no further request
	 * processing. */
	protected void abort(String message){
		throw new RuntimeException(message);
	}

	//Private helpers -----------------------------------------------------------

	/** If the client sent http basic authentication with the request, the username
	 * and password, else null.
	 * @param request
	 * @return username and password or null
	 * */
	@SuppressWarnings("unused")
	private PasswordCredential getUser(HttpServletRequest request){
		PasswordCredential user=null;
		//Maybe client is sending the authorisation?
		String authorization=request.getHeader("Authorization");
		if(authorization!=null && authorization.startsWith("Basic ")){
			//Client has sent credentials: check them.
			//check user & password
			String credentials=authorization.substring(6);//cut off "Basic "
			String userPassword=Base64Coder.decode(credentials);
			String[] ss=userPassword.split(":");
			String name=ss[0];
			String password=ss[1];
			user=new PasswordCredential(name, password.toCharArray());
		}else{
			user=null;
		}
		return user;
	}

}