package com.siine.user;

import inspiracio.atom.AbstractAtomBean;

import java.util.Date;

import atom.Entry;
import atom.gdata.Style;

/** A user, as stored in the server. */
public class User extends AbstractAtomBean{
	
	//State ----------------------------------------

	/** Numeric id, assigned by GAE datastore.
	 * I user Long rather than long so that it can be null. */
	private Long id;
	
	private String email;
	private String mobileNumber;
	private String androidModel;
	private String imei;
	
	private Date published;
	private Date updated;
	
	//Constructors ---------------------------------
	
	/** Sets nothing */
	public User(){}

	//Accessors ------------------------------------

	public String getAndroidModel(){return this.androidModel;}
	@Override public Long getId(){return id;}
	public String getEmail(){return email;}
	public String getImei(){return imei;}
	@Override public String getKey(){return null;}
	public String getMobileNumber(){return mobileNumber;}
	public Date getPublished(){return published;}
	public Date getUpdated(){return updated;}
	
	public void setAndroidModel(String s){this.androidModel=s;}
	public void setId(Long id){this.id=id;}
	public void setEmail(String s){this.email=s;}
	public void setImei(String s){this.imei=s;}
	public void setMobileNumber(String s){this.mobileNumber=s;}
	public void setPublished(Date d){this.published=d;}
	public void setUpdated(Date d){this.updated=d;}

	//Atom conversion ------------------------------
	
	/** Convert the entry into a new user.
	 * <p>
	 * Calling this method does not affect the
	 * instance on which it is called. It is as if this
	 * method is a constructor or a static method.
	 * I don't use constructors or static methods
	 * because they don't participate in inheritance.
	 * <p>
	 * Remember when overwriting:
	 * bean.setETag(entry.getETag());
	 *
	 * @param entry
	 * @return fresh bean instance
	 * */
	@Override public User fromEntry(Entry entry){
		//Get the data
		//Ignore title: it just contains the email again.
		String email=entry.getExtensionElement("siine:email").getTextContent();
		String androidModel=entry.getExtensionElement("siine:androidModel").getTextContent();
		String mobileNumber=entry.getExtensionElement("siine:mobileNumber").getTextContent();
		String imei=entry.getExtensionElement("siine:imei").getTextContent();
		Date updated=entry.getUpdated();
		Date published=entry.getPublished();
		String etag=entry.getETag();
		
		User user=new User();
		try{
			String s=entry.getId();
			long id=Long.parseLong(s);
			user.setId(id);
		}catch(NumberFormatException nfe){
			//Don't set id.
		}
		user.setEmail(email);
		user.setMobileNumber(mobileNumber);
		user.setAndroidModel(androidModel);
		user.setImei(imei);
		user.setUpdated(updated);
		user.setPublished(published);
		user.setETag(etag);
		return user;
	}

	/** Converts the user to an entry, according to the given style.
	 * 
	 * @param root Will the entry be the root element of an XML document?
	 * 	If so, toEntry should set any namespace attributes that are necessary to
	 * 	define the XML elements that come with the chosen style.
	 * 
	 * @param style
	 * 
	 * @see inspiracio.atom.AtomBean#toEntry(boolean, atom.gdata.Style)
	 */
	@Override public Entry toEntry(boolean root, Style style) {
		Entry entry=super.toEntry(root, style);
		
		//Ignoring the style for now, and just adding everything. Not guarding against nulls.
		if(id!=null)
			entry.setId(id);
		entry.setTitle(this.email);
		entry.setUpdated(this.updated);
		entry.setPublished(this.published);
		entry.setETag(this.getETag());
		entry.addSimpleExtElement("siine:email", this.email);
		entry.addSimpleExtElement("siine:mobileNumber", this.mobileNumber);
		entry.addSimpleExtElement("siine:androidModel", this.androidModel);
		entry.addSimpleExtElement("siine:imei", this.imei);
		
		return entry;
	}
	
}