package com.siine.atom.android;

import inspiracio.atom.client.AtomProxy;
import inspiracio.atom.client.RemoteProxyFactory;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.HttpURL;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import android.app.Activity;
import android.os.Bundle;
import atom.gdata.GDataParameters;

import com.siine.user.User;

public class AtomActivity extends Activity {
	private static final String COOKIE_VALUE_DELIMITER = ";";
	private static final char NAME_VALUE_SEPARATOR = '=';
	private static final String SET_COOKIE = "Set-Cookie";

	//State -----------------------------------------------------------------
	
	AtomProxy<User> proxy;	
	
    /** Called when the activity is first created. */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        try{
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
        	
        }catch(Throwable t){
        	StackTraceElement[] stack=t.getStackTrace();
        	t.printStackTrace();
        }
    }
    
    /** IP or hostname of the Atom service. Alex's laptop. */
    String SERVER="192.168.2.102";
    
    /** Port of Atom service */
    int PORT=8888;
    
    /** Email of the authenticated user */
    String EMAIL="alex@inspiracio.com";
    	
    /** Name of the authentication cookie */
    String COOKIE="dev_appserver_login";
    
    User getUser() throws HttpException, IOException{
		GDataParameters params=new GDataParameters();
		List<User>users=proxy.get(params);
		if(!users.isEmpty())
			return users.get(0);
		else
			return null;
    }

    /** initialises this.proxy 
     * @throws IOException */
    void init() throws IOException{
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
		factory.setGZip(false);//It doesn't seem to work from Android to simulated GAE.
		factory.setCookie(COOKIE, value);
		this.proxy=factory.get(User.class);
    }
}