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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;


/** Our wrapper around HttpServletResponse, in order to modify some
 * of its behaviour.
 * <p>
 * We modify the method encodeURL and encodeRedirectURL.
 * */
public class IHttpServletResponse extends HttpServletResponseWrapper {
	private static final Logger logger=Logger.getLogger(IHttpServletRequest.class);
	
	private static final String CONTENT_ENCODING="Content-Encoding";
	private static final String GZIP="gzip";

	static{
		//On class loading, set system property for a workaround in Tomcat that allows
		//arbitrary http messages.
		//Tomcat documentation: http://tomcat.apache.org/security-6.html See "low: Cross-site scripting   CVE-2008-1232".
		System.setProperty("org.apache.coyote.USE_CUSTOM_STATUS_MSG_IN_HEADER", "true");
	}

	//State ------------------------------------------------------------

	/** The status of the HTTP response set by {@link #setStatus(int)} or {@link #setStatus(int, String)} */
	private int status=HttpServletResponse.SC_OK;
	
	/** The client wants a gzipped response. */
	private boolean zipResponse=false;

	//Constructors ----------------------------------------------------

	/** Wrap a given response.
	 * inspiracio.servlet.jsp.HTTPPageContext calls this.
	 * @param response the response that will go in the wrapper.
	 * */
	public IHttpServletResponse(javax.servlet.http.HttpServletResponse response){
		super(response);
	}

	/** Wrap a given response.
	 * inspiracio.servlet.jsp.HTTPPageContext calls this.
	 * @param response the response that will go in the wrapper.
	 * @param zipResponse The client wants a gzipped response.
	 * */
	public IHttpServletResponse(javax.servlet.http.HttpServletResponse response, boolean zipResponse){
		this(response);
		this.zipResponse=zipResponse;
		if(zipResponse)
			this.setHeader(CONTENT_ENCODING, GZIP);
	}

	//Accessors --------------------------------------------------------

	/** Gets a stream to send the response to. */
	@Override public ServletOutputStream getOutputStream()throws IOException{
		ServletOutputStream out=super.getOutputStream();
		if(zipResponse){
			OutputStream o=new GZIPOutputStream(out);
			out=new ServletOutputStreamWrapper(o);
		}
		return out;
	}
	
	public int getStatus(){return status;}

	/** Saves the given status, filters non-Latin-1 chars from the
	 * message, and calls super.
	 * @param status
	 * @param message
	 */
	@Override public void setStatus(int status, String message){
		message=clean(message);
		super.setStatus(status, message);
		this.status=status;
	}

	/** Saves the given status and calls super
	 * @param status The new status.
	 */
	@Override public void setStatus(int status) {
		super.setStatus(status);
		this.status=status;
	}

	//Business methods -------------------------------------------------

	/** Adds a random parameter to the URL so that all
	 * URLs are different. We do this to prevent bad
	 * proxies from caching pages they shouldn't cache.
	 * <p>
	 * All URLs sent to the IHttpServletResponse.sendRedirect method should
	 * be run through this method. Otherwise, URL rewriting cannot be used
	 * with browsers which do not support cookies.
	 * @param url the url to be encoded.
   * @return URL with added random parameter
   * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(String)
	*/
	@Override public String encodeRedirectURL(String url){
		//no need to distinguish redirects and normal requests
		return this.encodeURL(url);
	}

	/** Must override this one too so that it can be called safely
	 * from ConFusion, which is not case-sensitive.
	 * @param url the URL to be encoded
	 * @return URL with added random parameter */
	@Override public String encodeRedirectUrl(String url){
		return this.encodeURL(url);
	}

	/** Adds a random parameter to the URL so that all
	 * URLs are different. We do this to prevent bad
	 * proxies from caching pages they shouldn't cache.
	 * <p>
	 * If the URL contains ".cfm", adds a random parameter.
	 * The parameter name is "dgv" and the value is
	 * System.currentTimeMillis(), which is in URL-encoded format.
	 * <p>
	 * Possible optimization: generate only one random value,
	 * and add it to all URLs that are encoded in the same
	 * request. Useful if generating a random value is a bit
	 * more expensive than checking a cache for it.
   * @param url the url to be encoded.
   * @return the encoded URL if encoding is needed; the unchanged
   * URL otherwise.
   * @see javax.servlet.http.HttpServletResponse#encodeURL(String)
	 */
	@Override public String encodeURL(String url){
		String result = url;
		if (-1 < url.indexOf(".xhtml")){
			String separator = -1 < url.indexOf("?") ? "&" : "?";
			String value = this.getRandom();
			result = url + separator + "dgv=" + value;
		}
		return result;
	}

	/** Source of randomness for getRandom(). Threadsafe.
	 * @see #getRandom() */
	private static Random random=new java.util.Random();

	/** Makes a fresh random String that can be used to distinguish
	 * URLs. Must be fast. There must be no obvious sequence in the
	 * values. The values must be made up of characters acceptable
	 * in URLs.
	 * @return String */
	private String getRandom(){
		//Sources of random values:
		//System.currentTimeMillis(). Predictable values.
		//new Object().toString(). Doesn't vary enough.
		//Math.random():double 0 <= x < 1. Ok, 64 bits.
		//ResultSet.getTimeStamp w/ nano-second: expensive.
		//java.util.Random.nextInt(Integer.MAX_VALUE). Ok, 32 bits.
		//java.util.Random.nextLong() Ok, 64 bits. Choose that one.
		long n = random.nextLong();
		if (n < 0) n = -n;//only positive
		String s = Long.toString(n);//Acceptable chars only
		return s;
	}

	/** Must override this one too so that it can be called safely
	 * from ConFusion, which is not case-sensitive.
	 * @param url the URL to be encoded
	 * @return URL with added random parameter */
	@Override public String encodeUrl(String url){
		return this.encodeURL(url);
	}

	/** Completes relative URL properly and send a redirect (302) response.
	 * We override the standard implementation because our rules for completing
	 * relative URLs are different.
	 * The problem is that the connector from Apache to JBoss prefixes
	 * something like "/http/web".
	 * Our HttpURL always gives the correct completion of a relative URL.
	 * @param location
	 * @throws IOException
	 * */
	@Override public void sendRedirect(String location) throws IOException {
		HttpURL httpURL = new HttpURL(location);//this definitely makes the URL be complete
		String completeLocation = httpURL.toString();
		super.sendRedirect(completeLocation);
	}

	/** Filters characters that are non Latin-1 out of the message. */
	@Override public void sendError(int sc, String msg) throws IOException {
		msg=clean(msg);
		if (logger.isDebugEnabled()) {
			String p = System.getProperty("org.apache.coyote.USE_CUSTOM_STATUS_MSG_IN_HEADER");
			logger.debug("USE_CUSTOM_STATUS_MSG_IN_HEADER: " + p);
		}
		super.sendError(sc, msg);
	}

	//Helpers ------------------------------------------------------

	/** Removes non Latin-1 chars from the s,
	 * so that it is safe in http headers.
	 * Accepts only chars in [0,255].
	 * You can refine this method. */
	private String clean(String s){
		if(s==null)
			return null;
		StringBuilder builder=new StringBuilder();
		for(int i=0; i<s.length(); i++){
			char c=s.charAt(i);
			if(0 <= c && c < 256)
				builder.append(c);
		}
		s=builder.toString();
		return s;
	}
}