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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** dummy implementation of HttpServletRequest.
 * All methods are bad. Override the ones you need. */
public class DummyHttpServletRequest implements HttpServletRequest {

	public String getAuthType() {throw new RuntimeException("not implemented");}
	public String getContextPath() {throw new RuntimeException("not implemented");}
	public Cookie[] getCookies() {throw new RuntimeException("not implemented");}
	public long getDateHeader(String arg0) {throw new RuntimeException("not implemented");}
	public String getHeader(String arg0) {throw new RuntimeException("not implemented");}
	@Override public Enumeration<String> getHeaderNames() {throw new RuntimeException("not implemented");}
	@Override public Enumeration<String> getHeaders(String arg0) {throw new RuntimeException("not implemented");}
	public int getIntHeader(String arg0) {throw new RuntimeException("not implemented");}
	public String getMethod() {throw new RuntimeException("not implemented");}
	public String getPathInfo() {throw new RuntimeException("not implemented");}
	public String getPathTranslated() {throw new RuntimeException("not implemented");}
	public String getQueryString() {throw new RuntimeException("not implemented");}
	public String getRemoteUser() {throw new RuntimeException("not implemented");}
	public String getRequestURI() {throw new RuntimeException("not implemented");}
	public StringBuffer getRequestURL() {throw new RuntimeException("not implemented");}
	public String getRequestedSessionId() {throw new RuntimeException("not implemented");}
	public String getServletPath() {throw new RuntimeException("not implemented");}
	public HttpSession getSession() {throw new RuntimeException("not implemented");}
	public HttpSession getSession(boolean arg0) {throw new RuntimeException("not implemented");}
	public Principal getUserPrincipal() {throw new RuntimeException("not implemented");}
	public boolean isRequestedSessionIdFromCookie() {throw new RuntimeException("not implemented");}
	public boolean isRequestedSessionIdFromURL() {throw new RuntimeException("not implemented");}
	public boolean isRequestedSessionIdFromUrl() {throw new RuntimeException("not implemented");}
	public boolean isRequestedSessionIdValid() {throw new RuntimeException("not implemented");}
	public boolean isUserInRole(String arg0) {throw new RuntimeException("not implemented");}
	public Object getAttribute(String arg0) {throw new RuntimeException("not implemented");}
	@Override public Enumeration<String> getAttributeNames() {throw new RuntimeException("not implemented");}
	public String getCharacterEncoding() {throw new RuntimeException("not implemented");}
	public int getContentLength() {throw new RuntimeException("not implemented");}
	public String getContentType() {throw new RuntimeException("not implemented");}
	public ServletInputStream getInputStream() throws IOException {throw new RuntimeException("not implemented");}
	public String getLocalAddr() {throw new RuntimeException("not implemented");}
	public String getLocalName() {throw new RuntimeException("not implemented");}
	public int getLocalPort() {throw new RuntimeException("not implemented");}
	public Locale getLocale() {throw new RuntimeException("not implemented");}
	@Override public Enumeration<Locale> getLocales() {throw new RuntimeException("not implemented");}
	public String getParameter(String arg0) {throw new RuntimeException("not implemented");}
	@Override public Map<String,String> getParameterMap() {throw new RuntimeException("not implemented");}
	@Override public Enumeration<String> getParameterNames() {throw new RuntimeException("not implemented");}
	public String[] getParameterValues(String arg0) {throw new RuntimeException("not implemented");}
	public String getProtocol() {throw new RuntimeException("not implemented");}
	public BufferedReader getReader() throws IOException {throw new RuntimeException("not implemented");}
	public String getRealPath(String arg0) {throw new RuntimeException("not implemented");}
	public String getRemoteAddr() {throw new RuntimeException("not implemented");}
	public String getRemoteHost() {throw new RuntimeException("not implemented");}
	public int getRemotePort() {throw new RuntimeException("not implemented");}
	public RequestDispatcher getRequestDispatcher(String arg0) {throw new RuntimeException("not implemented");}
	public String getScheme() {throw new RuntimeException("not implemented");}
	public String getServerName() {throw new RuntimeException("not implemented");}
	public int getServerPort() {throw new RuntimeException("not implemented");}
	public boolean isSecure() {throw new RuntimeException("not implemented");}
	public void removeAttribute(String arg0) {throw new RuntimeException("not implemented");}
	public void setAttribute(String arg0, Object arg1) {throw new RuntimeException("not implemented");}
	public void setCharacterEncoding(String arg0)throws UnsupportedEncodingException {throw new RuntimeException("not implemented");}

}