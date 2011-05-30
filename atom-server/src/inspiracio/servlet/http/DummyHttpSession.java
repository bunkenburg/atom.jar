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

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/** All methods not implemented. */
@SuppressWarnings("deprecation")
public class DummyHttpSession implements HttpSession{

	public Object getAttribute(String arg0) {throw new RuntimeException("not implemented");}
	@Override public Enumeration<String> getAttributeNames() {throw new RuntimeException("not implemented");}
	public long getCreationTime() {throw new RuntimeException("not implemented");}
	public String getId() {throw new RuntimeException("not implemented");}
	public long getLastAccessedTime() {throw new RuntimeException("not implemented");}
	public int getMaxInactiveInterval() {throw new RuntimeException("not implemented");}
	public ServletContext getServletContext() {throw new RuntimeException("not implemented");}
	@Deprecated public HttpSessionContext getSessionContext() {throw new RuntimeException("not implemented");}
	public Object getValue(String arg0) {throw new RuntimeException("not implemented");}
	public String[] getValueNames() {throw new RuntimeException("not implemented");}
	public void invalidate() {throw new RuntimeException("not implemented");}
	public boolean isNew() {throw new RuntimeException("not implemented");}
	public void putValue(String arg0, Object arg1) {throw new RuntimeException("not implemented");}
	public void removeAttribute(String arg0) {throw new RuntimeException("not implemented");}
	public void removeValue(String arg0) {throw new RuntimeException("not implemented");}
	public void setAttribute(String arg0, Object arg1) {throw new RuntimeException("not implemented");}
	public void setMaxInactiveInterval(int arg0) {throw new RuntimeException("not implemented");}

}