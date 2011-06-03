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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/** A wrapper for HttpURLConnection that can do gzip.
 * <p>
 * Now, it does gzip always. It would be better to negotiate with 
 * the server. */
public final class IHttpURLConnection extends HttpURLConnectionWrapper{
	
	private static final String ACCEPT_ENCODING="Accept-Encoding";
	private static final String CONTENT_ENCODING="Content-Encoding";
	private static final String GZIP="gzip";
	private static final String DEFLATE="deflate";
	
	//State --------------------------------------
	
	/** Compress requests and responses with gzip? */
	private final boolean gzip;
	
	//Constructor --------------------------------
	
	/** @param gzip Compress requests and responses with gzip? */
	public IHttpURLConnection(HttpURLConnection con, boolean gzip){
		super(con);
		this.gzip=gzip;
		if(gzip){
			//Tell the server that request body is gzipped.
			con.addRequestProperty(CONTENT_ENCODING, GZIP);

			//Tell the server that we want a gzipped response.
			con.addRequestProperty(ACCEPT_ENCODING, GZIP);
		}
	}

	//Methods ------------------------------------
	
	/** Gets the stream to read the response. */
	@Override public InputStream getInputStream() throws IOException{
		InputStream in=super.getInputStream();//communicate to server
		String encoding=super.getContentEncoding();
		if(GZIP.equalsIgnoreCase(encoding)){
			try{
				in=new GZIPInputStream(in);
			}catch(EOFException eofe){
				//We couldn't even wrap the input stream into a GZIP input stream.
				//That means there's nothing to read.
				//We can just return the original stream.
				//There's nothing to read anyway.
				return in;
			}
		}
		else if(DEFLATE.equalsIgnoreCase(encoding))
			in=new InflaterInputStream(in, new Inflater(true));
		return in;
	}

	/** Gets the stream to write the response. */
	@Override public OutputStream getOutputStream() throws IOException{
		OutputStream out=super.getOutputStream();
		if(gzip)
			out=new GZIPOutputStream(out);
		return out;
	}
	
}