package inspiracio.atom.android;

import inspiracio.atom.client.AtomProxy;
import inspiracio.atom.client.RemoteProxyFactory;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.HttpURL;
import inspiracio.user.User;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import atom.gdata.GDataParameters;

public class AtomActivity extends Activity {
	//private static final String COOKIE_VALUE_DELIMITER = ";";
	//private static final char NAME_VALUE_SEPARATOR = '=';
	//private static final String SET_COOKIE = "Set-Cookie";

	/** IP or hostname of the Atom service. Alex's laptop. */
    private static final String SERVER="192.168.2.102";
    
    /** Port of Atom service */
    private static final int PORT=8888;
    
    /** Email of the authenticated user */
    private static final String EMAIL="alex@inspiracio.com";
    	
    /** Name of the authentication cookie */
    private static final String COOKIE="dev_appserver_login";
    

	//State -----------------------------------------------------------------
	
	AtomProxy<User> proxy;	
	
    /** Called when the activity is first created. */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.init();
        this.atomGET();
        this.atomPOST();
        this.atomPUT();
        this.atomDELETE();
    }

    /** Sends an Atom-POST just using java.net.*. */
    void post(){
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
        	con.setRequestProperty("Content-Type", "application/xml+atom");//Prevents GAE server from parsing the response body and thereby using up the input stream.
        	//con.setChunkedStreamingMode(0);
        	OutputStream out=con.getOutputStream();
        	out=new BufferedOutputStream(out);
        	Writer writer=new OutputStreamWriter(out, "UTF-8");
        	writer.write(body);
        	writer.flush();
        	
        	int status=con.getResponseCode();
        	String msg=con.getResponseMessage();
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
        	
        	/*
    		init();    	
        	User u=null;
        	//u=getUser();
        	if(u==null){
        		//INSERT
        		u=new User();
        		u.setAndroidModel("android model");
        		u.setEmail("inspiracio@gmail.com");
        		u.setImei("imei");
        		u.setMobileNumber("678 693 830");
        		u=proxy.insert(u);//This is where the zip problem may be.
        		System.out.println(u);
        	}
        	*/
        }catch(Throwable t){
        	StackTraceElement[] stack=t.getStackTrace();
        	t.printStackTrace();
        }finally{
        	if(con!=null)
        		con.disconnect();
        }
    }

    /** Sends an Atom-GET, using atom-client.jar. */
	public void atomGET(){
		try{
			GDataParameters params=new GDataParameters();
			List<User> users=proxy.get(params);
			System.out.println(users);
		}catch(Throwable t){
			StackTraceElement[] stack=t.getStackTrace();
			t.printStackTrace();
        }finally{
        }
	}

    /** Sends an Atom-DELETE, using atom-client.jar. */
	public void atomDELETE(){
		try{
			String id=UUID.randomUUID().toString();
			String etag="14";
			proxy.delete(id, etag);
			System.out.println("deleted " + id + " " + etag);
		}catch(Throwable t){
			StackTraceElement[] stack=t.getStackTrace();
			t.printStackTrace();
        }finally{
        }
	}

    /** Sends an Atom-insert via POST, using atom-client.jar. */
	public void atomPOST(){
		try{
			User u=new User();
			u.setEmail(EMAIL);
			User r=proxy.insert(u);
			System.out.println(r);
		}catch(Throwable t){
			StackTraceElement[] stack=t.getStackTrace();
			t.printStackTrace();
        }finally{
        }
	}

    /** Sends an Atom-update via PUT, using atom-client.jar. */
	public void atomPUT(){
		try{
			User u=new User();
			u.setEmail(EMAIL);
			u.setETag(11);
			u.setId(UUID.randomUUID().toString());
			u.setUpdated(new Date());
			User r=proxy.update(u);
			System.out.println(r);
		}catch(Throwable t){
			StackTraceElement[] stack=t.getStackTrace();
			t.printStackTrace();
        }finally{
        }
	}

	User getUser() throws HttpException, IOException{
		GDataParameters params=new GDataParameters();
		List<User>users=proxy.get(params);
		if(!users.isEmpty())
			return users.get(0);
		else
			return null;
    }

    /** initialises this.proxy 
     * */
    void init(){
		HttpURL base=new HttpURL("http://domain/atom/-/");
		base.setHost(SERVER);
		base.setPort(PORT);
		
		/*
		//authenticate in simulated GAE
		HttpURL _ah=new HttpURL(base.toString());
		_ah.setAbsolute("/_ah/login");
		String data="continue=" + URLEncoder.encode("http://www.ikea.se") + "&email=" + EMAIL;
		URL u=new URL(_ah.toString());

		HttpURLConnection con=(HttpURLConnection)u.openConnection();
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		OutputStream o=con.getOutputStream();
		OutputStreamWriter writer=new OutputStreamWriter(o);
		writer.write(data);
		writer.flush();
		con.connect();

		//Get the cookie
    	Map<String,String> cookie = new HashMap<String,String>();
		String headerName=null;
		for (int i=1; (headerName = con.getHeaderFieldKey(i)) != null; i++) {
		    if (headerName.equalsIgnoreCase(SET_COOKIE)) {
		    	StringTokenizer st = new StringTokenizer(con.getHeaderField(i), COOKIE_VALUE_DELIMITER);
		    	// the specification dictates that the first name/value pair
		    	// in the string is the cookie name and value, so let's handle
		    	// them as a special case: 
		    	if (st.hasMoreTokens()) {
		    		String token  = st.nextToken();
		    		String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
		    		String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
		    		cookie.put(name, value);
		    	}
		    	while (st.hasMoreTokens()) {
		    		String token  = st.nextToken();
		    		String name=token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR)).toLowerCase();
		    		String value=token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
		    		cookie.put(name,value);
		    	}
		    }
		}
		String value =cookie.get(COOKIE);
		*/
		String value="bla";
		
		RemoteProxyFactory factory=new RemoteProxyFactory(base);
		//factory.setCookie(COOKIE, value);
		this.proxy=factory.get(User.class);
    }
}