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

import java.security.Principal;

/** Factory for Atom proxies that communicate inside the same JVM by 
 * normal java method calls. */
public class LocalProxyFactory implements ProxyFactory{

	//State --------------------------------------------------------------
	
	/** All requests run with this authenticated user, or null if none is authenticated. */
	private Principal principal;

	//Constructor --------------------------------------------------------

	/** Makes a fresh factory for local proxies. */
	public LocalProxyFactory(){}

	//Methods ------------------------------------------------------------

	/** Given an AtomBean-class, delivers an AtomProxy for it. 
	 * @param beanClass The Class identifying which beans the proxy should treat.
	 * @return a fresh proxy instance
	 * */
	@SuppressWarnings("unchecked")
	@Override public <T extends AtomBean> LocalAtomProxy<T> get(Class<T> beanClass){
		LocalAtomProxy<T> proxy;
		try{
			proxy=new LocalAtomProxy(beanClass){
				@Override protected Principal getCallerPrincipal(){
					return LocalProxyFactory.this.principal;
				}
			};
		}catch(ClassNotFoundException e){
			throw new RuntimeException(e);
		}catch (InstantiationException e){
			throw new RuntimeException(e);
		}catch (IllegalAccessException e){
			throw new RuntimeException(e);
		}
		return proxy;
	}

	/** Sets the logged-in user principal. 
	 * All subsequent Atom requests to proxies made by this factory will be made with
	 * this principal.
	 * 
	 * Puts the principal in the PrincipalFactory.
	 * 
	 * @param principal
	 * */
	public void setCallerPrincipal(Principal principal){
		this.principal=principal;
	}

	//Helpers ---------------------------------------------------------------
}