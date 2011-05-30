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

import java.security.Principal;

import inspiracio.atom.AtomBean;

/** A factory that can make Atom proxies. 
 * 
 * Implementations: 
 * 
 * RemoteProxyFactory makes proxies that send XML over http,
 * 
 * LocalProxyFactory makes java methods calls in the same JVM.
 * */
public interface ProxyFactory{

	/** Given an AtomBean-class, delivers an AtomProxy for it. */
	public <T extends AtomBean> AtomProxy<T> get(Class<T> beanClass);

	/** Sets the logged-in user principal. 
	 * What this exactly means depends on the implementing subclass.
	 * */
	public void setCallerPrincipal(Principal principal);

}