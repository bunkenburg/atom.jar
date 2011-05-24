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
package atom;

import inspiracio.servlet.http.HttpURL;
import inspiracio.xml.DOM;
import inspiracio.xml.Namespace;
import inspiracio.xml.sax.BaseXMLReader;
import inspiracio.xml.sax.EasySAXParseable;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;


import org.xml.sax.InputSource;

/** Superclass for elements that can be the root of an Atom-XML:
 * Feed and Entry. Encapsulates logic for XML schemas.
 * 
 * @author BARCELONA\alexanderb
 *
 */
abstract class RootElement implements EasySAXParseable {

	//State -------------------------------------------------
	
	/** The attributes of the root element. */
	private Map<String, String> attributes = new TreeMap<String, String>();
	private Map<String, String> schemaLocations = new TreeMap<String, String>();
	
	/** Should we make indented XML from this feed? */
	private boolean prettyprint=false;
	
	// Constructors --------------------------------------
	
	protected RootElement(){}
	
	//Accessors -------------------------------------------
	
	public void setPrettyprint(boolean b){this.prettyprint = b;}
	boolean getPrettyprint(){return prettyprint;}
	
	/** Adds an attribute to the root element of the feed.
	 * @param name 
	 * @param value */
	public void addAttribute(String name, String value){
		this.attributes.put(name, value);
	}

	/** Gets an attribute of the root element of the feed.
	 * @param name
	 * @return the value, or null if there is no such attribute
	 *  */
	protected String getAttribute(String name){
		return this.attributes.get(name);
	}

	/** Gets a reference to the map of attributes
	 * @return the map
	 * */
	public Map<String, String> getAttributes(){return this.attributes;}
	
	/** Adds a namespace with schema location.
	 * @param n namespace
	 * */
	public void addNamespace(Namespace n){
		String prefix=n.getPrefix();
		String url = n.getURL();
		String schemaLocation = n.getSchemaLocation();
		this.addNamespace(prefix, url, schemaLocation);
	}

	/** Adds a namespace with schema location.
	 * @param prefix Your elements will be prefixed like that. May be "".
	 * @param url The unique URL that identifies the namespace
	 * @param schemaLocation The URL of the XML schema for this namespace
	 * */
	public void addNamespace(String prefix, HttpURL url, HttpURL schemaLocation){
		String u = url.toString();
		String s = schemaLocation.toString();
		this.addNamespace(prefix, u, s);
	}

	/** Adds a namespace with schema location.
	 * @param prefix Your elements will be prefixed like that. May be "".
	 * @param url The unique URL that identifies the namespace
	 * @param schemaLocation The URL of the XML schema for this namespace
	 * */
	public void addNamespace(String prefix, String url, HttpURL schemaLocation){
		String s = schemaLocation.toString();
		this.addNamespace(prefix, url, s);
	}

	/** Adds a namespace without schema location.
	 * Adds attribute: xmlns:prefix = "url"
	 * @param prefix Your elements will be prefixed like that. 
	 * 	May be "" or null for the default schema location.
	 * @param url The unique URL that identifies the namespace
	 * */
	public void addNamespace(String prefix, String url){
		String loc = null;
		this.addNamespace(prefix, url, loc);
	}
	
	/** Adds a namespace with schema location.
	 * @param prefix Your elements will be prefixed like that. May be "".
	 * @param url The unique URL that identifies the namespace
	 * @param schemaLocation The URL of the XML schema for this namespace.
	 * 	Maybe null if we don't have it.
	 * */
	public void addNamespace(String prefix, String url, String schemaLocation){
		//If we use namespaces with schema, we definitely need schemas.
		this.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

		if (prefix!=null && 0 < prefix.length()){
			this.addAttribute("xmlns:" + prefix, url);
		} else {//the default namespace
			this.addAttribute("xmlns", url);
		}
		if(schemaLocation!=null){
			this.schemaLocations.put(url, schemaLocation);
			this.setNamespaceLocations();
		}
	}

	/** Sets the "xsi:schemaLocation" attribute of the feed to the schema locations
	 * of all the added namespaces, overriding the previous value of "xsi:schemaLocation".
	 * */
	private void setNamespaceLocations(){
		StringBuilder schemaLocation = new StringBuilder();
		for(Map.Entry<String, String> entry : this.schemaLocations.entrySet()){
			String namespaceURL = entry.getKey();
			String schema = entry.getValue();
			schemaLocation.append(namespaceURL);
			schemaLocation.append(' ');
			schemaLocation.append(schema);
			schemaLocation.append(' ');
		}
		this.addAttribute("xsi:schemaLocation", schemaLocation.toString());
	}
	
	/** Returns all the namespace declarations in this elements. */
	public List<Namespace> getNamespaces(){
		List<Namespace>ns=new ArrayList<Namespace>();
		for(Map.Entry<String, String>attribute : this.attributes.entrySet()){
			String key=attribute.getKey();
			String value=attribute.getValue();
			if(key.startsWith("xmlns")){
				String prefix="";
				if(key.startsWith("xmlns:")){
					prefix=key.substring("xmlns:".length());
				}
				String url=value;
				String schemaLocation=this.schemaLocations.get(url);//may be null
				Namespace n=new Namespace(prefix, url, schemaLocation);
				ns.add(n);
			}
		}
		return ns;
	}
	
	/** Writes the feed to the output stream, using SAX. 
	 * See http://java.sun.com/webservices/jaxp/dist/1.1/docs/tutorial/xslt/3_generate.html
	 * @param os OutputStream
	 * @exception TransformerException
	 * */
	public void write(OutputStream os) throws TransformerException {
		// Converts this feed into a series of calls to a ContentHandler
		// that represent the feed as XML.
		BaseXMLReader xmlReader=new BaseXMLReader(this);
		if(this.getPrettyprint())
			xmlReader.setPrettyprint(true);
		InputSource inputSource=new InputSource();//Will be ignored: all data comes from xmlReader
		SAXSource source=new SAXSource(xmlReader, inputSource);
		StreamResult result=new StreamResult(os);
		Transformer transformer=DOM.newTransformer();
		transformer.transform(source, result);//TransformerException
	}

}