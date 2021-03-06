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
package inspiracio.atom;

import inspiracio.servlet.http.BadRequestException;
import inspiracio.servlet.http.InternalServerErrorException;
import inspiracio.servlet.jsp.PageContextFactory;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import atom.Category;
import atom.gdata.GDataURL;

/** Access to AtomSAOs. 
 * The URL must have exactly one category, and that category identifies the SAO. */
class AtomSAOFactory{
	
	/** The prefix for packages of Atom beans.
	 * For example, if it's "com.siine", and the category is "user", 
	 * then the bean class is "com.siine.user.User". */
	static String BEAN_PACKAGE_PREFIX;
	static void setBeanPackage(String beanPackage){
		AtomSAOFactory.BEAN_PACKAGE_PREFIX=beanPackage;
	}

	/** For given URL return the SAO. 
	 * This implementation tries to guess the class name and instantiates.
	 * A different implementation could keep a cache of singletons. */
	static AtomSAO<AtomBean> get(GDataURL url)throws BadRequestException,InternalServerErrorException{
		//Identify the SAO
		List<Category> categories=url.getCategories();
		if(categories.size()!=1)
			throw new BadRequestException();//URL incorrect for Atom protocol
		Category category=categories.get(0);
		String term=category.getTerm();
		String Term=Character.toUpperCase(term.charAt(0)) + term.substring(1);
		String className=BEAN_PACKAGE_PREFIX + "." + term + "." + Term + "AtomSAO";
		try{
			Class<?> clazz=Class.forName(className);//ClassNotFoundException If you can't find the class, think about class loaders!
			Object o=clazz.newInstance();//IllegalAccessException, InstantiationException
			@SuppressWarnings("unchecked")
			AtomSAO<AtomBean> sao=(AtomSAO<AtomBean>)o;
			
			//Tell the SAO who is authenticated.
			HttpServletRequest request=(HttpServletRequest)PageContextFactory.getPageContext().getRequest();
			Principal principal=request.getUserPrincipal();
			sao.setCallerPrincipal(principal);

			return sao;
		}catch(ClassNotFoundException cnfe){
			throw new InternalServerErrorException(cnfe);
		}catch(IllegalAccessException iae){
			throw new BadRequestException();
		}catch(InstantiationException iae){
			throw new BadRequestException();
		}
	}
}