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

import inspiracio.lang.StringBufferUtils;
import inspiracio.lang.StringBuilderUtils;
import inspiracio.net.UTF8URLEncoder;
import inspiracio.servlet.jsp.PageContextFactory;
import inspiracio.servlet.jsp.ServletPageContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;


/** A JavaBean for a complete URL for http and https. 
 * Methods for making and manipulating URLs easily. 
 * <p>
 * This class has methods to access each of the parts
 * of a URL separately. This class is not threadsafe. 
 * (That's normal for a JavaBean.)
 * <p>
 * Nomenclature:
 * A <b>complete</b> URL begins with the protocol.
 * An <b>absolute</b> URL begins after host and port, with "/".
 * A <b>relative</b> URL does not begin with "/".
 * 
 * <h2>Parts of a URL</h2>
 * A URL is 
 * divided in these parts:
 * <p>
 * scheme://host:port path file?parameters
 * <ul>
 * 	<li>scheme: "http" or "https".
 * 	<li>host: The host may be a string or IP.
 * 	<li>machine: the first part of the host
 * 	<li>domain: the part of the host after the machine
 * 	<li>base domain: the part of the host that can be used for cookies,
 * 		for example ".inspiracio.cat"
 * 	<li>port: Standard ports are suppressed, see below.
 * 	<li>path: The path starts and ends in "/". It may be "/".
 * 	<li>file: The file does not contain "/" and may be empty.
 * 	<li>extension: The file extension. "html" is the extension of "index.html".
 *	<li>parameters: The parameters may be empty.
 * </ul>
 * 
 * <h2>Ports</h2>
 * Standard ports are suppressed from the URL.
 * That means the protocol and the port are dependent on each other,
 * which is not normal for a JavaBean.
 * The client programmer has to think: HttpURL is a wrapper for a StringBuilder, nothing more.
 * <ul>
 * 	<li>getPort: if there is no port, return standard port
 * 	<li>setPort: set the new port; normalise port
 * 	<li>setProtocol: set the new protocol; normalise port
 * 	<li>constructors and similar: set the url; normalise port
 * </ul>
 * Normalise port means: remove it if it is the standard for the protocol.
 * Disadvantage: It is possible to lose an explicitly set port!
 * <code><br/>
 * url.setProtocol("http");<br/>
 * url.setPort(80);<br/>
 * url.setProtocol("https"); //port 80 is lost<br/>
 * port = url.getPort(); //443<br/>
 * </code>
 * 
 * @author alexanderb */
@SuppressWarnings("deprecation")
public class HttpURL implements Cloneable, Comparable<HttpURL> {

	/* This class just keeps the whole complete URL in a StringBuilder and all
	 * the methods parse or format the part they need. That is good,
	 * because each method is only some index manipulations which are
	 * fast and there is not a lot of String creation.
	 * 
	 * Sadly I can not extend the java.net.URL because it is final.
	 * 
	 * Improvements:
	 * Factor out repeated index calculations. For example:
	 * -protocolEnd
	 * -machineEnd
	 * -portBegin=hostEnd
	 * -portEnd=pathBegin
	 * -pathEnd=fileBegin
	 * -question=fileEnd
	 * Could make private methods the return these indexes and
	 * program the business methods in terms of them.
	 * Cleaner, more robust code, with fewer repetitions. */

	/** encoding for URL parameters */
	private final static String ENCODING="UTF-8";

	/** The complete URL in a StringBuilder. */
	private StringBuilder url=null;

	/** Make an HttpURL initialised to the current complete URL.
	 * If there is a current request, initialise the buffer with
	 * that request. If there is no current request, leave the
	 * buffer null. The client must call setters on the constructed
	 * instance, for example setComplete().
	 * */
	public HttpURL(){
		try{
			this.setCurrentURL();
		}catch(NoCurrentRequestException e){
			this.url=null;
		}
	}
	
	/** Make an HttpURL from a complete URL, an absolute URL, or
	 * a relative URL. A complete URL must start with "http://" or
	 * "https://". An absolute URL starts with "/" and is interpreted
	 * relative to the protocol, host, and port of the current request.
	 * A relative URL does not start with "http://" nor "https://" nor
	 * "/", and is interpreted relative to the protocol, host, port,
	 * and path of the current request.
	 * <p>
	 * @param url as a String. Strings are immutable,
	 * 	so any manipulations on this HttpURL cannot affect the client. */
	public HttpURL(String url) {
		this();//Initialise to current URL
		if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//")){
			//It is a complete URL.
			this.setCompleteURL(url);
		} else if (url.startsWith("/")){
			//It is an absolute URL
			this.setAbsolute(url);
		} else {
			//It is a relative URL.
			//Make absolute URL from the relative URL.
			this.setRelative(url);
		}
	}

	/** Sets this HttpURL to be the URL of the current request.
	 * Must be called within a request. 
	 * Removes port if it is the standard port.
	 * @exception NoCurrentRequestException There is no current URL.
	 * 	We are not within the thread of a Request. */
	private void setCurrentURL() throws NoCurrentRequestException {
		PageContext pc=PageContextFactory.getPageContext();
		if(pc==null)
			throw new NoCurrentRequestException("No page context.");
		
		if(pc instanceof ServletPageContext){
			//Get servlet path.
			ServletPageContext spc=(ServletPageContext)pc;
			String originalCompleteServletPath=spc.getOriginalCompleteServletPath();//NullPointerException

			//Get query parameters.
			HttpServletRequest request=(HttpServletRequest)pc.getRequest();
			StringBuilder builder=new StringBuilder(originalCompleteServletPath);
			String queryString=request.getQueryString();
			if(queryString!=null){
				builder.append('?');
				builder.append(queryString);
			}
			this.setCompleteURL(builder);
			return;
		}
		// Incorrect after forward. Shows forward URL, not the original URL.
		HttpServletRequest request=(HttpServletRequest)pc.getRequest();
		if(request==null)
			throw new NoCurrentRequestException();
		StringBuffer buffer=request.getRequestURL();//quite good, but lacks parameters
		String queryString=request.getQueryString();
		if(queryString!=null){
			buffer.append('?');
			buffer.append(queryString);
		}
		this.url=new StringBuilder(buffer.toString());
		this.normalizePort();
	}
	
	/** Make an HttpURL from a complete URL, an absolute URL, or
	 * a relative URL. 
	 * <p>
	 * A complete URL must start with "http://" or "https://". 
	 * HttpServletRequest.getRequestURL returns complete URLs (without
	 * parameters).
	 * <p>
	 * An absolute URL starts with "/" and is interpreted
	 * relative to the protocol, host, and port of the current request.
	 * <p>
	 * A relative URL does not start with "http://" nor "https://" nor
	 * "/", and is interpreted relative to the protocol, host, port,
	 * and path of the current request.
	 * <p>
	 * @param url as a StringBuffer, this constructor takes a copy of the data
	 */
	public HttpURL(StringBuffer url) {
		this();//Initialise to current URL
		if (
			StringBufferUtils.startsWith(url, "http://") || 
			StringBufferUtils.startsWith(url, "https://")
		){
			//It is a complete URL.
			String s=url.toString();
			this.setCompleteURL(s);
		} else if (StringBufferUtils.startsWith(url, "/")){
			//It is an absolute URL
			this.setAbsolute(url.toString());
		} else {
			//It is a relative URL.
			//Make absolute URL from the relative URL.
			this.setRelative(url.toString());
		}
	}
	
	/** Make an HttpURL from a complete URL, an absolute URL, or
	 * a relative URL. 
	 * <p>
	 * A complete URL must start with "http://" or "https://". 
	 * HttpServletRequest.getRequestURL returns complete URLs (without
	 * parameters).
	 * <p>
	 * An absolute URL starts with "/" and is interpreted
	 * relative to the protocol, host, and port of the current request.
	 * <p>
	 * A relative URL does not start with "http://" nor "https://" nor
	 * "/", and is interpreted relative to the protocol, host, port,
	 * and path of the current request.
	 * <p>
	 * @param url as a StringBuilder. StringBuilders are mutable,
	 * so if the client retains a reference to the StringBuiler, the client
	 * should not manipulate the StringBuilder from the outside.
	 */
	public HttpURL(StringBuilder url) {
		this();//Initialise to current URL
		if (
			StringBuilderUtils.startsWith(url, "http://") || 
			StringBuilderUtils.startsWith(url, "https://")
		){
			//It is a complete URL.
			String s=url.toString();
			this.setCompleteURL(s);
		} else if (StringBuilderUtils.startsWith(url, "/")){
			//It is an absolute URL
			this.setAbsolute(url.toString());
		} else {
			//It is a relative URL.
			//Make absolute URL from the relative URL.
			this.setRelative(url.toString());
		}
	}
	
	/** Appends something to the underlying string buffer. 
	 * No syntax checks. 
	 * Returns this so that you can chain calls:
	 * url.append(a).append(b).
	 * */
	public HttpURL append(Object o){
		this.url.append(o);
		return this;
	}

	/** Returns a deep copy.
	 * @see java.lang.Object#clone() */
	@Override public HttpURL clone() {
		String url=this.toString();
		HttpURL clone=new HttpURL(url);
		return clone;
	}

	/** Natural order on URLs: alphabetic. */
	@Override public int compareTo(HttpURL o) {
		String ts=this.toString();
		String os=o.toString();
		int compare=ts.compareTo(os);
		return compare;
	}

	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override public boolean equals(Object obj) {
		if(obj instanceof HttpURL){
			//Maybe can be improved.
			HttpURL other=(HttpURL)obj;
			String s=other.toString();
			return s.equals(this.toString());
		}
		return false;
	}

	/** @see java.lang.Object#hashCode() */
	@Override public int hashCode() {
		return this.toString().hashCode();
	}

	/** Set this to represent the given complete URL.
	 * This instance of HttpURL keeps the StringBuilder,
	 * do not modify it from the outside and through HttpURL.
	 * @param completeURL */
	public void setCompleteURL(StringBuilder completeURL){
		this.url=completeURL;
		this.normalizePort();//maybe remove superfluous standard port
	}
	
	/** Set this to represent the given complete URL.
	 * This instance of HttpURL keeps the StringBuffer,
	 * do not modify it from the outside and through HttpURL.
	 * @param completeURL */
	public void setCompleteURL(String completeURL){
		this.url=new StringBuilder(completeURL);
		this.normalizePort();//maybe remove superfluous standard port
	}
	
	/** Interprets the given relative URL relative to
	 * the current value of this HttpURL. For example,
	 * if the current value is 
	 * "https://ssl.inspiracio.cat/adecco/home.cfm"
	 * and the parameter is "img/logo.gif", then the resulting
	 * value of this is "https://ssl.inspiracio.cat/adecco/img/logo.gif".
	 * @param relativeURL something like img/logo.gif. Does not
	 * start with "/".
	 * */
	public void setRelative(String relativeURL){
		this.setFile(relativeURL);
		this.cancelUps();
	}
	
	/** Interprets the given absolute URL relative to
	 * the current value of this HttpURL. Preserves the 
	 * protocol, host, and port.
	 * For example, if the current value is 
	 * "https://ssl.inspiracio.cat:445/adecco/home.cfm"
	 * and the parameter is "/Adesla/logo.gif", then the resulting
	 * value of this is "https://ssl.inspiracio.cat:445/Adesla/logo.gif".
	 * @param absoluteURL Must start with "/".
	 * */
	public void setAbsolute(String absoluteURL){
		int hostBegin=this.getHostBegin();
		int pathBegin=this.url.indexOf("/", hostBegin);
		if(pathBegin<0){
			//URL is like "http://www.inspiracio.cat"
			//No "/" after the host and port.
			pathBegin=this.url.length();
		}
		this.url.replace(pathBegin, this.url.length(), absoluteURL);
	}
	
	/** Returns this URL as absolute within the server, that
	 * means omits protocol, host, and port.
	 * For example, if the current value is 
	 * "https://ssl.inspiracio.cat:445/adecco/home.cfm?secure=1",
	 * then will return "/adecco/home.cfm?secure=1".
	 * @return absolute URL starting with "/". May be just "/".
	 * */
	public String getAbsolute(){
		String absolute = "/";
		int hostEnd = this.getHostEnd();
		if (this.url.length()<=hostEnd){
			//Like "http://www.inspiracio.cat"
			absolute = "/";
		} else {
			//find first '/' after host end
			int absStart = this.url.indexOf("/", hostEnd);
			if (absStart < 0){
				//Like "http://www.inspiracio.cat:8443"
				absolute = "/";
			} else {
				absolute = this.url.substring(absStart);
			}
		}
		return absolute;
	}
	
	/** Sets the file part of the URL, forgetting parameters. 
	 * @param file */
	public void setFile(String file){
		//the file part is after the last "/" that is not "//" -or right at the end
		int slash=this.url.lastIndexOf("/");
		int doubleslash=this.url.lastIndexOf("//");
		int start;
		int end=this.url.length();//of complete URL
		if(doubleslash+1==slash){//URL is like http://domain
			this.url.append('/');//http://domain/
			end=end+1;
			start=end;
		}else{
			start=slash+1;//of file part
		}
		this.url.replace(start, end, file);//StringIndexOutOfBoundsException if there was no "/" in this.url
	}
	
	/** Gets the file part of the URL, without parameters,
	 * for example "gen_multisite.jsp". 
	 * The file part is after the last "/" to the next "?" 
	 * or to the end.
	 * @return file May be "", never null. */
	public String getFile(){
		//int slash = this.url.lastIndexOf("/");
		int pathEnd = this.getPathEnd();
		
		int fileEnd = this.url.indexOf("?", pathEnd);
		if (fileEnd<0){//there is no "?"
			fileEnd = this.url.length();
		}

		//Special rule for "oferta.empleo/..."
		int empleo = this.url.indexOf(".empleo/");
		if (0 < empleo){
			fileEnd = empleo + 7;
		}
		
		String file;
		if (pathEnd==fileEnd){
			file = "";
		} else {
			file = this.url.substring(pathEnd, fileEnd);
		}
		return file;
	}
	
	/** Returns the extension of the file, for example "jpg". 
	 * The file extension is the part after the last "." in the
	 * file. If there is none, returns null.
	 * (Does not return "" in this case, because that would
	 * be correct for file "file." --nothing after last ".".)
	 * @return file extension, without preceding "." or null if 
	 * 	if there is none. */
	public String getExtension(){
		String extension = null;
		String file = this.getFile();
		int dot = file.lastIndexOf('.');
		if (0<dot){
			extension = file.substring(dot+1);
		}
		return extension;
	}
	
	/** The complete URL as String
	 * @return String */
	@Override public String toString() {
		//Improve by caching this?
		return url.toString();
	}

	/** The complete URL as StringBuffer.
	 * @deprecated Use StringBuilder instead.
	 * @return StringBuffer */
	public StringBuffer toStringBuffer(){
		String s=this.url.toString();
		StringBuffer buffer=new StringBuffer(s);
		return buffer;
	}

	/** The underlying StringBuilder.
	 * If the client modifies the StringBuilder,
	 * that modifies the HttpURL.
	 * @return StringBuffer */
	public StringBuilder toStringBuilder(){return url;}

	/** Return the host.
	 * The host is between "://" and the next "/" or ":" or 
	 * extends to the end of the URL in a case like
	 * "http://www.inspiracio.cat".
	 * @return host */
	public String getHost() {		
		int hostBegin=this.url.indexOf("://") + 3;
		int hostEnd=this.getHostEnd();
		String host=this.url.substring(hostBegin, hostEnd);//StringIndexOutOfBoundsException
		return host;
	}

	/** Returns the index of the first char after the host.
	 * That char is ":" if the URL has an explicit port,
	 * or is "/" if the URL has no explicit port, or is 
	 * after the end of the URL if the URL has nothing after the
	 * host.
	 * @return index of first char after the host */
	private int getHostEnd(){
		int hostBegin = this.getHostBegin();
		int slash = this.url.indexOf("/", hostBegin);
		int colon = this.url.indexOf(":", hostBegin);
		int hostEnd = 0;
		if (hostBegin < colon){
			//There is a port.
			hostEnd = colon;
		} else if (hostBegin < slash){
			//There is no port, but a slash.
			hostEnd = slash;
		} else {
			//There is nothing after the host.
			hostEnd = this.url.length();
		}
		return hostEnd;
	}

	/** Returns the machine. The machine is the part of the host
	 * before the first ".". Examples "www" or "static0".
	 * This method works if the host has a machine part. 
	 * For hosts that are a single word or IPs, the result
	 * is undefined.
	 * @return String */
	public String getMachine(){
		int hostBegin = this.url.indexOf("://") + 3;
		int machineEnd = this.url.indexOf(".", hostBegin);
		String machine = this.url.substring(hostBegin, machineEnd);
		return machine;
	}
	
	/** Returns the base domain.
	 * The base domain begins with ".". It starts after the
	 * part that is matched by the wildcard in the web server.
	 * There is no general rule. Here implemented:
	 * For "bla.bla.inspiracio.cat" returns ".inspiracio.cat".
	 * For "bla.bla.inspiracio.cat.algo" returns ".inspiracio.cat.algo".
	 * <p>
	 * Useful for setting domains in cookies.
	 * @return String */
	public String getBaseDomain(){
		//Find begin and end of the host:
		int hostBegin = this.getHostBegin();
		int slash = this.url.indexOf("/", hostBegin);
		int colon = this.url.indexOf(":", hostBegin);
		// If there is a port, begin < colon < slash.
		int hostEnd = (hostBegin < colon && colon < slash) ? colon : slash;
		
		//Find second "." before end of the host.
		int lastDot = this.url.lastIndexOf(".", hostEnd);//first
		int dot = this.url.lastIndexOf(".", lastDot-1);//second
		
		// Patch for .com.mx and .com.co
		if (this.url.substring(dot,lastDot).equals(".com")) {
			dot = this.url.lastIndexOf(".", dot-1);//third
		}
		// Patch for an URL without machine name
		if (dot<0) {
			dot = hostBegin;
		}
		// baseDomain must start with a dot
		String baseDomain = this.url.substring(dot, hostEnd);//StringIndexOutOfBoundsException
		if (!baseDomain.startsWith(".")) {
			baseDomain = "." + baseDomain;
		}
		return baseDomain;
	}

	/** Sets the machine. The machine is the part of the host
	 * before the first ".". Examples "www" or "static0".
	 * This method works if the host has a machine part. 
	 * For hosts that are a single work or IPs, the result
	 * is undefined.
	 * @param machine */
	public void setMachine(String machine){
		int hostBegin = this.getHostBegin();
		int machineEnd = this.url.indexOf(".", hostBegin);
		this.url.replace(hostBegin, machineEnd, machine);
	}

	/** Returns the domain. The domain is the part of the host
	 * after the first ".". Example "inspiracio.cat".
	 * @return String */
	public String getDomain(){		
		int hostBegin = this.url.indexOf("://") + 3;
		int begin = this.url.indexOf(".", hostBegin) + 1;
		int slash = this.url.indexOf("/", begin);
		int colon = this.url.indexOf(":", begin);
		// If there is a port, begin < colon < slash.
		int hostEnd = (begin < colon && colon < slash) ? colon : slash;
		String domain = this.url.substring(begin, hostEnd);
		return domain;
	}	
	
	/** Put in a normal host if the host has 
	 * "*.*.inspiracioX.cat".
	 * This method must be updated when we do more interesting things
	 * with URLs.
	 * */
	public void normalizeHost(){
	}

	/** Sets the host, leaving everything else the same.
	 * @param host
	 */
	public void setHost(String host){
		// The host is between "://" and the next "/" or ":"
		//Does not expect complete URLs like "http://www.inspiracio.cat".
		int hostBegin=this.getHostBegin();
		int slash=this.url.indexOf("/", hostBegin);
		int colon=this.url.indexOf(":", hostBegin);
		// If there is a port, begin < colon < slash.
		int hostEnd=(hostBegin < colon && colon < slash) ? colon : slash;
		this.url.replace(hostBegin, hostEnd, host);//StringIndexOutOfBoundsException
	}

	/** Gets the path. The path is after the host and port. 
	 * The path begins and ends in "/". It may be just "/".
	 * @return String
	 */
	public String getPath() {
		int pathBegin = this.getPathBegin();
		int pathEnd = this.getPathEnd();
		String path; 
		if (pathBegin==pathEnd){
			path = "/";
		} else {
			path = this.url.substring(pathBegin, pathEnd);//StringIndexOutOfBoundsException
		}
		return path;
	}
	
	/** @return index of the first char of the path. The first char
	 * of the path is the "/" after the host and port. If the URL
	 * is like "http://www.inspiracio.com", getPathBegin and length
	 * are equal. The path would start after the URL. */
	private int getPathBegin(){
		int hostBegin = this.getHostBegin();
		int pathBegin = this.url.indexOf("/", hostBegin);
		if (pathBegin <0){
			//there is no "/" at all after the host
			pathBegin = this.url.length();
		}
		return pathBegin;
	}

	/** @return index of the char after the last char of the path. 
	 * The path ends in "/" so the path end in the index of the
	 * first char of the file.  
	 * If the URL is like "http://www.inspiracio.com", getPathEnd 
	 * and length are equal. The path would start and end after 
	 * the URL. */
	private int getPathEnd(){
		int pathEnd = this.url.lastIndexOf("/") + 1;
		int hostBegin = this.getHostBegin();
		if (pathEnd <= hostBegin){
			//there is no "/" at all after the host
			pathEnd = this.url.length();
		}
		return pathEnd;
	}

	/** Returns a String containing the real path for a this URL.
	 * Use this in preference over javax.servlet.ServletContext.getRealPath(path),
	 * because the servlet context does not take into account the relationship
	 * between Apache and CF, which we have defined as virtual path in the
	 * mod_rewrite.conf of each portal. See inspiracio.servlet.http.HttpdIni.getVirtualPath()
	 * @return Absolute path, in notation for this operating system.
	 * */
	public String getRealPath(){
		PageContext pc = PageContextFactory.getPageContext();
		ServletContext servletContext = pc.getServletContext();
		//HttpdIni httpdIni = HttpdIni.getInstance();
		String virtualPath = "";//httpdIni.getVirtualPath();
		String path = this.getPath();//Starts with "/"
		
		//path = "/" + virtualPath + path.substring(0) + this.getFile()
		//Optimization: In Java 5, use StringBuilder
		StringBuffer buffer = new StringBuffer(path);
		buffer.insert(1, virtualPath);//Insert after initial "/".
		String file = this.getFile();
		buffer.append(file);
		path = buffer.toString();
		
		String realPath = servletContext.getRealPath(path);
		return realPath;
	}
	
	/** @return index of the first char of the host. */
	private int getHostBegin(){
		int hostBegin = this.url.indexOf("://") + 3;
		return hostBegin;
	}

	/** Sets the path, maintaining the file and parameters.
	 * @param path Must begin and end in "/".
	 */
	public void setPath(String path) {
		//Does not expect complete URLs without a "/" after the host.
		int hostBegin = this.getHostBegin();
		int pathBegin = this.url.indexOf("/", hostBegin);
		int pathEnd = this.getPathEnd();
		this.url.replace(pathBegin, pathEnd, path);//StringIndexOutOfBoundsException
	}

	/**
	 * Sets a URL parameter. If there already is a URL parameter 
	 * with the same key, it is overwritten. This methods takes 
	 * care of URL-encoding for the key and the value.
	 * @param key
	 * @param value
	 * @exception NoSuchElementException Current URL parameters have bad format.
	 */
	public void setParameter(String key, String value) throws NoSuchElementException {
		try {
			String key1 = URLEncoder.encode(key, ENCODING);
			String value1 = URLEncoder.encode(value, ENCODING);
			Map<String, String> parameters = this.getParameters();//NoSuchElementException
			parameters.put(key1, value1);
			this.setParameters(parameters);
		} catch (UnsupportedEncodingException e){
			//should never happen
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets a URL parameter. If there already is a URL parameter 
	 * with the same key, it is overwritten. This methods takes 
	 * care of URL-encoding for the key and the value.
	 * @param key
	 * @param value
	 * @exception NoSuchElementException Current URL parameters have bad format.
	 */
	public void setParameter(String key, boolean value) throws NoSuchElementException {
		try {
			String key1 = URLEncoder.encode(key, ENCODING);
			String value1 = Boolean.toString(value);
			Map<String, String> parameters = this.getParameters();//NoSuchElementException
			parameters.put(key1, value1);
			this.setParameters(parameters);
		} catch (UnsupportedEncodingException e){
			//should never happen
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets a URL parameter. If there already is a URL parameter 
	 * with the same key, it is overwritten. This methods takes 
	 * care of URL-encoding for the key and the value.
	 * @param key
	 * @param value
	 * @exception NoSuchElementException Current URL parameters have bad format.
	 */
	public void setParameter(String key, long value) throws NoSuchElementException {
		try {
			String key1 = URLEncoder.encode(key, ENCODING);
			String value1 = Long.toString(value);
			Map<String, String> parameters = this.getParameters();//NoSuchElementException
			parameters.put(key1, value1);
			this.setParameters(parameters);
		} catch (UnsupportedEncodingException e){
			//should never happen
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the URL parameters as a modifiable Map. 
	 * Modifications to the map do not affect the URL.
	 * <p>
	 * This implementation does not treat multiple values for one 
	 * key correctly. If they are combined with ",", treats the 
	 * whole list as value. If there are multiple instances of the 
	 * same key, the last one wins.
	 * That is acceptable for now, because parameters with
	 * multiple values are a source of errors anyway.
	 * @exception NoSuchElementException The parameters in the URL 
	 * 	have bad format.
	 * @return Map
	 */
	public Map<String, String> getParameters() throws NoSuchElementException {
		int question = this.url.lastIndexOf("?");
		
		if (question < 0) {
			return new HashMap<String, String>();
		} else {
			// there are some parameters
			Map<String, String> parameters = new HashMap<String, String>();
			String queryString = this.url.substring(question + 1);
			
			//Regular expression implementation
			String regex = "[\\?=&]";//Match '?', '=', '&'.
			String[] tokens = queryString.split(regex, -1);
			int i = 0;
			while (i < tokens.length){
				String key = tokens[i];
				String value = i+1<tokens.length ? tokens[i+1] : "";
				key = UTF8URLEncoder.decode(key);
				value = UTF8URLEncoder.decode(value);
				parameters.put(key, value);
				i = i+2;
			}
			return parameters;
		}
	}
	
	/** Gets a parameter. If there is no parameter with the
	 * given key, returns null.
	 * @param key
	 * @return parameter value, or null */
	public String getParameter(String key){
		Map<String, String> parameters=this.getParameters();
		String value=parameters.get(key);
		return value;
	}

	/** Gets a parameter as int. 
	 * If there is no parameter with the given key or the 
	 * value is not an int, returns null.
	 * @param key
	 * @return parameter value, or null */
	public Integer getIntParameter(String key){
		String s=this.getParameter(key);
		try{
			return Integer.parseInt(s);
		}catch(NumberFormatException nfe){}
		return null;
	}

	/** Gets a parameter, treat the key case-insensitive.
	 * If there is no parameter with the given key, returns null.
	 * You should always prefer the method getParameter, as it is 
	 * more efficient, and because everything we do should be
	 * case sensitive.
	 * @param key
	 * @return parameter value, or null */
	public String getParameterNC(String key){
		Map<String, String> parameters = this.getParameters();
		Map<String, String> parametersLC = new TreeMap<String, String>();
		Iterator<Map.Entry<String, String>> entries = parameters.entrySet().iterator();
		while (entries.hasNext()){
			Map.Entry<String, String> entry = entries.next();
			String keyCase = entry.getKey().toString();
			String keyLC = keyCase.toLowerCase();
			String value = entry.getValue();
			parametersLC.put(keyLC, value);
		}
		String keyLC = key.toLowerCase();
		String value = parametersLC.get(keyLC);
		return value;
	}

	/** Sets the parameters of the URL to the given mappings,
	 * overwriting all the existing parameters. 
	 * @param parameters A Map<String, String> */
	public void setParameters(Map<String, String> parameters){
		int question=this.url.lastIndexOf("?");
		if(0<question){
			//there are parameters already
			int length = this.url.length();
			this.url.replace(question, length, "");
		}
		
		//Assert: url has no parameters
		char connector='?';
		Iterator<Map.Entry<String,String>> entries=parameters.entrySet().iterator();
		while(entries.hasNext()){
			Map.Entry<String,String> entry = entries.next();
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			key = UTF8URLEncoder.encode(key);
			value = UTF8URLEncoder.encode(value);
			this.url.append(connector);
			this.url.append(key);
			this.url.append("=");
			this.url.append(value);
			connector = '&';
		}
	}
	
	/** Sets the port of the URL. 
	 * If the port is the standard port for the protocol, 
	 * there will be no explicit port in the URL.
	 * @param port The new port */
	public void setPort(int port){
		int hostBegin = this.getHostBegin();
		int colon = this.url.indexOf(":", hostBegin);
		int pathBegin = this.getPathBegin();
		int portBegin = hostBegin<colon && colon<pathBegin ? colon : pathBegin;
		int portEnd = pathBegin;
		String portString = ":" + port;
		this.url.replace(portBegin, portEnd, portString);//StringIndexOutOfBoundsException
		this.normalizePort();//Remove the port if it is standard
	}
	
	/** Gets the port of the URL. 
	 * If there is no port, guesses the standard ports:
	 * http: 80 and https: 443.
	 * @return the port */
	public int getPort(){
		int port = -1;
		int hostBegin = this.getHostBegin();
		int colon = this.url.indexOf(":", hostBegin);
		int pathBegin = this.getPathBegin();
		if (hostBegin<colon && colon<pathBegin){
			//There is a port.
			int portBegin = colon + 1;
			int portEnd = pathBegin;
			String portString = this.url.substring(portBegin, portEnd);
			port = Integer.parseInt(portString);
		} else {
			//There is no port. Return standard port.
			String protocol = this.getScheme();
			port = getStandardPort(protocol);
		}
		return port;
	}
	
	/** If there is an explicit port in the URL and it is the standard
	 * port for the protocol, remove the port. */
	private void normalizePort(){
		int hostBegin=this.getHostBegin();
		int colon=this.url.indexOf(":", hostBegin);
		int pathBegin=this.getPathBegin();
		if(hostBegin<colon && colon<pathBegin){
			//There is a port.
			int portBegin=colon + 1;
			int portEnd=pathBegin;
			String portString=this.url.substring(portBegin, portEnd);
			int port=Integer.parseInt(portString);
			
			//If the port is standard, remove it.
			String protocol=this.getScheme();
			int standardPort=getStandardPort(protocol);
			if(port==standardPort){
				//remove it
				this.url.replace(colon, portEnd, "");
			}
		} else {
			//There is no port. Do nothing.
		}
	}//normalizePort

	/** Return the standard port for the protocol,
	 * or -1 if this method doesn't know the protocol. 
	 * @param protocol "http" o "https"
	 * @return standard protocol, or -1 */
	static int getStandardPort(String protocol){
		int port=-1;
		if("http".equals(protocol)){
			port=80;
		}else if("https".equals(protocol)){
			port=443;
		}
		return port;
	}
	
	/** Gets the scheme of the URL. 
	 * @return the part before "://", usually "http" or "https"*/
	public String getScheme(){
		int schemeEnd=this.url.indexOf("://");
		String scheme=this.url.substring(0, schemeEnd);
		return scheme;
	}
	
	/** Sets the scheme of the URL. 
	 * If there was a port and the port is the standard port of the new
	 * scheme, it will be removed.
	 * If there was no port, there will be no port.
	 * @param scheme the part before "://", usually "http" or "https"*/
	public void setScheme(String scheme){
		int schemeEnd=this.url.indexOf("://");
		this.url.replace(0, schemeEnd, scheme);
		this.normalizePort();//Maybe now the port has become superfluous.
	}
	
	/** Replaces "/folder/.." by "".
	 * */
	private void cancelUps(){
		final String UP = "/..";
		int up = this.url.indexOf(UP);
		while (0<up){
			//remove one "/folder/.."
			int slash = this.url.lastIndexOf("/", up-1);
			this.url.replace(slash, up+UP.length(), "");
			//find next up
			up = this.url.indexOf(UP);
		}
	}//cancelUps
	
	/** testing only 
	 * @param args 
	 * @exception Exception */
	public static void main(String[] args) throws Exception {
		HttpURL url;
		String[] ss = {
				"http://ws.inspiracio.cat/atom/-/offer?employer-id=&q=",
				"https://ssl.inspiracio.cat:443/gen_multisite.ij/iCodigoPerfil=123&perfil=inspiracio",
				"http://ssl.inspiracio.cat:80/gen_multisiteinspiracio.html",
				"http://ssl.inspiracio.cat:80",
				"https://ssl.inspiracio.cat:443",
				"http://ssl.inspiracio.cat:81"
		};
		for (int i=0; i<ss.length; i++){
			String s = ss[i];
			url = new HttpURL(s);
			say(url);
			Map<String, String> ps = url.getParameters();
			say(ps);
		}
		say("bye");say(0);
	}
	
	private static void say(Object o){System.out.println(o);}
	private static void say(int i){System.out.println(i);}
	
	/** Matches four groups of 1-3 digits separated by dots or "localhost" */
	public final static Pattern PATTERN_IPV4=Pattern.compile("((\\d{1,3}(\\.|$)){4})|(localhost)");

}//HttpURL
