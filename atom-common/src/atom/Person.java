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

import inspiracio.xml.DOM;
import inspiracio.xml.XMLString;
import inspiracio.xml.sax.EasyContentHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Person construct according to Atom 1.0 at 
 * http://atompub.org/rfc4287.html.
 * */
public class Person{

	//State -----------------------------------------------------------------
	private Text name;
	private String uri;
	private String email;
	//private List<ExtensionElement> extensionElements = new List<ExtensionElement>();//future
	
	//Constructors ----------------------------------------------------------
	
	public Person(){}
	public Person(String name){
		this.name=new PlainText(name);
	}
	
	//Accessors -------------------------------------------------------------
	
	/** Gets email 
	 * @return email
	 * */
	String getEmail(){return email;}

	/** Sets email 
	 * @param email
	 * */
	void setEmail(String email){this.email = email;}

	/** Gets URI 
	 * @return URI
	 * */
	String getUri(){return uri;}

	/** Sets URI 
	 * @param uri
	 * */
	void setUri(String uri){this.uri=uri;}

	/** Gets name 
	 * @return name
	 * */
	Text getName(){return name;}
	
	/** Sets the name of this person.
	 * The name is obligatory. 
	 * @param name Just the name, no XML, no HTML, no escaping.
	 * */
	public void setName(String name){this.name=new PlainText(name);}
	
	/** Sets the name of this person.
	 * The name is obligatory. 
	 * @param name as Atom Text element.
	 * */
	public void setName(Text name){this.name=name;}
	
	/** Gets URI 
	 * @return URI
	 * */
	String getURI(){return uri;}
	
	/** Sets the URI of this person.
	 * The URI is not obligatory. 
	 * @param uri No XML-escaping please */
	public void setURI(String uri){this.uri=uri;}
	
	/** Convert this person to an XML-DOM element with the
	 * given tag. Will include at least the obligatory name-element.
	 * @param document The document where the element will go.
	 * @param tag The tag of the element, example "author
	 * @return Element 
	 * */
	public Element toElement(Document document, String tag){
		Element element=document.createElement(tag);
		element.appendChild(this.name.toElement(document, "name"));
		if(this.uri!=null && 0<this.uri.length())
			DOM.appendTextElement(document, element, "uri", XMLString.escape(this.uri));
		if(this.email!=null && 0<this.email.length())
			DOM.appendTextElement(document, element, "email", XMLString.escape(this.email));
		return element;
	}
	
	/** Writes this person to an XML-SAX content handler with the
	 * given tag. Will include at least the obligatory name-element.
	 * @param handler
	 * @param tag The tag of the element, example "author"
	 * @exception SAXException
	 * */
	public void parse(EasyContentHandler handler, String tag) throws SAXException {
		handler.startElement(tag);
		this.name.parse(handler, "name");
		if(this.uri!=null && 0<this.uri.length())
			handler.element("uri", XMLString.escape(this.uri));
		if(this.email!=null && 0<this.email.length())
			handler.element("email", XMLString.escape(this.email));
		handler.endElement(tag);
	}
	
	/** Parse a person from an element. 
	 * @param element
	 * @return Person
	 * */
	static Person parse(Element element){
		Person person=new Person();
		
		//Attributes --not yet implemented in Person
		//person.base = DOM.getAttribute(element, "base");
		//person.lang = DOM.getAttribute(element, "lang");
		
		//Loop over all the children.
		//Each child element, treat it with the right type
		NodeList children = element.getChildNodes();
		for(int i=0; i<children.getLength(); i++){
			Node child = children.item(i);
			//ignore all node types except elements: attributes, text.
			if (child.getNodeType()==Node.ELEMENT_NODE){
				org.w3c.dom.Element childElement = (org.w3c.dom.Element)child;
				//discriminate on tag name. Can this be done better?
				String tagName = childElement.getTagName();
				if ("name".equals(tagName)){
					//1 name
					Text name = Text.parse(childElement);
					person.setName(name);
				} else if ("email".equals(tagName)){
					//? email
					String email = childElement.getTextContent();
					person.setEmail(email);
				} else if ("uri".equals(tagName)){
					//? uri
					String uri = childElement.getTextContent();
					person.setURI(uri);
				} else {
					//* extension elements ---not implemented yet.
					//inspiracio.xml.Element extensionElement = new inspiracio.xml.Element(childElement);
					//person.addExtElement(extensionElement);
					throw new RuntimeException("Extension elements in Person not implemented.");
				}
			}
		}
		return person;
	}
	
}