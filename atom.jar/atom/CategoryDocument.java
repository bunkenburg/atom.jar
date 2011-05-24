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
package atom;

import java.util.List;

/** Category documents according to http://tools.ietf.org/html/rfc5023.
 * <p>
 * Category Documents contain lists of categories described using the
 * "atom:category" element from the Atom Syndication Format [RFC4287].
 * Categories can also appear in Service Documents, where they indicate
 * the categories allowed in a Collection (see Section 8.3.6).
 * </p>
 * <p>
 * Category Documents are identified with the "application/atom+xml"
 * media type (see Section 16.1).
 * */
public class CategoryDocument {

	private List<Category> categories;
	
	/** Add a category 
	 * @param category
	 * */
	public void addCategory(Category category){
		this.categories.add(category);
	}
	
	/** Get the categories.
	 * @return List
	 * */
	public List<Category> getCategories(){
		return this.categories;
	}
}