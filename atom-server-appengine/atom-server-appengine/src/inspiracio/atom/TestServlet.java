package inspiracio.atom;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Receive POST from java.net.URL in Android and send the body back. */
public class TestServlet extends HttpServlet{

	@Override protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		//Receive the body
		ServletInputStream in=request.getInputStream();
		Reader reader=new InputStreamReader(in, "UTF-8");
		StringBuilder builder=new StringBuilder();
		int i=reader.read();
		while(0<=i){
			char c=(char)i;
			i=reader.read();
			builder.append(c);
		}
		String body=builder.toString();
		System.out.println("Request body received: " + body);
		
		//Write the response
		response.setStatus(200);//inserted
		ServletOutputStream out=response.getOutputStream();
		Writer writer=new OutputStreamWriter(out,"UTF-8");
		writer.write(body);
		writer.flush();
	}
}