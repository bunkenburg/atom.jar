/*  Copyright 2011 Alexander Bunkenburg alex@inspiracio.com

    This file is part of atom.jar.

    atom.jar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    atom.jar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with atom.jar.  If not, see <http://www.gnu.org/licenses/>.
 */
package inspiracio.atom;

import java.util.ArrayList;
import java.util.List;

import atom.Entry;
import atom.gdata.Style;

/** For convenience, a superclass for implementations of AtomBean.
 * @author BARCELONA\alexanderb
 */
public abstract class AbstractAtomBean implements AtomBean {

	//ETag ------------------------------------------------------------

	/** The String for the strong ETag that is the version of this object.
	 * unquoted, unescaped: the clients to that. */
	private String etag;

	/** Gets an ETag value that represents the version of this business object.
	 * Whenever the business object is changed, the ETag must also change.
	 * The ETag is strong (see ETag spec in HTTP 1.1 spec).
	 * A strong ETag value is a String enclosed in double quotes and inside the
	 * String the quotes are escaped; but this method returns just the String.
	 * The clients of the method take care of quoting and escaping. */
	@Override public String getETag(){return etag;}

	/** If the ETag of this object is an int, returns the int.
	 * @exception NumberFormatException ETag is not an int. */
	public int getVersion()throws NumberFormatException{
		String etag=this.getETag();
		int version=Integer.parseInt(etag);//NumberFormatException
		return version;
	}

	/** Sets ETag. Set a String that represents the version of this object.
	 * Must change every time the object changes. Any String is okay:
	 * clients takes care of escaping and quoting. */
	@Override public void setETag(String etag){this.etag=etag;}

	/** Sets ETag. Like setETag(String), just for convenience when the
	 * ETag is an int. */
	public void setETag(int etag){this.etag=Integer.toString(etag);}

	/** Sets ETag to an int. */
	public void setVersion(int version){this.setETag(version);}

	//Namespaces ------------------------------------------------------

	/** Identifies one namespace in an XML. */
	private static class Namespace{
		/** identifies the namespace locally, may be empty, may be like "inspiracio" */
		private String prefix;

		/** identifies the namespace globally, like "http://www.inspiracio.cat" */
		private String id;

		/** URL of the XML schema */
		private String location;

		/** for better debugging  */
		@Override public String toString(){
			return "Namespace[" + this.prefix + "->" + this.id + " at " + this.location + "]";
		}
	}

	/** the namespaces registered */
	private List<Namespace> namespaces = new ArrayList<Namespace>();

	/** Sets a namespace for the generated XML.
	 * @param prefix May be empty.
	 * @param id of the namespace, like "http://www.inspiracio.cat" */
	public void addNamespace(String prefix, String id){
		Namespace ns=new Namespace();
		ns.prefix=prefix;
		ns.id=id;
		this.namespaces.add(ns);
	}

	/** Sets a namespace for the generated XML with xsd for validation.
	 * @param prefix May be empty.
	 * @param id of the namespace, like "http://www.inspiracio.cat" */
	public void addNamespace(String prefix, String id, String location){
		Namespace ns = new Namespace();
		ns.prefix = prefix;
		ns.id = id;
		ns.location = location;
		this.namespaces.add(ns);
	}

	/** Is the form clean? Has it been saved in the server?
	 * <p>
	 * In the future this property can go into the superclass.
	 * */
	private boolean clean;

	public boolean isClean(){return clean;}
	public boolean isDirty(){return !clean;}
	@Override public void setClean(){clean=true;}
	public void setClean(boolean c){clean=c;}
	public void setDirty(){clean=false;}

	//Conversion to Entry ----------------------------------------------

	/** Converts to an entry.
	 * If your bean does not have style, override this method,
	 * but call super.toEntry first, so that it can set the namespaces.
	 * @see inspiracio.atom.AtomBean#toEntry()
	 */
	@Override public Entry toEntry() throws Exception {
		Entry entry =  new Entry();

		//etag
		String etag=this.getETag();
		entry.setETag(etag);

		//namespaces
		//if(root)
		this.addNamespace("gd", "http://schemas.google.com/g/2005");
		for(Namespace ns : this.namespaces){
			if(ns.location!=null)
				entry.addNamespace(ns.prefix, ns.id, ns.location);
			else
				entry.addNamespace(ns.prefix, ns.id);
		}

		return entry;
	}

	/** Converts the entry according to the given style.
	 * <p>
	 * Override if your bean has style, but call super.toEntry()
	 * first so that it can set the namespaces (if you want namespaces).
	 * @param root Will the entry be the root element of an XML document?
	 * 	If so, toEntry should set any namespace attributes that are necessary to
	 * 	define the XML elements that come with the chosen style.
	 * @param style
	 * @see inspiracio.atom.AtomBean#toEntry(boolean, atom.gdata.Style)
	 */
	@Override public Entry toEntry(boolean root, Style style) //throws Exception
	{
		Entry entry=new Entry();

		//etag
		String etag=this.getETag();
		if(etag!=null){
			//etag may be null correctly if we are converting a new bean to entry to send it in a create-request
			entry.setETag(etag);
		}

		//namespaces
		if(root){
			this.addNamespace("gd", "http://schemas.google.com/g/2005");
		}
		for(Namespace ns : this.namespaces){
			if(ns.location!=null)
				entry.addNamespace(ns.prefix, ns.id, ns.location);
			else
				entry.addNamespace(ns.prefix, ns.id);
		}

		return entry;
	}

	/** Convert the entry into a new bean.
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
	@Override public AtomBean fromEntry(Entry entry){
		throw new RuntimeException("not implemented in " + this.getClass());
	}

}