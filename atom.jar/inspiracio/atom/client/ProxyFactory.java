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