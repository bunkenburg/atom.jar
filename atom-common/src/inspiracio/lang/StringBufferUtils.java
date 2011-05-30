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

/** Methods that should be in StringBuffer. 
 * We cannot extend StringBuffer, because it is final.
 * Make static methods here. 
 * <p>
 * @deprecated Prefer StringBuilder over StringBuffer.
 * */
public class StringBufferUtils {
	
	/** Replace all occurrences of a key by a value. 
	 * @param buffer Will change this buffer.
	 * @param key The string to look for in the buffer.
	 * 	This is just a String. not a regular expression.
	 * @param value The string that goes in the buffer
	 * @return How many replacements has the method made? */
	public static int replaceAll(StringBuffer buffer, String key, String value){
		//Is there a better way to do this?
		int count = 0;
		int i = 0;
		while (0<=i && i<buffer.length()){
			//find next occurrence
			i = buffer.indexOf(key, i);
			if (0<=i){
				//substitute one occurrence
				int end = i + key.length();
				buffer.replace(i, end, value);
				count++;
				//and prepare for the next
				i = end;
			}
		}
		return count;
	}//replaceAll
	
	/** Replace all occurrences of a key-char by a value-char.
	 * Having a separate method for char-replacement is good
	 * for performance, because we know that the buffer needn't
	 * grow and we will not shift chars.
	 * @param buffer Will change this buffer.
	 * @param key The char to look for in the buffer.
	 * @param value The char that goes in the buffer 
	 * */
	public static void replaceAll(StringBuffer buffer, char key, char value){
		final int N = buffer.length();
		for (int i=0; i<N; i++){
			char c = buffer.charAt(i);
			if (c==key){
				buffer.setCharAt(i, value);
			}
		}
	}//replaceAll
	
	/** Does the buffer start with prefix? 
	 * @param buffer
	 * @param prefix 
	 * @return boolean */
	public static boolean startsWith(StringBuffer buffer, String prefix){
		boolean startsWith = buffer.indexOf(prefix)==0;
		return startsWith;
	}

	/** Puts all chars in the buffer in lower case.
	 * Does not allocate Strings, does not change the size of the
	 * buffer.
	 * @param buffer to be put into lower case */
	public static void toLowerCase(StringBuffer buffer){
		final int N = buffer.length();
		for (int i=0; i<N; i++){
			char c = buffer.charAt(i);
			if (Character.isUpperCase(c)){
				c = Character.toLowerCase(c);
				buffer.setCharAt(i, c);
			}
		}
	}
}