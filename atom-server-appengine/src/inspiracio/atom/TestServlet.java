package inspiracio.atom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** A test servlet to see whether POST request work well from 
 * java.net.HttpURLConnection in Android to a servlet in GAE. */
public class TestServlet extends HttpServlet{

	/** Receive an Atom entry XML in the body and send it back. */
	@Override protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
		InputStream in=request.getInputStream();
		
		Reader reader=new InputStreamReader(in, "UTF-8");
		StringBuilder builder=new StringBuilder();
		int i=reader.read();
		while(0<=i){
			char c=(char)i;
			builder.append(c);
			i=reader.read();
		}
		String body=builder.toString();
		
		ServletOutputStream out=response.getOutputStream();
		Writer writer=new OutputStreamWriter(out, "UTF-8");
		writer.write(body);
		writer.flush();
	}

}