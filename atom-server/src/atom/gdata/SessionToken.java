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

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author BARCELONA\alexanderb
 *
 */
public class SessionToken extends Token {

	/** format string for formatting expiration dates in http responses */
	private static final String PATTERN = "yyyyMMdd'T'HHmmss'Z'";
	
	/** expiration timestamp of the token */
	private Date expiration;
	
	/** Construct from value and expiration.
	 * @param value
	 * @param expiration
	 * */
	public SessionToken(String value, Date expiration){
		super(value);
		this.expiration = expiration;
	}

	/**
	 * @return the expiration
	 */
	Date getExpiration() {
		return expiration;
	}

	/** Gets the expiration, in format to be sent in http responses. 
	 * Format: "yyyyMMdd'T'HHmmss'Z'"
	 * @return the expiration
	 */
	String getExpirationString() {
		DateFormat df = new SimpleDateFormat(PATTERN);
		String s = df.format(this.expiration);
		return s;
	}

	/**
	 * @param expiration the expiration to set
	 */
	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
	
	/** for observation during development 
	 * @return string
	 * */
	public String toString(){
		String s = super.toString() + "{value=" + this.getValue() + ", expiration=" + this.getExpiration() + " }";
		return s;
	}
}