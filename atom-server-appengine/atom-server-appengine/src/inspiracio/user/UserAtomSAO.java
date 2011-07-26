package inspiracio.user;

import inspiracio.atom.AbstractAtomSAO;
import inspiracio.servlet.http.ForbiddenException;
import inspiracio.servlet.http.HttpException;
import inspiracio.servlet.http.InternalServerErrorException;
import inspiracio.servlet.http.NotAuthorizedException;
import inspiracio.servlet.http.PreconditionFailedException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import atom.Entry;
import atom.gdata.GDataURL;

/** AtomSAO for users */
public class UserAtomSAO extends AbstractAtomSAO<User>{

	/** Gets the user that represents the authenticated user.
	 * A user can only get themselves, not any other users.
	 * 
	 * @param url
	 * @return Some beans from the store that match the parameters
	 * 
	 * @exception RuntimeException always. Must override if you want retrieval.
	 * 
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * 
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * 
	 * @throws InternalServerErrorException The SAO signals that something has gone
	 * 	wrong in processing the request and that the servlet should reply 500 Internal
	 * 	Server Error to the client.
	 * */
	@Override public List<User> get(GDataURL url) throws NotAuthorizedException,ForbiddenException, InternalServerErrorException, HttpException {
		List<User>users=new ArrayList<User>();
		User u=new User();
		u.setEmail("test@inspiracio.com");
		u.setETag(12);
		u.setId(UUID.randomUUID().toString());
		u.setUpdated(new Date());
		users.add(u);
		return users;
	}

	/** Create a new user in the server.
	 * 
	 * The client must be authenticated. The client must be authenticated under the
	 * same email that appears in the user to insert. That means, a user can only insert
	 * themselves. A user cannot insert another user.
	 * 
	 * @param url The URL that has received the insertion request. The implementation
	 * 	may ignore it, or may use some parameters from the URL.
	 * 
	 * @param user The bean to be inserted.
	 * 
	 * @param slug The client wants something similar to this String appear in the id.
	 * 	Here ignored.
	 * 
	 * @return The entry as inserted. Usually, the id will have been changed.
	 * 
	 * @exception RuntimeException always. Must override if you want creation.
	 * 
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * 
	 * @throws ForbiddenException The SAO requires the client to be authenticated, but
	 * 	the username and password provided by the client could not be validated by the
	 * 	SAO.
	 * */
	@Override public User insert(GDataURL url, User user, String slug)throws NotAuthorizedException, ForbiddenException, HttpException{
		//Insert user into database.
		Date updated=new Date();
		user.setUpdated(updated);//initialise
		//user.setPublished(updated);//initialise
		user.setVersion(0);//initialise to first version which is 0.
		String id=UUID.randomUUID().toString();//the freshly assigned numeric id.
		user.setId(id);

		return user;
	}

	/** Edit a user
	 * 
	 * @param user The user as edited by the client. 
	 * 	The client must not change: id, etag, email, updated.
	 * 	These properties are for identification and versioning.
	 * 
	 * @throws NotAuthorizedException The SAO requires the client to be authenticated,
	 * 	but the client has not provided username and password.
	 * 
	 * @throws ForbiddenException The authenticated user can only edit their own user object:
	 * 	The email of the authenticated user and the email in the user object must be the same.
	 * */
	@Override public User update(User user) throws NotAuthorizedException,ForbiddenException, HttpException{
		try{
			//Modify user in database.
			long version=user.getVersion();//It came with this numeric etag.
			version=version+1;//increase the version
			user.setVersion(version);
			Date now=new Date();
			user.setUpdated(now);
			return user;
		}
		catch(NumberFormatException nfe){
			//User came without numeric version as etag.
			//We can't check whether it's up-to-date, and therefore we can't update.
			throw new PreconditionFailedException("User must have ETag.");
		}
	}

	/** Remove a user from the server.
	 * 
	 * @param id The id of the user to be removed.
	 * 
	 * @param etag Must be current, else throw PreconditionFailedException
	 * 
	 * @throws NotAuthorizedException Must be authenticated.
	 * 
	 * @throws ForbiddenException The authenticated user can only delete their own user.
	 * */
	@Override public void delete(String id, String etag) throws NotAuthorizedException,ForbiddenException, HttpException {
		say("DELETE " + id + " " + etag);
	}

	//Conversion ---------------------------------------------------------------------

	/** Translates an entry to the user.
	 * Delegates to User.fromEntry(entry).
	 * 
	 * @param entry
	 * 
	 * @return The parsed bean
	 * */
	public User toAtomBean(Entry entry){
		User dummy=new User();
		User user=dummy.fromEntry(entry);
		return user;
	}

	/** Converts from subclasses of Date to Date itself.
	 * The problem is Timestamp, which is a subclass of Date, delivered by Entry,
	 * but GAE datastore cannot store it. */
	Date toDate(Date d){
		if(d.getClass()==Date.class)
			return d;
		long time=d.getTime();
		return new Date(time);
	}
	
	//Helpers -----------------------------------------------------------------------------
	
	void say(Object o){System.out.println(o);}
}