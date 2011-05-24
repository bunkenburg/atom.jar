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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Helpers for manipulating Strings with XML content. */
public class XMLString {

	/** Char: - short hyphen */
	private static final int C_SHORT_HYPHEN = 8211;

	/** Char: -- long hyphen */
	private static final int C_LONG_HYPHEN = 8212;
	
	/** Char: ' left quote*/
	private static final int C_LEFT_SIMPLE_QUOTE = 8216;
	
	/** Char: ' right quote */
	private static final int C_RIGHT_SIMPLE_QUOTE = 8217;
	
	/** Char: " left double quote */
	private static final int C_LEFT_DOUBLE_QUOTE = 8220;
	
	/** Char: " right double quote */
	private static final int C_RIGHT_DOUBLE_QUOTE = 8221;
	
	/** Char: " double quote */
	private static final int C_DOUBLE_QUOTE = 34;
	
	/** Char: ' simple quote */
	private static final int C_SIMPLE_QUOTE = 39;
	
	/** Char: - hyphen */
	private static final int C_HYPHEN = 45;
	
	/** Parses the string passed as parameter to substitute special characters
	 * that are not defined in the standard ISO-8859-1 to be enabled to be used
	 * in an xml document
	 * @param in String to be parsed
	 * @return The same string passed as parameter 
	 */
	public static String stripSmartQuotes(String in) {
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			
			switch (c) {
			case C_SHORT_HYPHEN:
			case C_LONG_HYPHEN:
				sb.append((char)C_HYPHEN);
				break;
			case C_LEFT_DOUBLE_QUOTE:
			case C_RIGHT_DOUBLE_QUOTE:
				sb.append((char)C_DOUBLE_QUOTE);
				break;
			case C_LEFT_SIMPLE_QUOTE:
			case C_RIGHT_SIMPLE_QUOTE:
				sb.append((char)C_SIMPLE_QUOTE);
				break;
			default:
				sb.append(c);
			}				
		}
		
		return sb.toString();
	}
	
	
	/** Removes XML-style comments from a String.
	 * Removes all occurrences of "&lt;!-- something --&gt;". 
	 * @param xml String that may contain XML-comments
	 * @return the same String without XML-comments
	 * */
	public static String stripComments(String xml){
		//".*?" matches zero or many characters, reluctantly,
		//which means the first occurrences of "-->" terminates the comment.
		final String regex = "<!--.*?-->";
		final String repl = "";
		//Use DOTALL: . also matches line break, 
		//so that XML comments can be one line or multi-line.
		final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(xml);
		return matcher.replaceAll(repl);
	}
	
	/** Escapes a String for inserting in XML elements.
	 * Assume thats the encoding of the XML is UTF-8 and
	 * does not escape accents and so on.
	 * An exact re-implementation of ColdFusion's HTMLEdit function,
	 * but independent of coldfusion-classes.
	 * In detail, does:
	 * <table border="1">
	 * <tr><th>int</th><th>char</th><th>escape(c)</th><th>escape(c).length()</th></tr>
	 * <tr><td>13</td><td>CR</td><td>[empty String]</td><td>0</td></tr>
	 * <tr><td>34</td><td>"</td><td>&amp;quot;</td><td>6</td></tr>
	 * <tr><td>38</td><td>&</td><td>&amp;amp;</td><td>5</td></tr>
	 * <tr><td>60</td><td><</td><td>&amp;lt;</td><td>4</td></tr>
	 * <tr><td>62</td><td>></td><td>&amp;gt;</td><td>4</td></tr>
	 * </table>
	 * @param s The string to escape.
	 * @return The string with the replacements done.
	 */
	public static String escape(String s){
		if(s==null)return null;
		StringBuilder buffer = new StringBuilder();
		for (int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			switch (c){
			case 13: /* append "" to buffer */ break;
			case 34: buffer.append("&quot;"); break;//34 = 32 + 2 = 100010 = x12
			case 38: buffer.append("&amp;"); break;
			case 60: buffer.append("&lt;"); break;
			case 62: buffer.append("&gt;"); break;
			default: buffer.append(c);
			}
		}//for
		return buffer.toString();
	}

	/** Unescapes a String that comes from XML to get back the original string.
	 * The inverse function of escape(String): String.
	 * <p>
	 * In detail, does:
	 * <table border="1">
	 * <tr><th>s</th><th>unescape(c)</th></tr>
	 * <tr><td>&amp;quot;</td><td>"</td></tr>
	 * <tr><td>&amp;amp;</td><td>&amp;</td></tr>
	 * <tr><td>&amp;lt;</td><td><</td></tr>
	 * <tr><td>&amp;gt;</td><td>></td></tr>
	 * </table>
	 * Maybe there is some XML-API function that improves and generalises this,
	 * and replaces all entities.
	 * @param s The string to unescape.
	 * @return The string with the replacements done.
	 */
	public static String unescape(String s){
		//Can be optimised
		/*
		if(s==null)return null;
		s=s.replaceAll("&quot;", "\"");
		s=s.replaceAll("&amp;", "&");
		s=s.replaceAll("&lt;", "<");
		s=s.replaceAll("&gt;", ">");
		return s;
		*/
		
		if(s==null)return null;
		StringBuilder buffer = new StringBuilder();
		for (int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			switch (c){
			case '&':
				if(s.startsWith("&quot;", i)){
					buffer.append('"');
					i=i+"&quot;".length()-1;//Can this change the variable that controls the loop?
				}
				else if(s.startsWith("&amp;", i)){
					buffer.append('&');
					i=i+"&amp;".length()-1;//Can this change the variable that controls the loop?
				}
				else if(s.startsWith("&lt;", i)){
					buffer.append('<');
					i=i+"&lt;".length()-1;//Can this change the variable that controls the loop?
				}
				else if(s.startsWith("&gt;", i)){
					buffer.append('>');
					i=i+"&gt;".length()-1;//Can this change the variable that controls the loop?
				}
				else{
					buffer.append('&');
				}
				break;
			default: buffer.append(c);
			}
		}//for
		return buffer.toString();
	}
	
	// testing ---------------------------------------------
	
	/** testing
	 * @param args */
	public static void main(String[] args){
		String s = "Hola<!--He says:\"Huhu\"\nbu--> guapo<!--comment-->!";
		say(s);
		String escaped=escape(s);
		say(escaped);
		String unescaped=unescape(escaped);
		say(unescaped);
		say(""+s.equals(unescaped));
	}
	
	/** only debug 
	 * @param o any object */
	static void say(Object o){System.out.println(o);}
}