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

import inspiracio.xml.sax.EasyContentHandler;

import java.util.HashMap;
import java.util.Map;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/** A generator according to Atom 1.0 spec at 
 * http://atompub.org/rfc4287.html.
 * 
 * <p>
 * The "atom:generator" element's content identifies the agent used to 
 * generate a feed, for debugging and other purposes.
 * </p>
 * <pre>
 * atomGenerator = element atom:generator {
 * 		atomCommonAttributes,
 * 		attribute uri { atomUri }?,
 * 		attribute version { text }?,
 * 		text
 * }
 * </pre>
 * <p>
 * The content of this element, when present, MUST be a string that is 
 * a human-readable name for the generating agent. Entities such as 
 * "&amp;" and "&lt;" represent their corresponding characters ("&" and 
 * "<" respectively), not markup.
 * </p>
 * <p>
 * The atom:generator element MAY have a "uri" attribute whose value 
 * MUST be an IRI reference [RFC3987]. When dereferenced, the resulting 
 * URI (mapped from an IRI, if necessary) SHOULD produce a representation 
 * that is relevant to that agent.
 * </p>
 * <p>
 * The atom:generator element MAY have a "version" attribute that 
 * indicates the version of the generating agent.
 * */
public class Generator {

	private String uri;
	private String version;
	private String text = "";//obligatory
	
	/**
	 * @return the text
	 */
	String getText() {
		return text;
	}
	
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * @return the uri
	 */
	String getUri() {
		return uri;
	}
	
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * @return the version
	 */
	String getVersion() {
		return version;
	}
	
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/** Converts this generator into an XML DOM element.
	 * @param document The document where the element will go.
	 * @return Element
	 * */
	Element toElement(Document document){
		Element generator = document.createElement("generator");
		if (this.uri!=null){
			generator.setAttribute("uri", this.uri);
		}
		if (this.version!=null){
			generator.setAttribute("version", this.version);
		}
		org.w3c.dom.Text text = document.createTextNode(this.text);
		generator.appendChild(text);
		return generator;
	}
	
	/** Parses this generator into an XML SAX content handler.
	 * @param handler The content handler where the element will go.
	 * @exception SAXException
	 * */
	void parse(EasyContentHandler handler) throws SAXException {
		Map<String, String> attributes = new HashMap<String, String>();
		if (this.uri!=null){
			attributes.put("uri", this.uri);
		}
		if (this.version!=null){
			attributes.put("version", this.version);
		}
		handler.element("generator", attributes, this.text);
	}
	
}