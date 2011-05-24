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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/** Wraps an input stream into a servlet input stream. 
 * <p>
 * This implementation is inefficient. 
 * For efficiency, override all methods.
 * */
class ServletInputStreamWrapper extends ServletInputStream {

	//State ------------------------------------------------
	
	private InputStream in;
	
	//Constructor ------------------------------------------
	
	ServletInputStreamWrapper(InputStream in){
		this.in=in;
	}
	
	//Methods ----------------------------------------------
	
	@Override public int read() throws IOException {
		return in.read();
	}

	@Override public int available() throws IOException {
		return in.available();
	}

	@Override public void close() throws IOException {
		in.close();
	}

	@Override public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}

	@Override public boolean markSupported() {
		return in.markSupported();
	}

	@Override public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	@Override public int read(byte[] b) throws IOException {
		return in.read(b);
	}

	/** Reads one line: until CRLF and returns it.
	 * <p>
	 * The bytes are interpreted as ASCII. */
	String readLineString()throws IOException{
		int offset=0;
		int length=1024;
		byte[] bytes=new byte[length];
		int red=this.readLine(bytes, offset, length);
		if(red==-1)
			throw new EOFException();
		String line=new String(bytes, offset, red-2);
		return line;
	}
	
	@Override public synchronized void reset() throws IOException {
		in.reset();
	}

	@Override public long skip(long n) throws IOException {
		return in.skip(n);
	}

}