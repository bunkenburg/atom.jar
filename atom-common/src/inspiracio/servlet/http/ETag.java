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
package inspiracio.servlet.http;

/** Methods for managing ETag. See HTTP 1.1 spec. */
public class ETag {

	/** no instantiation */
	private ETag(){}
	
	/** From an arbitrary String, make a strong ETag.
	 * Escapes and double-quotes. */
	public static String makeStrong(String s){
		s=s.replaceAll("\"", "\\\"");//escape " to \"
		s='"' + s + '"';//enclose in "
		return s;
	}

	/** From a strong ETag, gets the String.
	 * Unquotes and unescapes. */
	public static String parseStrong(String s){
		s=s.substring(1, s.length()-1);//unquote
		s=s.replaceAll("\\\"", "\"");//unescape \" to "
		return s;
	}
}