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

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/** Wraps an output stream into a servlet output stream. 
 * <p>
 * This implementation is inefficient. 
 * For efficiency, override all methods.
 * */
class ServletOutputStreamWrapper extends ServletOutputStream {

	//State ------------------------------------------------
	
	private OutputStream out;
	
	//Constructor ------------------------------------------
	
	ServletOutputStreamWrapper(OutputStream out){
		this.out=out;
	}
	
	//Methods ----------------------------------------------

	@Override public void close() throws IOException {
		out.close();
	}

	@Override public void flush() throws IOException {
		out.flush();
	}

	@Override public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}

	@Override public void write(byte[] b) throws IOException {
		out.write(b);
	}

	@Override public void write(int b) throws IOException {
		out.write(b);
		
		@SuppressWarnings("unused")
		String s="break";
	}
	
	void say(Object o){System.out.toString();}
}