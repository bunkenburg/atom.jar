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
	private String bla;
	
	//Constructors --------------------------------------
	
	public User(){}
	
	//Accessors -----------------------------------------
	
	@Override public Object getId(){return id;}
	public String getEmail(){return email;}
	public Date getUpdated(){return this.updated;}
	public void setBla(String e){this.bla=e;}
	public void setEmail(String e){this.email=e;}
	public void setId(String id){this.id=id;}
	public void setUpdated(Date d){this.updated=d;}


	//Conversion ----------------------------------------

	/***/
	@Override public Entry toEntry(boolean root, Style style){
		Entry e=super.toEntry(root, style);
		if(root)
			e.addNamespace("inspiracio", "http://www.inspiracio.com/xsd/user.xsd");
		e.addSimpleExtElement("inspiracio:bla", bla);
		return e;
	}

	/** */
	@Override public User fromEntry(Entry e){
		String id=e.getId();
		String email="dummy@inspiracio.com";
		Date updated=e.getUpdated();
		String bla=e.getExtensionElementTextContent("inspiracio:bla");
		User u=new User();
		u.setId(id);
		u.setEmail(email);
		u.setUpdated(updated);
		u.setBla(bla);
		return u;
	}
}