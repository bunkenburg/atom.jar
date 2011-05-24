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

/** A token in the AuthSub authentication protocol.
 * Could be one-time token or session token.
 * 
 * @author BARCELONA\alexanderb
 *
 */
public class Token {

	/** The value of the token, 256 bytes as UTF-8 String */
	private String value;
	
	/** Construct a token with value="".
	 * */
	Token(){this.value = "";}

	/** Construct a token with value. May be a one-time token or a session token.
	 * @param value 
	 * */
	public Token(String value){this.value = value;}

	/**
	 * @return the value
	 */
	public String getValue(){return value;}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/** for observation during development 
	 * @return string
	 * */
	public String toString(){
		String s = super.toString() + "{value=" + this.getValue() + " }";
		return s;
	}
}