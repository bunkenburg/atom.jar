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
package inspiracio.lang;

/** Methods that should be in StringBuilder. 
 * We cannot extend StringBuilder, because it is final.
 * Make static methods here. */
public class StringBuilderUtils {
	
	/** Replace all occurrences of a key by a value. 
	 * @param builder Will change this builder.
	 * @param key The string to look for in the builder.
	 * 	This is just a String. not a regular expression.
	 * @param value The string that goes in the builder
	 * @return How many replacements has the method made? */
	public static int replaceAll(StringBuilder builder, String key, String value){
		//Is there a better way to do this?
		int count=0;
		int i=0;
		while(0<=i && i<builder.length()){
			//find next occurrence
			i=builder.indexOf(key, i);
			if(0<=i){
				//substitute one occurrence
				int end=i + key.length();
				builder.replace(i, end, value);
				count++;
				//and prepare for the next
				i=end;
			}
		}
		return count;
	}//replaceAll
	
	/** Replace all occurrences of a key-char by a value-char.
	 * Having a separate method for char-replacement is good
	 * for performance, because we know that the builder needn't
	 * grow and we will not shift chars.
	 * @param builder Will change this builder.
	 * @param key The char to look for in the builder.
	 * @param value The char that goes in the builder 
	 * */
	public static void replaceAll(StringBuilder builder, char key, char value){
		final int N = builder.length();
		for (int i=0; i<N; i++){
			char c = builder.charAt(i);
			if (c==key){
				builder.setCharAt(i, value);
			}
		}
	}//replaceAll
	
	/** Does the builder start with prefix? 
	 * @param builder
	 * @param prefix 
	 * @return boolean */
	public static boolean startsWith(StringBuilder builder, String prefix){
		boolean startsWith=builder.indexOf(prefix)==0;
		return startsWith;
	}

	/** Does the builder start with prefix? 
	 * @param builder
	 * @param prefix 
	 * @param offset
	 * @return boolean */
	public static boolean startsWith(StringBuilder builder, String prefix, int offset){
		int i=builder.indexOf(prefix, offset);
		boolean startsWith=i==offset;
		return startsWith;
	}

	/** Puts all chars in the builder in lower case.
	 * Does not allocate Strings, does not change the size of the
	 * builder.
	 * @param builder to be put into lower case */
	public static void toLowerCase(StringBuilder builder){
		final int N = builder.length();
		for (int i=0; i<N; i++){
			char c = builder.charAt(i);
			if (Character.isUpperCase(c)){
				c = Character.toLowerCase(c);
				builder.setCharAt(i, c);
			}
		}
	}
}