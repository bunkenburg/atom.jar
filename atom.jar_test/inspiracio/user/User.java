package inspiracio.user;

import inspiracio.atom.AbstractAtomBean;

import java.util.Date;

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

}