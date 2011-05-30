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
package inspiracio.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/** Makes an enumeration from a list.
 * Some servlet API needs enumerations. */
public class ListEnumeration<E> implements Enumeration<E> {

	// State ------------------------------------
	
	private Iterator<E>iterator;
	
	//Constructor ------------------------------
	
	public ListEnumeration(List<E> list){
		this.iterator=list.iterator();
	}
	
	//Methods ----------------------------------
	
	@Override public boolean hasMoreElements() {
		return this.iterator.hasNext();
	}

	@Override public E nextElement() {
		return this.iterator.next();
	}

}