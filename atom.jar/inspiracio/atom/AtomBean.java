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

import atom.Entry;
import atom.gdata.Style;

/** A business object that can be visible to clients of Atom API.
 * This interface obliges beans to have translations from and to entries.
 * <p>
 * We have no interface inspiracio.portal.Bean; if we
 * had it, then this interface would extend it.
 * </p>
 * */
public interface AtomBean{

	//State ------------------------------------------------------------------------------

	/** Returns the key that we use to store the bean in the session.
	 * For example "form-12". */
	String getKey();
	
	/** Business objects used as Atom entries must have ID.
	 * The return type must be something that has an easy toString().
	 * Best to use String or Integer or similar. */
	Object getId();

	/** Gets an ETag value that represents the version of this business object.
	 * Whenever the business object is changed, the ETag must also change.
	 * The ETag is strong (see ETag spec in HTTP 1.1 spec).
	 * A strong ETag value is a String enclosed in double quotes and inside the
	 * String the quotes are escaped; but this method returns just the String.
	 * The clients of the method take care of quoting and escaping. */
	String getETag();

	/** Sets ETag. Set a String that represents the version of this object.
	 * Must change every time the object changes. Any String is okay:
	 * clients takes care of escaping and quoting. */
	void setETag(String etag);

	//Translation to and from entries -----------------------------------------------------

	/** Convert this bean into an Atom entry.
	 * If the Bean supports style, use plain style.
	 * @return Entry
	 * */
	Entry toEntry() throws Exception;

	/** Convert this bean into an Atom entry.
	 * @param root Will the entry be the root element of an XML document?
	 * 	If so, toEntry should set any namespace attributes that are necessary to
	 * 	define the XML elements that come with the chosen style.
	 * @param style
	 * @return Entry
	 * @exception Exception Can I refine this?
	 * */
	Entry toEntry(boolean root, Style style) throws Exception;

	/** Convert the entry into a new bean.
	 * <p>
	 * Calling this method does not affect the
	 * instance on which it is called. It is as if this
	 * method is a constructor or a static method.
	 * I don't use constructors or static methods
	 * because they don't participate in inheritance.
	 * @param entry
	 * @return fresh bean instance
	 * */
	AtomBean fromEntry(Entry entry);

	/** Sets the object as clean (<code>clean = true;</code>) */
	public void setClean();

}