package inspiracio.atom.client;

import inspiracio.servlet.http.HttpURL;
import inspiracio.user.User;

import java.net.HttpCookie;

import org.junit.Test;

import atom.gdata.GDataParameters;

public class RemoteProxyFactoryTest{
	
	/** Test setting cookies. 
	 * Not complete. */
	@Test public void test()throws Exception{
		//Make a proxy factory and tell it where the server is:
		HttpURL base=new HttpURL("http://domain/atom/-/");
		RemoteProxyFactory factory=new RemoteProxyFactory(base);

		//Tell it who I am (give it authentication-cookie):
		String value="23456789";
		HttpCookie cookie=new HttpCookie("ACSID", value);
		factory.setCookie(cookie);
		HttpCookie cookie1=new HttpCookie("another", value);
		factory.setCookie(cookie1);

		//Get a proxy:
		AtomProxy<User> proxy=factory.get(User.class);

		//Get my user
		GDataParameters params=new GDataParameters();
		//List<User> users=proxy.get(params);
		//if(users.size()!=1)throw new RuntimeException();
	}

}