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

import inspiracio.atom.AbstractAtomSAO;
import inspiracio.atom.AtomBean;
import inspiracio.atom.AtomSAO;
import inspiracio.lang.NotImplementedException;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.PreconditionFailedException;

import java.security.Principal;
import java.util.List;

import atom.gdata.GDataParameters;
import atom.gdata.GDataURL;

/** A local Atom proxy, one that connects the interface AtomProxy 
 * directly to AtomSAO, with normal direct java method calls within
 * the same JVM.
 * <p>
 * The implementation may be call-by-reference, whereas the remote
 * proxies are call-by-value. Clients should be programmed in a way
 * so that they are independent of this difference.
 * */
abstract class LocalAtomProxy<T extends AtomBean> implements AtomProxy<T>{
	
	//State ----------------------------------------------------
	
	/** Calls are delegated to this SAO. */
	private AtomSAO<T> sao;;
	
	//Constructors ---------------------------------------------
	
	/** Make a proxy for this bean class.
	 * @param beanClass
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * */
	@SuppressWarnings("unchecked")
	LocalAtomProxy(Class<T> beanClass) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		//Get AtomSAO<beanClass>
		//beanClass is something like com.siine.user.User
		//The SAO will something like com.siine.user.UserAtomSAO
		String beanClassName=beanClass.getName();
		String saoClassName=beanClassName+"AtomSAO";			
		Class<?> clazz=Class.forName(saoClassName);//ClassNotFoundException If you can't find the class, think about class loaders!
		Object o=clazz.newInstance();//IllegalAccessException, InstantiationException
		this.sao=(AtomSAO<T>)o;
	}
	
	//Methods --------------------------------------------------
	
	/** Delegates to the SAO. */
	@Override public void delete(Object id, String etag)throws PreconditionFailedException, HttpException{
		//Tell the SAO who the authenticated user is
		Principal p=this.getCallerPrincipal();
		sao.setCallerPrincipal(p);
		
		String i=id.toString();
		this.sao.delete(i, etag);
	}

	/** Delegates to the SAO. 
	 * @param params
	 * */
	@Override public List<T> get(GDataParameters params) throws HttpException{
		//Make url with all parameters
		GDataURL url=this.getURL();
		String id=params.getId();
		if(id!=null)
			url.append("/entry" + id);
		url.setParameters(params);

		//Tell the SAO who the authenticated user is
		Principal p=this.getCallerPrincipal();
		sao.setCallerPrincipal(p);
		
		//If the sao is an AbstractAtomSAO, we get the bean directly, no conversion to entries at all.
		if(sao instanceof AbstractAtomSAO<?>){
			AbstractAtomSAO<T> aa=(AbstractAtomSAO<T>)sao;
			List<T> beans=aa.get(url);
			return beans;
		}else{
			//Could go from beans to entries and back to beans.
			//But let's try to avoid that.
			throw new NotImplementedException();
		}
	}

	/** Delegates to the SAO. 
	 * @param beans to insert
	 * @return inserted beans
	 * */
	@Override public List<T> insert(List<T> beans) throws HttpException{
		//Tell the SAO who the authenticated user is
		Principal p=this.getCallerPrincipal();
		sao.setCallerPrincipal(p);
		
		throw new NotImplementedException();
	}

	/** Delegates to the SAO. 
	 * @param bean to insert
	 * @return inserted bean
	 * */
	@Override public T insert(T bean) throws HttpException{
		//Tell the SAO who the authenticated user is
		Principal p=this.getCallerPrincipal();
		sao.setCallerPrincipal(p);
		
		GDataURL url=this.getURL();
		String slug=null;
		T inserted=this.sao.insert(url, bean, slug);
		return inserted;
	}

	/** Delegates to the SAO. 
	 * @param bean to update
	 * @return updated bean
	 * */
	@Override public T update(T bean) throws PreconditionFailedException, HttpException{
		//Tell the SAO who the authenticated user is
		Principal p=this.getCallerPrincipal();
		sao.setCallerPrincipal(p);
		
		T updated=this.sao.update(bean);
		return updated;
	}

	//Helpers ---------------------------------------------------
	
	/** LocalAtomProxyFactory gives implementation. */
	abstract protected Principal getCallerPrincipal();
	
	/** Every call returns a fresh instance of a GDataURL. 
	 * Communication is local, so we don't really need the URL,
	 * we just need it to add the parameters to it, and the 
	 * parameters are different every time.
	 * @return like "http://domain/atom/-/dummy" no trailing "/"
	 * */
	private GDataURL getURL(){
		String s="http://domain/atom/-/dummy";//Dummy: scheme, domain, and even category are dummies.
		GDataURL url=new GDataURL(s);
		return url;
	}
}
