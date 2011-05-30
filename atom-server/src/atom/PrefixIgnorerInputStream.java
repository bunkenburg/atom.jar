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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

/** An input stream that ignores an optional prefix of the stream.
 * <p>
 * If the streams starts with the prefix, the prefix is ignored and the stream
 * is URL-decoded.
 * <p>
 * If the stream does not start with the prefix, it is just passed through. No
 * URL-decoding.
 * <p>
 * This implementation reads and caches the whole stream in the constructor:
 * For large streams, that is inefficient.
 * @author BARCELONA\alexanderb
 *
 */
public class PrefixIgnorerInputStream extends InputStream {

	//State ---------------------------------------------
	
	/** The underlying input stream. */
	private ByteArrayInputStream delegate; 
	
	//Constructor ---------------------------------------
	
	/** A PrefixIgnorerInputStream for a specified prefix.
	 * @param prefix the optional prefix to ignore
	 * @param delegate underlying stream
	 * @throws IOException
	 */
	public PrefixIgnorerInputStream(byte[] prefix, InputStream delegate) throws IOException {
		//Read the whole stream. Can do this more efficiently.
		ByteArrayOutputStream bo=new ByteArrayOutputStream();
		int i=delegate.read();
		while(-1 < i){
			byte b = (byte)i;
			bo.write(b);
			i=delegate.read();
		}
		byte[] bytes = bo.toByteArray();

		//Maybe eat the prefix
		if (prefix.length <= bytes.length){
			boolean same = true;// prefix[0..i] == bytes[0..i]
			for (i=0; i<prefix.length; i++){
				same = same && prefix[i]==bytes[i];
			}
			if (same){
				int length = bytes.length - prefix.length;
				byte[] shifted = new byte[length];
				for (i=0; i<length; i++){
					shifted[i] = bytes[i+prefix.length];
				}
				//Now URL-decoding
				String encoded = new String(shifted, "US-ASCII");
				String decoded = URLDecoder.decode(encoded, "UTF-8");//the form must post in UTF-8
				bytes = decoded.getBytes("UTF-8");
			}
		}
		
		this.delegate = new ByteArrayInputStream(bytes);
	}
	
	/** Reads next byte.
	 * @return the next byte as int, or -1 if there are no more bytes.
	 * */
	@Override public int read() {
		int i = this.delegate.read();
		return i;
	}

}