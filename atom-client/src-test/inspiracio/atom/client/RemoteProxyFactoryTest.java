package inspiracio.atom.client;

import inspiracio.servlet.http.HttpURL;
import inspiracio.user.User;

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
		factory.setCookie("ACSID", value);
		factory.setCookie("another", value);
		
		//Get a proxy:
		AtomProxy<User> proxy=factory.get(User.class);

		//Get my user
		GDataParameters params=new GDataParameters();
		//List<User> users=proxy.get(params);
		//if(users.size()!=1)throw new RuntimeException();
	}

}