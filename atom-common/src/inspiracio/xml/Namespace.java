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
package inspiracio.xml;


/** XML namespace.
 * Immutable. */
public class Namespace {

	//State ----------------------------------------
	
	/** The prefix for the namespace, may be "". */
	private String prefix;
	
	/** The URL that identifies this namespace. */
	private String url;
	
	/** Optionally, the location of the XML schema. */
	private String schemaLocation;
	
	//Constructors ---------------------------------
	
	public Namespace(String prefix, String url, String schemaLocation){
		this.prefix=prefix;
		this.url=url;
		this.schemaLocation=schemaLocation;
	}
	
	//Accessors -------------------------------------
	
	public String getPrefix(){return prefix;}
	public String getURL(){return url;}
	public String getSchemaLocation(){return schemaLocation;}
	
}