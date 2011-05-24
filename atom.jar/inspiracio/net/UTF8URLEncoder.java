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
package inspiracio.net;

import inspiracio.lang.StringBuilderUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;


/** Like java.net.URLEncoder, but always uses UTF-8 and
 * does not throw EncodingUnsupportedException. 
 * <p>
 * Encodes according to application/x-www-form-urlencoded MIME format.
 * UTF-8 is always supported and should always be used.
 */
public class UTF8URLEncoder {

	/** Returns URL-decoded form of a String, using char-encoding UTF-8.
	 * @param s 
	 * @return URL-encoded form */
	public static String decode(String s){
		try {
			String d = URLDecoder.decode(s, "UTF-8");
			return d;
		} catch (UnsupportedEncodingException e){
			//Never, UTF-8 is always supported
			throw new RuntimeException(e);
		}
	}
	
	/** Returns URL-encoded form of a String, using char-encoding UTF-8.
	 * @param s 
	 * @return URL-encoded form */
	public static String encode(String s){
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e){
			//Never, UTF-8 is always supported
			throw new RuntimeException(e);
		}
	}
	
	/** Returns a String that is similar to the input-String
	 * but is ok to put in semantic URLs. The method works for languages that 
	 * are mainly Latin + diacritics, It does not work for other languages.
	 * <p>
	 * The result is obtained like this:
	 * <ol>
	 * 	<li>change all chars to lowercase (because many users don't distinguish uppercase and lowercase)
	 * 	<li>remove diacritical marks from these characters: "áàäçéèëíìïóòöúùüñ"
	 * 	<li>substitute all chars by '-' except; a-z, 0-9, dot, hyphen, underscore.
	 * 	<li>substitute multiple hyphens by single hyphen
	 * 	<li>remove initial hyphen and final hyphen
	 * 	<li>if nothing survives, return "-".
	 * </ol>
	 * See URL spec in http://www.ietf.org/rfc/rfc2396.txt.
	 * See trac http://bcnserver/trac/area-tecnica/ticket/3819.
	 * See class java.net.IDN for an alternative approach.
	 * @param s 
	 * @return URL-encoded form */
	public static String encodeSemantic(String s){
		StringBuilder result=new StringBuilder(s);

		//lowercase and get rid of dangerous chars
		final int N=result.length();
		for (int i=0; i<N; i++){
			char c=result.charAt(i);
			c=semanticChar(c);
			result.setCharAt(i, c);
		}
		//Assert: all chars are safe (URLEncoded)
		
		//The following operations may change the size of the
		//buffer, but it is unlikely that they do it a lot.
		
		//remove hyphens from the start
		while (0<result.length() && result.charAt(0)=='-'){
			result.deleteCharAt(0);
		}
		
		//multiple hyphens to single hyphen
		while (0 < StringBuilderUtils.replaceAll(result, "--", "-")){
			//try the condition again
		}
		
		//remove final hyphen
		while (0<result.length() && result.charAt(result.length()-1)=='-'){
			result.deleteCharAt(result.length()-1);
		}
		
		//Maybe we have eliminated everything?
		if(result.length()==0) {
			result.append('-');
		}
		
		String resultString=result.toString();
		return resultString;
	}//encodeSemantic
	
	/** For a given char, return the corresponding char that can go into a
	 * semantic URL.
	 * @param c0 char to check
	 * @return c the corresponding semantic char
	 */
	private static char semanticChar(char c0) {
		char c = c0;

		// lower case
		if (Character.isUpperCase(c)) {
			c = Character.toLowerCase(c);
		}

		// remove diacritics
		int diacritic = CHARS_NO.indexOf(c);
		if (0 <= diacritic) {
			c = CHARS_SI.charAt(diacritic);
		}

		// unsafe chars --> '-'
		boolean safe = isSafe(c);
		if (!safe) {
			c = '-';
		}

		return c;
	}
	
	/** Chars that must be replaced by the corresponding char in CHARS_SI.
	 * Only lower case chars are relevant.
	 * Add here diacritic chars and similar stuff.
	 * If a char is missing here, it will be treated as unsafe. */
	private static final String CHARS_NO = "áàäçéèëíìïóòöúùüñ";
	
	/** Chars that are the replacements */
	private static final String CHARS_SI = "aaaceeeiiiooouuun";

	/** True if this character is safe in URL-encoding. 
	 * According to application/x-www-form-urlencoded MIME format. 
	 * Permits:
	 * <ul>
	 * 	<li>a-z
	 * 	<li>A-Z
	 * 	<li>0-9
	 * 	<li>.-_ dot hyphen underscore
	 * </ul>
	 * Does not accept * asterisk, although it is okay for a URL
	 * (see http://www.ietf.org/rfc/rfc2396.txt), where it is classified as
	 * mark. But the trac http://bcnserver/trac/area-tecnica/ticket/3819
	 * prefers to exclude it to adapt to habits of the online survey industry.
	 * @param c 
	 * @return boolean */
	public static boolean isSafe(char c){
		return
			('a' <= c && c <= 'z') ||
			('A' <= c && c <= 'Z') ||
			('0' <= c && c <= '9') ||
			c=='.' || c=='-' || c=='_';
	}
	
	/** testing only 
	 * @param args0 */
	public static void main(String[] args0){
		String[] args = args0;
		if (args.length==0){
			args = new String[]{
					"El Corte Inglés S. A.", 
					"", 
					"!$%&/()Oracle España",
					"----",
					"\"complete\"",
					"\"filter out"
			};
		}
		say("hi");
		for (int i=0; i<args.length; i++){
			String s = args[i];
			String t = encodeSemantic(s);
			String u = encode(s);
			say(s + " --semantic-> " + t);
			say(s + " --encode---> " + u);
		}
		say("bye");
	}
	
	private static void say(Object o){
		System.out.println(o);
	}
}