package inspiracio.servlet.http;

import org.junit.Test;

public class HttpURLTest {

	@Test public void t(){
		   HttpURL url = new HttpURL();
           url.setScheme("https");
           url.setHost("siine-web-integration.appspot.com");
           url.setPath("/_ah/");
           url.setFile("login");
           url.setParameter("continue", "http://localhost/");
           url.setParameter("auth", "bla");
           String authURI = url.toString();
	}
}