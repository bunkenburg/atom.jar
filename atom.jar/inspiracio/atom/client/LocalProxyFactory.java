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