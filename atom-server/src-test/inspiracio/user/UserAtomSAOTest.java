package inspiracio.user;

import inspiracio.servlet.http.ForbiddenException;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.InternalServerErrorException;
import inspiracio.servlet.http.NotAuthorizedException;
import inspiracio.xml.Namespace;

import java.security.Principal;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import atom.Feed;
import atom.gdata.GDataURL;

public class UserAtomSAOTest{

	/** Even though a SAO overrides only get() and not getFeed(), the feed XML can still have
	 * namespaces: the SAO must put the namespaces in the entries in bean.toEntry(true, style).
	 * Then, when AbstractAtomSAO adds the entries to the feed, the method Feed.addEntry()
	 * transfers the namespaces from the entries to the feed. 
	 * @throws HttpException 
	 * @throws InternalServerErrorException 
	 * @throws ForbiddenException 
	 * @throws NotAuthorizedException */
	@Test public void getWithNamespaces() throws NotAuthorizedException, ForbiddenException, InternalServerErrorException, HttpException{
		GDataURL url=new GDataURL("http://www.google.com/atom/-/user?style=full");
		UserAtomSAO sao=new UserAtomSAO();
		sao.setCallerPrincipal(new Principal(){@Override public String getName(){return "alex@inspiracio.com";}});
		Feed feed=sao.getFeed(url);
		List<Namespace>ns=feed.getNamespaces();
		Assert.assertTrue(3<ns.size());//Three are inevitable: atom, gd, xsi. The fourth comes from the entry.
	}
}