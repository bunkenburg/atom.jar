package inspiracio.atom.client;

import inspiracio.servlet.http.HttpURL;
import inspiracio.user.User;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

/** A client that just uses java.net.HttpURLConnection. */
public class HttpURLConnectionClient{

	/** IP or hostname of the Atom service. Alex's laptop. */
    private static final String SERVER="192.168.2.102";
    
    /** Port of Atom service */
    private static final int PORT=8888;
    
    /** Email of the authenticated user */
    private static final String EMAIL="alex@inspiracio.com";
    	
    /** Sends an Atom-insert via POST, with just java.net.* classes. */
	@Test public void post(){
    	HttpURLConnection con=null;
        try{
        	String body=
        		"<?xml version='1.0' encoding='UTF-8'?>" +
        		"<entry xmlns:gd='http://schemas.google.com/g/2005' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
        		"<title>inspiracio@gmail.com</title>" +
        		"<siine:email>" + EMAIL + "</siine:email>" +
        		"<siine:mobileNumber>678 693 830</siine:mobileNumber>" +
        		"<siine:androidModel>android model</siine:androidModel>" +
        		"<siine:imei>imei</siine:imei>" +
        		"</entry>";
        	
        	//Try to POST from Android to GAE, just from java.net.URLConnection to a servlet, no atom.jar.
    		HttpURL base=new HttpURL("http://domain/test/-/");
    		base.setHost(SERVER);
    		base.setPort(PORT);
        	URL u=new URL(base.toString());
        	URLConnection co=u.openConnection();
        	con=(HttpURLConnection)co;
        	con.setDoOutput(true);
        	con.setDoInput(true);
        	con.setRequestMethod("POST");
        	con.setRequestProperty("Content-Type", "application/xml+atom");
        	//con.setChunkedStreamingMode(0);//For chunking client must send Content-Length. Don't chunk.
        	OutputStream out=con.getOutputStream();
        	out=new BufferedOutputStream(out);
        	Writer writer=new OutputStreamWriter(out, "UTF-8");
        	writer.write(body);
        	writer.flush();
        	
        	int status=con.getResponseCode();
        	String msg=con.getResponseMessage();
        	say("Response received:");
        	say(status + " " + msg);
        	InputStream in=con.getInputStream();
        	in=new BufferedInputStream(in);
        	Reader reader=new InputStreamReader(in, "UTF-8");
        	StringBuilder builder=new StringBuilder();
        	int i=reader.read();
        	while(0<=i){
        		char c=(char)i;
        		builder.append(c);
        		i=reader.read();
        	}
        	body=builder.toString();
        	say(body);
        }catch(Throwable t){
        	@SuppressWarnings("unused")
			StackTraceElement[] stack=t.getStackTrace();
        	t.printStackTrace();
        }finally{
        	if(con!=null)
        		con.disconnect();
        }
	}
	
	/** Sends an Atom-insert via POST, using atom-client.jar. */
	@Test public void atomPOST(){
		try{
			HttpURL base=new HttpURL("http://domain/test/-/");
			base.setHost(SERVER);
			base.setPort(PORT);
			RemoteProxyFactory factory=new RemoteProxyFactory(base);
			factory.setGZip(false);//XXX Later try with gzip.
			AtomProxy<User> proxy=factory.get(User.class);

			User u=new User();
			u.setEmail(EMAIL);

			User r=proxy.insert(u);
			say(r);

		}catch(Throwable t){
			@SuppressWarnings("unused")
			StackTraceElement[] stack=t.getStackTrace();
			t.printStackTrace();
        }finally{
        }
	}
	
	void say(Object o){System.out.println(o);}
}