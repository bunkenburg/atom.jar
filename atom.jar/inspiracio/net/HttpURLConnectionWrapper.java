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
package inspiracio.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.util.List;
import java.util.Map;

/** A wrapper for HttpURLConnection. */
class HttpURLConnectionWrapper extends HttpURLConnection {

	//State ----------------------------------------------
	
	private HttpURLConnection con;
	
	//Constructor ----------------------------------------
	
	protected HttpURLConnectionWrapper(HttpURLConnection con){
		super(con.getURL());
		this.con=con;
	}
	
	//Methods --------------------------------------------
	
	@Override public InputStream getErrorStream(){return con.getErrorStream();}
	@Override public String getHeaderField(int n){return con.getHeaderField(n);}
	@Override public long getHeaderFieldDate(String name, long Default) {return con.getHeaderFieldDate(name, Default);}
	@Override public String getHeaderFieldKey(int n){return con.getHeaderFieldKey(n);}
	@Override public boolean getInstanceFollowRedirects(){return con.getInstanceFollowRedirects();}
	@Override public Permission getPermission() throws IOException{return con.getPermission();}
	@Override public String getRequestMethod(){return con.getRequestMethod();}
	@Override public int getResponseCode() throws IOException {return con.getResponseCode();}
	@Override public String getResponseMessage() throws IOException {return con.getResponseMessage();}
	@Override public void setChunkedStreamingMode(int chunklen) {con.setChunkedStreamingMode(chunklen);}
	@Override public void setFixedLengthStreamingMode(int contentLength) {con.setFixedLengthStreamingMode(contentLength);}
	@Override public void setInstanceFollowRedirects(boolean followRedirects) {con.setInstanceFollowRedirects(followRedirects);}
	@Override public void setRequestMethod(String method)throws ProtocolException{con.setRequestMethod(method);}
	@Override public void addRequestProperty(String key, String value) {con.addRequestProperty(key, value);}
	@Override public boolean getAllowUserInteraction() {return con.getAllowUserInteraction();}
	@Override public int getConnectTimeout() {return con.getConnectTimeout();}
	@Override public Object getContent() throws IOException {return con.getContent();}
	@Override public Object getContent(@SuppressWarnings("rawtypes") Class[] classes) throws IOException {return con.getContent(classes);}
	@Override public String getContentEncoding(){return con.getContentEncoding();}
	@Override public int getContentLength() {return con.getContentLength();}
	@Override public String getContentType() {return con.getContentType();}
	@Override public long getDate() {return con.getDate();}
	@Override public boolean getDefaultUseCaches() {return con.getDefaultUseCaches();}
	@Override public boolean getDoInput() {return con.getDoInput();}
	@Override public boolean getDoOutput() {return con.getDoOutput();}
	@Override public long getExpiration() {return con.getExpiration();}
	@Override public String getHeaderField(String name) {return con.getHeaderField(name);}
	@Override public int getHeaderFieldInt(String name, int Default){return con.getHeaderFieldInt(name, Default);}
	@Override public Map<String, List<String>> getHeaderFields(){return con.getHeaderFields();}
	@Override public long getIfModifiedSince(){return con.getIfModifiedSince();}
	@Override public InputStream getInputStream() throws IOException{return con.getInputStream();}
	@Override public long getLastModified(){return con.getLastModified();}
	@Override public OutputStream getOutputStream() throws IOException{return con.getOutputStream();}
	@Override public int getReadTimeout(){return con.getReadTimeout();}
	@Override public Map<String, List<String>> getRequestProperties() {return con.getRequestProperties();}
	@Override public String getRequestProperty(String key){return con.getRequestProperty(key);}
	@Override public URL getURL(){return con.getURL();}
	@Override public boolean getUseCaches(){return con.getUseCaches();}
	@Override public void setAllowUserInteraction(boolean allowuserinteraction){con.setAllowUserInteraction(allowuserinteraction);}
	@Override public void setConnectTimeout(int timeout){con.setConnectTimeout(timeout);}
	@Override public void setDefaultUseCaches(boolean defaultusecaches){con.setDefaultUseCaches(defaultusecaches);}
	@Override public void setDoInput(boolean doinput){con.setDoInput(doinput);}
	@Override public void setDoOutput(boolean dooutput){con.setDoOutput(dooutput);}
	@Override public void setIfModifiedSince(long ifmodifiedsince){con.setIfModifiedSince(ifmodifiedsince);}
	@Override public void setReadTimeout(int timeout){con.setReadTimeout(timeout);}
	@Override public void setRequestProperty(String key, String value){con.setRequestProperty(key, value);}
	@Override public void setUseCaches(boolean usecaches){con.setUseCaches(usecaches);}
	@Override public String toString(){return con.toString();}
	@Override public void disconnect(){con.disconnect();}
	@Override public boolean usingProxy(){return con.usingProxy();}
	@Override public void connect() throws IOException{con.connect();}
}