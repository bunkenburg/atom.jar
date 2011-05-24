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

/** A category according to Atom 1.0 spec at http://atompub.org/rfc4287.html.
 * */
public class Category {

	/** The term of the category, for example "offer". Obligatory. */
	private String term;
	
	/** Not obligatory. Atom URI  */
	private String scheme;
	
	/** Not obligatory. */
	private String label;
	
	/**
	 * @return the label
	 */
	String getLabel() {
		return label;
	}
	
	/**
	 * @param label the label to set
	 */
	void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * @return the scheme
	 */
	String getScheme() {
		return scheme;
	}
	
	/** 
	 * @param scheme the scheme to set
	 */
	void setScheme(String scheme) {
		this.scheme = scheme;
	}
	
	/** The term of the category. This is the important part!
	 * Example: "offer".
	 * @return the term
	 */
	public String getTerm(){return term;}
	
	/** The term of the category. This is the important part!
	 * Example: "offer".
	 * @param term the term to set
	 */
	public void setTerm(String term) {
		this.term = term;
	}
	
	/** for observation during debug 
	 * @return String representation */
	public String toString(){
		String s = "atom.Category[" + this.getTerm() + "]";
		return s;
	}
	
}