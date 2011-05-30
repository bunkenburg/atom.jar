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
package atom.gdata;

/** Our specific additional query parameter "style" that determines
 * the amount of data in the response. Similar to the style of
 * java.text.DateFormat.
 * */
public enum Style{
	
	/** plain style: only standard Atom-elements */
	PLAIN, 
	
	/** short style:
	 * Good for FAST searches. Enough information for 
	 * a normal human client */
	SHORT, 
	
	/** long style: 
	 * Slow queries */
	LONG, 
	
	/** full queries: all fields that we want to make
	 * public in API */
	FULL;
	
	/** Parse a style. Maps null to PLAIN. The others must match
	 * case-sensitive.
	 * @param style as String 
	 * @return Style.constant */
	public static Style parseStyle(String style){
		if (style==null || style.equals("plain")) return PLAIN;
		if (style.equals("short")) return SHORT;
		if (style.equals("long")) return LONG;
		if (style.equals("full")) return FULL;
		throw new RuntimeException("no such style: " + style);
	}
	
	/** Nice String representation: "plain", "short", etc.
	 * The opposite of parseStyle. 
	 * @return String 
	 * */
	@Override public String toString(){
		if (this==PLAIN) return "plain";
		if (this==SHORT) return "short";
		if (this==LONG) return "long";
		if (this==FULL) return "full";
		return null;
	}
	
}