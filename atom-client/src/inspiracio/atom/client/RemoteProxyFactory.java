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
import inspiracio.lang.Equals;
import inspiracio.net.IHttpURLConnection;
import inspiracio.security.PrincipalMaker;
import inspiracio.servlet.http.ETag;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.HttpURL;
import inspiracio.servlet.http.InternalServerErrorException;
import inspiracio.util.Base64Coder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import atom.Entry;
import atom.Feed;
import atom.gdata.GDataParameters;
import atom.gdata.Style;

/** Delivers instances of AtomProxy. */
public final class RemoteProxyFactory implements ProxyFactory, PrincipalMaker{
	private static final Logger logger=Logger.getLogger(RemoteProxyFactory.class);

	//State ---------------------------------------------

	/** The URL to use up to "/-/". Do I need the default port here? */
	private String base="https://ubuntu:443/studio/atom/-/";

	/** The user name that will be used for http basic authentication */
	private String name=null;

	/** The password that will be used for http basic authentication */
	private String password=null;

	/** The logged-in principal, if there is one. */
	private Principal principal=null;
	
	/** The cookies to include in every request, in a map. */
	private Map<String,String>cookies=new HashMap<String,String>();
	
	/** Should compress requests and response with gzip? */
	private boolean gzip=true;

	//Constructor --------------------------------------

	/** New factory
	 * @param base Base URL, like "https://ubuntu:443/studio/atom/-/"
	 * */
	public RemoteProxyFactory(HttpURL base){
		this();
		this.base=base.toString();
	}

	/** new factory */
	public RemoteProxyFactory(){
//		PrincipalFactory.addPrincipalMaker(this);//Register it as principal maker
	}

	//Accessors ----------------------------------------

	/** Sets the scheme: "http" or https".
	 * If the current port is the default port for the
	 * current scheme, also changes the port to the
	 * default port for the new scheme.
	 * */
	public void setScheme(String scheme){
		HttpURL url=new HttpURL(this.base);
		String old=url.getScheme();
		if(!scheme.equals(old)){	//only if there's really a change
			url.setScheme(scheme);
			this.base=url.toString();
			PropertyChangeEvent e=new PropertyChangeEvent(this, "scheme", old, scheme);
			this.propertyChanged(e);
		}
	}

	public String getScheme(){
		HttpURL url=new HttpURL(this.base);
		return url.getScheme();
	}

	public void setHost(String host){
		HttpURL url=new HttpURL(this.base);
		String old=url.getHost();
		if(!host.equals(old)){	//only if there's really a change
			url.setHost(host);
			this.base=url.toString();
			PropertyChangeEvent e=new PropertyChangeEvent(this, "host", old, host);
			this.propertyChanged(e);
		}
	}

	/** Compress requests and responses with gzip? */
	public void setGZip(boolean b){this.gzip=b;}

	/** Compress requests and responses with gzip? */
	public boolean getGZip(){return this.gzip;}

	public String getHost(){
		HttpURL url=new HttpURL(this.base);
		return url.getHost();
	}

	public void setPort(int port){
		HttpURL url=new HttpURL(this.base);
		Integer old=url.getPort();
		if(!Equals.equals(port, old)){	//Only if there's really a change.
			url.setPort(port);
			this.base=url.toString();
			PropertyChangeEvent e=new PropertyChangeEvent(this, "port", old, port);
			this.propertyChanged(e);
		}
	}

	public int getPort(){
		HttpURL url=new HttpURL(this.base);
		return url.getPort();
	}
	
	/** Sets name for http basic authentication. */
	public void setName(String name){
		String old=this.name;
		if(!Equals.equals(old, name)){	//Only if there's really a change
			this.name=name;
			logger.info("name: " + name);
			PropertyChangeEvent e = new PropertyChangeEvent(this, "name", old, name);
			this.propertyChanged(e);
		}
	}

	/** Gets the name for http basic authentication. */
	public String getName(){return name;}

	/** Sets this cookie to every request. 
	 * Overwrites previously set cookie with same name. */
	public void setCookie(String name, String value){
		this.cookies.put(name, value);
	}
	
	/** Sets the logged-in user principal. */
	@Override public void setCallerPrincipal(Principal principal){this.principal=principal;}

	/** Gets the current user.
	 * If this maker is not appropriate now, return null.
	 * If this maker is appropriate, but there is not current user, also returns null. */
	@Override public Principal getCallerPrincipal(){return this.principal;}

	/** Sets the password for http basic authentication. */
	public void setPassword(String password){
		String old=this.password;
		if(!Equals.equals(old, password)){	//only if there's really a change
			this.password = password;
			logger.info("password: *****");
			PropertyChangeEvent e = new PropertyChangeEvent(this, "password", old, password);
			this.propertyChanged(e);
		}
	}

	public String getPassword(){return password;}
	public String getBaseURL(){return base;}
//	@Override public PageContext getPageContext(){return pc;}

	/** Set name and password together, for http basic authentication.
	 * Generates a property changed event for the listeners without specifying
	 * which property changed. */
	public void setNamePassword(String name, String password){
		String oldName=this.name;
		this.name=name;
		String oldPassword=this.password;
		this.password=password;
		//fire event only if there's really a change
		if(!Equals.equals(oldName, name) || !Equals.equals(oldPassword, password)){
			PropertyChangeEvent e=new PropertyChangeEvent(this, "*", null, null);
			this.propertyChanged(e);
		}
	}

	//The factory method ------------------------------

	/** Given an AtomBean-class, delivers an AtomProxy for it. */
	@SuppressWarnings("unchecked")
	@Override public <T extends AtomBean> AtomProxy<T> get(Class<T> beanClass){
		//Make a proxy object that delegates to in invocation handler.
		InvocationHandler handler=this.makeHandler(beanClass);
		Class<?>[] interfaces={AtomProxy.class};//the interface the proxy will implement
		ClassLoader loader=AtomProxy.class.getClassLoader();//need a lower loader?
		AtomProxy<T> proxy=(AtomProxy<T>)Proxy.newProxyInstance(loader, interfaces, handler);
		return proxy;
	}

	//Events --------------------------------------------

	/** the registered property change listeners */
	private List<PropertyChangeListener> listeners=new ArrayList<PropertyChangeListener>();

	/** adds a property change listener
	 * @param listener */
	public void addPropertyChangeListener(PropertyChangeListener listener){
		synchronized(this.listeners){
			listeners.add(listener);
		}
	}

	/** removes a property change listener
	 * @param listener */
	public void removePropertyChangeListener(PropertyChangeListener listener){
		synchronized(this.listeners){
			listeners.remove(listener);
		}
	}

	/** call all the listeners to tell them about a property change event */
	private void propertyChanged(PropertyChangeEvent e){
		List<PropertyChangeListener> copy=new ArrayList<PropertyChangeListener>();
		synchronized(this.listeners){
			for(PropertyChangeListener listener : this.listeners)
				copy.add(listener);
		}
		for(PropertyChangeListener listener : copy)
			listener.propertyChange(e);
	}

	//Helpers --------------------------------------------

	/** Open a connection to a URL.
	 * The connection can do gzip.
	 * @exception MalformedURLException
	 * @exception IOException
	 * */
	private HttpURLConnection openConnection(HttpURL url)throws MalformedURLException, IOException{
		boolean gzip=RemoteProxyFactory.this.gzip;
		String s=url.toString();
		URL u=new URL(s);//MalformedURLException
		HttpURLConnection con=(HttpURLConnection)u.openConnection();//IOException
		IHttpURLConnection icon=new IHttpURLConnection(con, gzip);
		this.basicAuthentication(icon);
		this.cookies(icon);
		return icon;
	}

	/** If the factory is configured with name and password, adds
	 * request headers for http basic authentication to the
	 * http URL connection. Else does nothing.
	 * @param con
	 * */
	private void basicAuthentication(HttpURLConnection con){
		String name=this.name;
		if(name!=null){
			String password=this.password;
			String credentials = name + ":" + password;
			logger.trace("encoding Basic credentials for user: " + name);
			credentials = Base64Coder.encode(credentials);
			con.setRequestProperty("Authorization", "Basic " + credentials);
		}

		//Also add http header to identify GData version.
		//GData-Version: 2
		con.setRequestProperty("GData-Version", "2");
	}

	/** If the factory is configured with cookie(s), adds a
	 * request header for the cookie(s) to the
	 * http URL connection. Else does nothing.
	 * @param con
	 * */
	private void cookies(HttpURLConnection con){
		if(!this.cookies.isEmpty()){
			StringBuilder value=new StringBuilder();
			boolean first=true;
			for(Map.Entry<String, String> entry : this.cookies.entrySet()){
				String name=entry.getKey();
				String v=entry.getValue();
				if(!first)
					value.append("; ");
				value.append(name);
				value.append("=");
				value.append(v);
				first=false;
			}
			String s=value.toString();
			con.setRequestProperty("Cookie", s);
		}
		
		//Also add http header to identify GData version.
		//GData-Version: 2
		con.setRequestProperty("GData-Version", "2");
	}

	/** https://domain/atom/-/category
	 * Returns a fresh instance of HttpURL which becomes property of the caller. */
	private HttpURL getCategoryURL(Class<? extends AtomBean> beanClass){
		//Optimisable, cacheable
		StringBuilder builder=new StringBuilder();
		builder.append(RemoteProxyFactory.this.base);
		String category=beanClass.getSimpleName();
		category=Character.toLowerCase(category.charAt(0)) + category.substring(1);
		builder.append(category);
		return new HttpURL(builder);
	}

	/** Makes a fresh invocation handler for an Atom bean
	 * @param beanClass like Desk.class */
	private InvocationHandler makeHandler(final Class<? extends AtomBean> beanClass){
		//Opt.: Could cache handlers, as they are stateless.

		InvocationHandler handler=new InvocationHandler(){
			//can refer to beanClass because it is final argument of makeHandler

			/** Client has called an Atom method.
			 * The implementation is here.
			 * By reflection, find out which method they have called, and
			 * do http-atom communication with the server.
			 * @param proxy Ignored: Atom proxies are stateless, so they have no identity.
			 * @param method create, delete, get, update
			 * @param args Arguments of the method call
			 * @return
			 * 	create: T,
			 * 	delete: void,
			 * 	get: List<T>,
			 * 	update: T
			 * @exception Throwable Whatever the Atom method threw.
			 * */
			@Override
			@SuppressWarnings("unchecked")
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable{

				//Must implement AtomProxy
				String methodName=method.getName();
				HttpURL url=getCategoryURL(beanClass);
				if("get".equals(methodName)){		//List<T> get(GDataParameters params);
					GDataParameters parameters=(GDataParameters)args[0];
					AtomBean beanInstance=beanClass.newInstance();//Must be bean: must have safe default constructor
					return get(url, beanInstance, parameters);
				}
				else if("insert".equals(methodName)){
					//T insert(T bean)
					if(args[0] instanceof AtomBean){
						AtomBean bean = (AtomBean)args[0];
						return insert(url, bean);
					}
					//List<T> insert(List<T>)
					else if(args[0] instanceof List<?>){
						List<AtomBean>beans=(List<AtomBean>)args[0];
						return insert(url, beans);
					}
					throw new NoSuchMethodException(methodName + args[0].getClass().getSimpleName());
				}
				else if("delete".equals(methodName)){	//void delete(String id, String etag);
					String id = args[0].toString();
					String etag=args[1].toString();
					delete(url, id, etag);
					return null;
				}
				else if("update".equals(methodName)){	//T update(T bean);
					AtomBean bean=(AtomBean)args[0];
					return update(url, bean);
				}
				else throw new NoSuchMethodException(methodName);
			}
		};
		return handler;
	}

	/** Insert a bean
	 * @param url The URL that received the POST
	 * @param bean
	 * @exception HttpException Look at status (=subclass) and message for more information.
	 * */
	private AtomBean insert(HttpURL url, AtomBean bean)	throws HttpException{
		HttpURLConnection con=null;
		try{
			url.setParameter("style", "full");//Want the response with the most detail possible
			Entry entry=bean.toEntry(true, Style.FULL);//send with the most detail possible. Exception
			
			//using java's HttpURLConnection
			con=openConnection(url);
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setChunkedStreamingMode(0);
			con.setRequestMethod("POST");
			
			//Problem: JBoss's implementation of http basic authentication with POST gives me an
			//empty request input stream! But with PUT it works fine. So here a workaround:
			//Simulate a POST by sending a PUT with a http header indicating the real desired
			//http method POST.
			//con.setRequestMethod("PUT");//ProtocolException
			//See http://code.google.com/apis/gdata/docs/2.0/basics.html.
			//con.setRequestProperty("X-HTTP-Method-Override", "POST");
			
			OutputStream out=con.getOutputStream();//IOException
			entry.write(out);//TransformerException
			out.close();//IOException

			InputStream in=con.getInputStream();//IOException 401
			entry=Entry.parse(in);
			in.close();
			bean=bean.fromEntry(entry);
			return bean;
		}
		catch(MalformedURLException mue){
			//bug
			logger.error("Bad URL: ", mue);
			throw new InternalServerErrorException(mue);
		}
		catch(ProtocolException pe){
			//bug
			logger.error("Bad protocol: ", pe);
			throw new InternalServerErrorException(pe);
		}
		catch(IOException ioe){
			//Another IOException: that's a normal communication problem.
			//See if we have a server response at all.
			try{
				int status = con.getResponseCode();
				String msg = con.getResponseMessage();
				logger.warn("HTTP response: " + status + " " + msg);
				HttpException he=HttpException.getInstance(status, msg);
				throw he;
			}catch(IOException ioe1){
				//We haven't event reached the server.
				logger.warn("Error in communication to server.", ioe);
				throw new InternalServerErrorException(ioe);
			}
		}
		catch(TransformerException te){
			//bug
			logger.error("Transformer exception: ", te);
			throw new InternalServerErrorException(te);
		}
		catch(Exception e){
			//bug
			logger.error("Exception: ", e);
			throw new InternalServerErrorException(e);
		}
		finally{
			if(con!=null)
				con.disconnect();
		}
	}

	/** Insert many beans.
	 * First implementation: send one request for each.
	 * Second implementation: send them in one batch.
	 * @param url The URL that received the POST
	 * @param beans
	 * @exception HttpException Look at status (=subclass) and message for more information.
	 * */
	private List<AtomBean> insert(HttpURL url, List<AtomBean> beans)throws HttpException{
		List<AtomBean>results=new ArrayList<AtomBean>();

		//First implementation: one request for each. First failure fails all, no transaction.
		/*
		for(AtomBean bean : beans){
			AtomBean result=this.insert(url, bean);//exception
			results.add(result);
		}
		*/

		//Second implementation: one request, the server has a transaction.
		HttpURLConnection con=null;
		try{
			url.setParameter("style", "full");//Want the response with the most detail possible.
			Feed feed=new Feed();
			//feed.setBatchOperation(INSERT);	//INSERT is the default. When we implement others, maybe we must set it explicitly.
			AtomBean dummy=null;//just keep hold of one bean, so that I can call fromEntry later.
			Entry entry=null;
			for(AtomBean bean : beans){
				dummy=bean;
				entry=bean.toEntry(true, Style.FULL);//Send with the most detail possible.
				feed.addEntry(entry);//This also updates the namespaces of the feed.
			}
			beans=null;//help gc
			logger.debug("POST " + url);

			//using java's HttpURLConnection
			con=openConnection(url);
			con.setRequestMethod("POST");
			
			//Problem: JBoss's implementation of http basic authentication with POST gives me an
			//empty request input stream! But with PUT it works fine. So here a workaround:
			//Simulate a POST by sending a PUT with a http header indicating the real desired
			//http method POST.
			//con.setRequestMethod("PUT");//ProtocolException
			//See http://code.google.com/apis/gdata/docs/2.0/basics.html.
			//con.setRequestProperty("X-HTTP-Method-Override", "POST");
			
			con.setDoOutput(true);
			con.setDoInput(true);
			OutputStream out=con.getOutputStream();//IOException
			feed.write(out);//TransformerException
			feed=null;//help gc
			out.close();//IOException

			//Everything has worked: read the response.
			InputStream in=con.getInputStream();//IOException 401
			logger.debug("HTTP response code: " + con.getResponseCode());
			Feed resultFeed=Feed.parse(in);
			in.close();
			List<Entry>entries=resultFeed.getEntries();
			resultFeed=null;//help gc
			for(Entry e : entries){
				//201 = entry.getBatchStatus()
				AtomBean bean=dummy.fromEntry(e);
				results.add(bean);
			}

		}
		catch(MalformedURLException mue){//bug
			logger.error("Bad URL: ", mue);
			throw new InternalServerErrorException(mue);
		}
		catch(ProtocolException pe){//bug
			logger.error("Bad protocol: ", pe);
			throw new InternalServerErrorException(pe);
		}
		catch(IOException ioe){
			//Another IOException: that's a normal communication problem.
			//See if we have a server response at all.
			try{
				int status=con.getResponseCode();
				String msg = con.getResponseMessage();
				logger.warn("HTTP response: " + status + " " + msg);
				HttpException he=HttpException.getInstance(status, msg);
				throw he;
			}catch(IOException ioe1){//We haven't event reached the server.
				logger.warn("Error in communication to server.", ioe);
				throw new InternalServerErrorException(ioe);
			}
		}
		catch(TransformerException te){//bug
			logger.error("Transformer exception: ", te);
			throw new InternalServerErrorException(te);
		}
		catch(Exception e){//bug
			logger.error("Exception: ", e);
			throw new InternalServerErrorException(e);
		}
		finally{
			if(con!=null)
				con.disconnect();
		}

		return results;
	}

	/**
	 * @param url
	 * @param id
	 * @param etag The ETag of the object that the client has seen.
	 * @exception HttpException Look at status code (=subclass) and message for more information
	 * */
	private void delete(HttpURL url, String id,String etag)throws HttpException{
		HttpURLConnection con=null;
		try{
			url.append("/entry").append(id);
			logger.debug("DELETE " + url + " - id: " + id + ", etag: " + etag);

			//using java's HttpURLConnection
			con=openConnection(url);//MalformedURLException, IOException
			etag=ETag.makeStrong(etag);
			con.setRequestProperty("If-Match", etag);
			con.setRequestMethod("DELETE");//ProtocolException

			//send the request
			con.getInputStream();//IOException 401
			logger.debug("HTTP response code: " + con.getResponseCode());//IOException
		}
		catch(MalformedURLException mue){
			//bug
			logger.error("Exception: ", mue);
			throw new InternalServerErrorException(mue);
		}
		catch(ProtocolException pe){
			//bug
			logger.error("Exception: ", pe);
			throw new InternalServerErrorException(pe);
		}
		catch(IOException ioe){
			//Another IOException: that's a normal communication problem.
			//See if we have a server response at all.
			try{
				int status = con.getResponseCode();
				String msg = con.getResponseMessage();
				logger.warn("HTTP response: " + status + " " + msg);
				HttpException he=HttpException.getInstance(status, msg);
				throw he;
			}catch(IOException ioe1){
				//We haven't even reached the server.
				logger.warn("Error in communication to server.", ioe);
				throw new InternalServerErrorException(ioe);
			}
		}
		finally{
			if(con!=null)
				con.disconnect();
		}
	}

	/**
	 * @param url
	 * @exception HttpException See status (=subclass) and message
	 * */
	private AtomBean update(HttpURL url, AtomBean bean)throws HttpException{
		HttpURLConnection con=null;
		try{
			url.append("/entry").append(bean.getId());
			url.setParameter("style", "full");//Want the response with the most detail possible
			Entry entry=bean.toEntry(true, Style.FULL);//Send with most detail possible. IOException
			logger.debug("PUT " + url + " - bean: " + bean);

			con=openConnection(url);
			String etag=bean.getETag();
			etag=ETag.makeStrong(etag);
			con.setRequestProperty("If-Match", etag);
			con.setRequestMethod("PUT");//ProtocolException
			con.setDoOutput(true);
			con.setDoInput(true);
			OutputStream out=con.getOutputStream();//IOException
			entry.write(out);//TransformerException
			out.close();//IOException

			InputStream in=con.getInputStream();//IOException 401
			logger.debug("HTTP response code: " + con.getResponseCode());//IOException
			entry=Entry.parse(in);
			bean=bean.fromEntry(entry);
			return bean;
		}
		catch(MalformedURLException mue){
			//bug
			logger.error("Exception: ", mue);
			throw new InternalServerErrorException(mue);
		}
		catch(ProtocolException pe){
			//bug
			logger.error("Exception: ", pe);
			throw new InternalServerErrorException(pe);
		}
		catch(IOException ioe){
			//Another IOException: that's a normal communication problem.
			//See if we have a server response at all.
			try{
				int status=con.getResponseCode();
				String msg=con.getResponseMessage();
				logger.warn("HTTP response: " + status + " " + msg);
				HttpException he=HttpException.getInstance(status, msg);
				throw he;
			}catch(IOException ioe1){
				//We haven't event reached the server.
				logger.warn("Error in communication to server.", ioe);
				throw new InternalServerErrorException(ioe);
			}
		}
		catch(TransformerException te){
			//bug
			logger.error("Transformer exception: ", te);
			throw new InternalServerErrorException(te);
		}
		catch(Exception e){
			//bug
			logger.error("Exception: ", e);
			throw new InternalServerErrorException(e);
		}
		finally{
			if(con!=null)
				con.disconnect();
		}
	}

	/**
	 * @param url
	 * @param bean A dummy instance of the right bean-class. The method needs it to call fromEntry on it.
	 * @param params
	 * @exception HttpException See status code (=subclass) and message for more information.
	 * */
	private List<AtomBean> get(HttpURL url, AtomBean bean, GDataParameters params)throws HttpException{
		HttpURLConnection con=null;
		try{
			String id=params.getId();
			if(id!=null)
				url.append("/entry" + id);
			url.setParameters(params);

			logger.debug("GET " + url);
			con=openConnection(url);
			con.setRequestMethod("GET");//ProtocolException

			InputStream in=con.getInputStream();//IOException 401
			logger.debug("HTTP response code: " + con.getResponseCode());//IOException
			Feed feed=Feed.parse(in);
			List<AtomBean> beans=new ArrayList<AtomBean>();
			for(Entry entry : feed.getEntries()){
				bean=bean.fromEntry(entry);
				beans.add(bean);
			}
			return beans;
		}
		catch(MalformedURLException mue){
			//bug
			logger.error("Exception: ", mue);
			throw new InternalServerErrorException(mue);
		}
		catch(ProtocolException pe){
			//bug
			logger.error("Exception: ", pe);
			throw new InternalServerErrorException(pe);
		}
		catch(IOException ioe){
			//Another IOException: that's a normal communication problem.
			//See if we have a server response at all.
			try{
				int status = con.getResponseCode();
				String msg = con.getResponseMessage();
				logger.warn("HTTP response: " + status + " " + msg);
				HttpException he=HttpException.getInstance(status, msg);
				throw he;
			}catch(IOException ioe1){
				//We haven't event reached the server.
				logger.warn("Error in communication to server.", ioe);
				throw new InternalServerErrorException(ioe);
			}
		}
		catch(Exception e){
			//bug
			logger.error("Exception: ", e);
			throw new InternalServerErrorException(e);
		}
		finally{
			if(con!=null)
				con.disconnect();
		}
	}

}