package inspiracio.atom.client;

import inspiracio.lang.Equals;
import inspiracio.servlet.http.HttpException;
import inspiracio.user.User;

import java.security.Principal;
import java.util.List;

import org.junit.Test;

import atom.gdata.GDataParameters;

public class LocalProxyFactoryTest {

	/** A local GET request. */
	@Test public void localGet()throws HttpException{
		//Make a proxy factory for local proxies.
		ProxyFactory factory=new LocalProxyFactory();
		
		//Tell it who I am
		Principal principal=new Principal(){
			@Override public String getName(){return "alex@inspiracio.com";}
		};
		factory.setCallerPrincipal(principal);
		
		//Get a proxy:
		AtomProxy<User> proxy=factory.get(User.class);
		
		//Get my user:
		GDataParameters params=new GDataParameters();
		List<User>users=proxy.get(params);
		
		//Check results
		if(users.size()!=1)
			throw new RuntimeException();
		User user=users.get(0);
		if(!Equals.equals(user.getEmail(), principal.getName()))
			throw new RuntimeException();
	}

	/** A local POST request. */
	@Test public void localPost()throws HttpException{
		//Get a proxy:
		AtomProxy<User> proxy=this.getProxy();
		
		//Post a new user:
		User user=new User();
		user.setEmail("alex@inspiracio.com");
		
		user=proxy.insert(user);
		
		//Check results
	}
	
	/** A local PUT/update request. */
	@Test public void localPut()throws HttpException{
		//Get a proxy:
		AtomProxy<User> proxy=this.getProxy();
		
		//Post a new user:
		User user=new User();
		user.setEmail("alex@inspiracio.com");
		
		user=proxy.update(user);
		
		//Check results
	}
	
	/** A local DELETE request. */
	@Test public void localDelete()throws HttpException{
		//Get a proxy:
		AtomProxy<User> proxy=this.getProxy();
		
		//Post a new user:
		String id="23456789";
		String etag="16";
		
		proxy.delete(id, etag);
		
		//Check results
	}
	

	//Helpers ---------------------------------------------------
	
	AtomProxy<User> getProxy(){
		//Make a proxy factory for local proxies.
		ProxyFactory factory=new LocalProxyFactory();
		
		//Tell it who I am
		Principal principal=new Principal(){
			@Override public String getName(){return "alex@inspiracio.com";}
		};
		factory.setCallerPrincipal(principal);
		
		//Get a proxy:
		AtomProxy<User> proxy=factory.get(User.class);
		return proxy;
	}
	
}