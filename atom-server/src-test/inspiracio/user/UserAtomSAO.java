package inspiracio.user;

import inspiracio.atom.AbstractAtomSAO;
import inspiracio.lang.Equals;
import inspiracio.lang.NotImplementedException;
import inspiracio.servlet.http.ForbiddenException;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.InternalServerErrorException;
import inspiracio.servlet.http.NotAuthorizedException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import atom.Entry;
import atom.gdata.GDataURL;

/** Atom SAO for testing */
public class UserAtomSAO extends AbstractAtomSAO<User>{

	@Override public void delete(String id, String etag) throws NotAuthorizedException, ForbiddenException, HttpException {
		//Someone logged in?
		Principal principal=this.getCallerPrincipal();
		if(principal==null)
			throw new NotAuthorizedException();
		
		//Simulation of deletion.
	}

	/** Only returns the user for the authenticated user. */
	@Override public List<User> get(GDataURL url) throws NotAuthorizedException,ForbiddenException, InternalServerErrorException, HttpException {
		Principal principal=this.getCallerPrincipal();
		if(principal==null)
			throw new NotAuthorizedException();
		User user=new User();
		user.setEmail(principal.getName());
		List<User>users=new ArrayList<User>();
		users.add(user);
		return users;
	}

	@Override public User insert(GDataURL url, User user, String slug) throws NotAuthorizedException, ForbiddenException, HttpException{
		Principal principal=this.getCallerPrincipal();
		if(principal==null)
			throw new NotAuthorizedException();
		if(!Equals.equals(user.getEmail(), principal.getName()))
			throw new ForbiddenException();

		//Dummy for DB insertion
		user.setId(UUID.randomUUID().toString());
		user.setUpdated(new Date());
		return user;
	}

	@Override public User toAtomBean(Entry entry) {
		throw new NotImplementedException();
	}

	@Override public User update(User user) throws NotAuthorizedException,ForbiddenException, HttpException{
		Principal principal=this.getCallerPrincipal();
		if(principal==null)
			throw new NotAuthorizedException();
		if(!Equals.equals(user.getEmail(), principal.getName()))
			throw new ForbiddenException();

		//Dummy for DB update
		user.setUpdated(new Date());
		return user;
	}

}