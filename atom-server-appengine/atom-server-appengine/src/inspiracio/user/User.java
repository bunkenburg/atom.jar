package inspiracio.user;

import inspiracio.atom.AbstractAtomBean;

import java.util.Date;

import atom.Entry;
import atom.gdata.Style;

/** A very simple Atom bean, just for examples and tests. */
public class User extends AbstractAtomBean{

	//State ---------------------------------------------
	
	private String id;
	private String email;
	private Date updated;
	
	//Constructors --------------------------------------
	
	public User(){}
	
	//Accessors -----------------------------------------
	
	@Override public Object getId(){return id;}
	public String getEmail(){return email;}
	public Date getUpdated(){return this.updated;}
	public void setEmail(String e){this.email=e;}
	public void setId(String id){this.id=id;}
	public void setUpdated(Date d){this.updated=d;}

	//Conversion ----------------------------------------

	/** Convert entry to user. */
	@Override public User fromEntry(Entry entry){
		String etag=entry.getETag();
		String email=entry.getTitle().format();
		String id=entry.getId();
		Date updated=entry.getUpdated();
		User u=new User();
		u.setETag(etag);
		u.setEmail(email);
		u.setId(id);
		u.setUpdated(updated);
		return u;
	}

	/** Converts this user to an entry. */
	@Override public Entry toEntry() throws Exception{
		Entry e=super.toEntry();
		String etag=this.getETag();
		e.setETag(etag);
		e.setId(this.id);
		e.setTitle(this.email);
		e.setUpdated(this.updated);
		return e;
	}

	@Override public Entry toEntry(boolean root, Style style){
		try{
			return this.toEntry();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	//Helpers -------------------------------------------
	
	@Override public String toString(){
		return "User[" + id + "]{" + email + "," + updated + "}";
	}

}