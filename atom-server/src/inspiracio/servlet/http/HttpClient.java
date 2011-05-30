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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/** Experimental.
 * Substitute with good http client, like the one from Apache.
 * */
public class HttpClient {

	/** Send http request without body */
	public byte[] execute(String method, HttpURL url)throws IOException{
		byte[] requestBody = new byte[0];
		return this.execute(method, url, requestBody);
	}

	/** Send http request with request body */
	public byte[] execute(String method, HttpURL url, byte[] requestBody)throws IOException{

		//use curl
		try{
			//write request body to file
			File file = File.createTempFile("HttpClient", null);
			OutputStream out = new FileOutputStream(file);
			out.write(requestBody);
			out.close();
			String filename = file.getAbsolutePath();

			//curl url --data-binary @<file> -X <method>
			Runtime runtime = Runtime.getRuntime();
			String[] cmdarray = {"curl", url.toString(), "--data-binary", "@"+filename, "-X", method};
			Process process = runtime.exec(cmdarray);
			InputStream in = process.getInputStream();
			int exit = process.waitFor();//InterruptedException
			if(exit==0)
				file.delete();
			else
				throw new RuntimeException();
System.out.println(exit);

			//convert response body in byte[]
			ArrayList<Byte> buffer = new ArrayList<Byte>();
			int i = in.read();
			while(0<i){
				byte b = (byte)i;
				buffer.add(b);
				i = in.read();
char c = (char)i;System.out.print(c);
			}
			Byte[] response = buffer.toArray(new Byte[0]);
			byte[] bytes = new byte[response.length];
			for(int j=0; j<response.length; j++){
				Byte B = response[j];
				byte b = B;
				bytes[j] = b;
			}

			return bytes;
		}catch(InterruptedException ie){
			throw new RuntimeException(ie);
		}
	}
}