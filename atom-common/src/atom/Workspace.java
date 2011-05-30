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

import java.util.ArrayList;
import java.util.List;

/** A group of collections that are offered by an Atom server.
 * http://tools.ietf.org/html/rfc5023
 * */
public class Workspace {
	
	/** title of the workspace */
	private String title;
	
	/** The collections in this workspace. */
	private List<Collection> collections = new ArrayList<Collection>();

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/** Adds a collection.
	 * @param collection
	 * */
	public void addCollection(Collection collection){
		this.collections.add(collection);
	}

}